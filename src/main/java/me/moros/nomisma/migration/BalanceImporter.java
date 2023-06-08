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

package me.moros.nomisma.migration;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BalanceImporter {
  private final Nomisma parent;

  public BalanceImporter(Nomisma parent) {
    this.parent = parent;
  }

  public CompletableFuture<Boolean> importData() {
    return parent.executor().async().submit(this::importBalances);
  }

  private boolean importBalances() {
    Map<Currency, FileConfiguration> data = new HashMap<>();
    for (Currency c : Registries.CURRENCIES) {
      File file = parent.path().resolve(c.identifier() + ".yml").toFile();
      if (file.exists()) {
        data.put(c, YamlConfiguration.loadConfiguration(file));
      }
    }
    if (data.isEmpty()) {
      return false;
    }
    Map<UUID, TempUser> userMap = new ConcurrentHashMap<>();
    for (var entry : data.entrySet()) {
      populate(entry.getKey(), entry.getValue(), userMap);
    }
    if (userMap.isEmpty()) {
      return false;
    }
    userMap.values().stream().map(u -> new User(u.uuid, u.name, u.balance)).forEach(Registries.USERS::addPending);
    return true;
  }

  private void populate(Currency currency, FileConfiguration config, Map<UUID, TempUser> userMap) {
    ConfigurationSection pointsSection = config.getConfigurationSection("Points");
    if (pointsSection == null) {
      pointsSection = config.getConfigurationSection("Players");
    }
    if (pointsSection == null) {
      return;
    }

    ConfigurationSection uuidSection = config.getConfigurationSection("UUIDs");
    if (uuidSection != null) {
      for (String uuidString : uuidSection.getKeys(false)) {
        UUID uuid = UUID.fromString(uuidString);
        String name = uuidSection.getString(uuidString);
        userMap.putIfAbsent(uuid, new TempUser(uuid, name));
      }
    }

    for (String uuidString : pointsSection.getKeys(false)) {
      UUID uuid = UUID.fromString(uuidString);
      TempUser user = userMap.get(uuid);
      if (user != null) {
        user.balance.putIfAbsent(currency, BigDecimal.valueOf(Math.max(0, pointsSection.getLong(uuidString))));
      }
    }
  }

  private record TempUser(UUID uuid, @Nullable String name, Map<Currency, BigDecimal> balance) {
    private TempUser(UUID uuid, @Nullable String name) {
      this(uuid, name, new ConcurrentHashMap<>());
    }
  }
}
