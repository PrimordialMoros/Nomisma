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

package me.moros.nomisma.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reactive.Subscriber;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;
import org.spongepowered.configurate.serialize.SerializationException;

public final class ConfigManager {
  private final Config defaultConfig;

  private final Logger logger;
  private final WatchServiceListener listener;
  private final ConfigurationReference<CommentedConfigurationNode> reference;
  private final ValueReference<Config, CommentedConfigurationNode> configReference;

  public ConfigManager(Logger logger, Path directory) {
    this.logger = logger;
    this.defaultConfig = new Config();
    Path path = directory.resolve("nomisma.conf");
    try {
      Files.createDirectories(path.getParent());
      listener = WatchServiceListener.create();
      reference = listener.listenToConfiguration(f -> HoconConfigurationLoader.builder().path(f).build(), path);
      configReference = reference.referenceTo(Config.class, NodePath.path(), defaultConfig);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void subscribe(Subscriber<? super CommentedConfigurationNode> subscriber) {
    reference.updates().subscribe(subscriber);
  }

  public void save() {
    try {
      reference.save();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  public void close() {
    try {
      reference.close();
      listener.close();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  public Config config() {
    Config config = configReference.get();
    return config == null ? defaultConfig : config;
  }

  @SuppressWarnings("unchecked")
  public <T> T config(Iterable<String> path, T def) {
    try {
      return (T) reference.get(path).get(def.getClass(), def);
    } catch (SerializationException e) {
      logger.warn(e.getMessage(), e);
    }
    return def;
  }
}
