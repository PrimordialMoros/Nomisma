/*
 * Copyright 2022 Moros
 *
 * This file is part of Nomisma.
 *
 * Nomisma is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nomisma is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Nomisma. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.nomisma.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.zaxxer.hikari.HikariDataSource;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.Leaderboard.LeaderboardEntry;
import me.moros.nomisma.model.Leaderboard.LeaderboardResult;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.storage.sql.SqlQueries;
import me.moros.nomisma.util.Tasker;
import me.moros.storage.SqlStreamReader;
import me.moros.storage.StorageType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;

public final class StorageImpl implements EconomyStorage {
  private final HikariDataSource source;
  private final StorageType type;
  private final Logger logger;
  private final Jdbi DB;

  StorageImpl(@NonNull StorageType type, @NonNull Logger logger, @NonNull HikariDataSource source) {
    this.type = type;
    this.logger = logger;
    this.source = source;
    DB = Jdbi.create(this.source);
    if (type != StorageType.H2 && type != StorageType.POSTGRESQL) {
      DB.registerArgument(new UUIDArgumentFactory());
    }
    if (!tableExists("nomisma_players")) {
      init();
    }
  }

  private void init() {
    InputStream stream = Objects.requireNonNull(Nomisma.plugin().getResource(type.schemaPath()), "Null schema.");
    Collection<String> statements = SqlStreamReader.parseQueries(stream);
    DB.useHandle(handle -> {
      Batch batch = handle.createBatch();
      statements.forEach(batch::add);
      batch.execute();
    });
  }

  @Override
  public @NonNull StorageType type() {
    return type;
  }

  @Override
  public void close() {
    source.close();
  }

  @Override
  public @NonNull User createProfile(@NonNull UUID uuid, @NonNull String name) {
    User profile = loadProfile(uuid);
    if (profile == null) {
      profile = DB.withHandle(handle -> {
        handle.createUpdate(SqlQueries.PLAYER_INSERT.query())
          .bind(0, uuid).bind(1, name).execute();
        return new User(uuid, name);
      });
    }
    return profile;
  }

  private User profileRowMapper(ResultSet rs, StatementContext ctx) throws SQLException {
    Map<Currency, BigDecimal> balance = new HashMap<>();
    for (Currency c : Registries.CURRENCIES) {
      BigDecimal value = rs.getBigDecimal(c.identifier());
      balance.put(c, value == null ? BigDecimal.ZERO : value);
    }
    UUID uuid = rs.getObject("player_uuid", UUID.class);
    String name = rs.getString("player_name");
    return new User(uuid, name, balance);
  }

  @Override
  public @Nullable User loadProfile(@NonNull UUID uuid) {
    try {
      User temp = DB.withHandle(handle ->
        handle.createQuery(SqlQueries.PLAYER_SELECT_BY_UUID.query())
          .bind(0, uuid).map(this::profileRowMapper).findOne().orElse(null)
      );
      if (temp != null && uuid.equals(temp.uuid())) {
        return temp;
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public @Nullable User loadProfile(@NonNull String name) {
    try {
      User temp = DB.withHandle(handle ->
        handle.createQuery(SqlQueries.PLAYER_SELECT_BY_NAME.query())
          .bind(0, name).map(this::profileRowMapper).findOne().orElse(null)
      );
      if (temp != null && name.equalsIgnoreCase(temp.name())) {
        return temp;
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public @NonNull Collection<@NonNull User> loadAllProfiles() {
    try {
      return DB.withHandle(handle ->
        handle.createQuery(SqlQueries.PLAYER_SELECT_ALL.query()).map(this::profileRowMapper).stream().toList()
      );
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return List.of();
  }

  @Override
  public void saveProfileAsync(@NonNull User user) {
    Map<Currency, BigDecimal> snapshot = user.balanceSnapshot();
    Tasker.async(() -> saveProfile(user, snapshot));
  }

  @Override
  public boolean saveProfile(@NonNull User user, @NonNull Map<@NonNull Currency, @NonNull BigDecimal> balance) {
    try {
      var map = balance.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().identifier(), Entry::getValue));
      String query = SqlQueries.updateProfile(map.keySet());
      DB.useHandle(handle -> handle.createUpdate(query).bind("player_name", user.name())
        .bind("player_uuid", user.uuid()).bindMap(map).execute());
      return true;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  @Override
  public @NonNull LeaderboardResult topBalances(@NonNull Currency currency, int offset, int limit) {
    try {
      String query = SqlQueries.selectTop(currency, offset, limit);
      return new LeaderboardResult(DB.withHandle(handle -> handle.createQuery(query).map(this::leaderboardMapper).list()));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return new LeaderboardResult(List.of());
  }

  private LeaderboardEntry leaderboardMapper(ResultSet rs, StatementContext ctx) throws SQLException {
    return new LeaderboardEntry(rs.getString("player_name"), rs.getBigDecimal("balance"));
  }

  @Override
  public boolean createColumn(@NonNull Currency currency) {
    try {
      boolean exists = DB.withHandle(handle -> handle.queryMetadata(d -> d.getColumns(null, null, "%", null))
        .map(x -> x.getColumn("COLUMN_NAME", String.class)).stream().anyMatch(currency.identifier()::equalsIgnoreCase)
      );
      if (!exists) {
        DB.withHandle(handle -> handle.createUpdate(SqlQueries.addColumn(currency)).execute());
        return true;
      }
    } catch (Exception e) {
      logger.warn(e.getMessage(), e);
    }
    return false;
  }

  private boolean tableExists(String table) {
    try {
      return DB.withHandle(handle -> {
        String catalog = handle.getConnection().getCatalog();
        return handle.queryMetadata(d -> d.getTables(catalog, null, "%", null))
          .map(x -> x.getColumn("TABLE_NAME", String.class)).stream().anyMatch(table::equalsIgnoreCase);
      });
    } catch (Exception e) {
      logger.warn(e.getMessage(), e);
    }
    return false;
  }

  private static final class UUIDArgumentFactory extends AbstractArgumentFactory<UUID> {
    private UUIDArgumentFactory() {
      super(Types.BINARY);
    }

    @Override
    protected Argument build(UUID value, ConfigRegistry config) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
      buffer.putLong(value.getMostSignificantBits());
      buffer.putLong(value.getLeastSignificantBits());
      ByteArrayInputStream stream = new ByteArrayInputStream(buffer.array());
      return (position, statement, ctx) -> statement.setBinaryStream(position, stream);
    }
  }
}
