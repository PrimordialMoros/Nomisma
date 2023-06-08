/*
 * Copyright 2022-2023 Moros
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import me.moros.nomisma.Nomisma;
import me.moros.storage.Builder;
import me.moros.storage.StorageDataSource;
import me.moros.storage.StorageType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Factory class that constructs and returns a Hikari-based database storage.
 * @see EconomyStorage
 * @see StorageImpl
 */
public final class StorageFactory {
  private StorageFactory() {
  }

  public static @Nullable EconomyStorage createInstance(Nomisma plugin) {
    Config config = plugin.configManager().config(List.of("storage"), new Config());
    Builder builder = StorageDataSource.builder(config.type).database(config.database)
      .host(config.host).port(config.port).username(config.username).password(config.password);
    builder.configure(c -> {
      c.setMaximumPoolSize(config.poolSettings.maximumPoolSize);
      c.setMinimumIdle(config.poolSettings.minimumIdle);
      c.setMaxLifetime(config.poolSettings.maxLifetime);
      c.setKeepaliveTime(config.poolSettings.keepAliveTime);
      c.setConnectionTimeout(config.poolSettings.connectionTimeout);
    });
    if (config.type.isLocal()) {
      switch (config.type) {
        case HSQL -> builder.properties(p -> {
          p.put("sql.syntax_mys", true);
          p.put("hsqldb.default_table_type", "cached");
        });
        case H2 -> builder.properties(p -> {
          p.put("MODE", "PostgreSQL");
          p.put("DB_CLOSE_ON_EXIT", false);
        });
      }
      Path parent = plugin.path().resolve("data").resolve(config.type.realName());
      try {
        Files.createDirectories(parent);
      } catch (IOException e) {
        plugin.logger().error(e.getMessage(), e);
        return null;
      }
      // Convert to uri and back to path - needed only for windows compatibility
      var uri = parent.resolve("nomisma" + (config.type == StorageType.SQLITE ? ".db" : "")).toUri();
      builder.path(Path.of(uri));
    }
    StorageDataSource data = builder.build("nomisma-hikari");
    if (data != null) {
      var storage = new StorageImpl(plugin, data);
      storage.init(plugin::getResource);
      return storage;
    }
    return null;
  }

  @ConfigSerializable
  private static final class Config {
    private final StorageType type = StorageType.H2;
    private final String host = "localhost";
    private final int port = 3306;
    private final String username = "nomisma";
    private final String password = "password";
    private final String database = "nomisma";
    private final PoolSettings poolSettings = new PoolSettings();
  }

  @ConfigSerializable
  private static final class PoolSettings {
    private final int maximumPoolSize = 6;
    private final int minimumIdle = 6;
    private final int maxLifetime = 1_800_000;
    private final int keepAliveTime = 0;
    private final int connectionTimeout = 5000;
  }
}
