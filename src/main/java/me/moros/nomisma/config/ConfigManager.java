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

package me.moros.nomisma.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import me.moros.nomisma.Nomisma;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

public final class ConfigManager {
  private final HoconConfigurationLoader loader;

  private CommentedConfigurationNode configRoot;

  public ConfigManager(@NonNull String directory) {
    Path path = Paths.get(directory, "nomisma.conf");
    loader = HoconConfigurationLoader.builder().path(path).build();
    try {
      Files.createDirectories(path.getParent());
      configRoot = loader.load();
    } catch (IOException e) {
      Nomisma.logger().warn(e.getMessage(), e);
    }
  }

  public void reload() {
    try {
      configRoot = loader.load();
    } catch (IOException e) {
      Nomisma.logger().warn(e.getMessage(), e);
    }
  }

  public void save() {
    try {
      Nomisma.logger().info("Saving nomisma config");
      loader.save(configRoot);
    } catch (IOException e) {
      Nomisma.logger().warn(e.getMessage(), e);
    }
  }

  public @NonNull CommentedConfigurationNode config() {
    return configRoot;
  }
}
