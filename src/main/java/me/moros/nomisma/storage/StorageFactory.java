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

import java.io.File;

import me.moros.nomisma.Nomisma;
import me.moros.storage.ConnectionBuilder;
import me.moros.storage.StorageType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

/**
 * Factory class that constructs and returns a Hikari-based database storage.
 * @see EconomyStorage
 * @see StorageImpl
 */
public final class StorageFactory {
  private StorageFactory() {
  }

  public static @Nullable EconomyStorage createInstance() {
    CommentedConfigurationNode storageNode = Nomisma.configManager().config().node("storage");
    String configValue = storageNode.node("engine").getString("h2");
    StorageType engine = StorageType.parse(configValue, StorageType.H2);
    if (!configValue.equalsIgnoreCase(engine.toString()) || engine == StorageType.SQLITE) {
      engine = StorageType.H2;
      Nomisma.logger().warn("Failed to parse: " + configValue + ". Defaulting to H2.");
    }

    CommentedConfigurationNode connectionNode = storageNode.node("connection");
    String host = connectionNode.node("host").getString("localhost");
    int port = connectionNode.node("port").getInt(engine == StorageType.POSTGRESQL ? 5432 : 3306);
    String username = connectionNode.node("username").getString("economy");
    String password = connectionNode.node("password").getString("password");
    String database = connectionNode.node("database").getString("nomisma");

    String path = "";
    if (engine == StorageType.H2) {
      path = Nomisma.plugin().getDataFolder() + File.separator + "economy-h2;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE";
    }

    String poolName = engine.name() + " Nomisma Hikari Connection Pool";

    return ConnectionBuilder.create(StorageImpl::new, engine)
      .path(path).database(database).host(host).port(port)
      .username(username).password(password)
      .build(poolName, Nomisma.logger());
  }
}
