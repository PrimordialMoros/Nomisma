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

package me.moros.nomisma;

import java.util.Objects;

import me.moros.nomisma.command.CommandManager;
import me.moros.nomisma.config.ConfigManager;
import me.moros.nomisma.hook.bossshop.BSPointsHook;
import me.moros.nomisma.hook.placeholder.NomismaExpansion;
import me.moros.nomisma.hook.vault.VaultLayer;
import me.moros.nomisma.listener.PlayerListener;
import me.moros.nomisma.locale.TranslationManager;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.Leaderboard;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.storage.CurrencyLoader;
import me.moros.nomisma.storage.EconomyStorage;
import me.moros.nomisma.storage.StorageFactory;
import me.moros.nomisma.util.Tasker;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nomisma extends JavaPlugin {
  private static Nomisma plugin;

  private String author;
  private String version;

  private Logger logger;

  private ConfigManager configManager;
  private TranslationManager translationManager;

  private CurrencyLoader loader;
  private EconomyStorage storage;

  private VaultLayer vaultLayer;
  private Leaderboard leaderboard;

  @Override
  public void onEnable() {
    plugin = this;
    author = getDescription().getAuthors().get(0);
    version = getDescription().getVersion();
    logger = LoggerFactory.getLogger(getClass().getSimpleName());

    String dir = plugin.getDataFolder().toString();
    configManager = new ConfigManager(dir);
    translationManager = new TranslationManager(dir);

    loader = CurrencyLoader.createInstance(dir);
    if (loader == null) {
      logger.error("Could not create Currencies folder! Aborting plugin load.");
      setEnabled(false);
      return;
    }
    loader.loadAllCurrencies();
    storage = Objects.requireNonNull(StorageFactory.createInstance(), "Unable to connect to database!");
    Registries.CURRENCIES.forEach(storage::createColumn);

    Registries.USERS.init(storage);
    leaderboard = new Leaderboard(storage);
    handleHooks();

    try {
      new CommandManager(this);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      setEnabled(false);
      return;
    }
    getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    configManager.save();
  }

  @Override
  public void onDisable() {
    if (vaultLayer != null) {
      Bukkit.getServicesManager().unregister(Economy.class, vaultLayer);
    }
    Registries.USERS.forEach(storage::saveProfileAsync);
    Tasker.INSTANCE.shutdown();
    storage.close();
    getServer().getScheduler().cancelTasks(this);
  }

  private void handleHooks() {
    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new NomismaExpansion().register();
    }
    Currency primary = Registries.CURRENCIES.stream().filter(Currency::primary).findFirst().orElse(null);
    if (primary == null) {
      logger.warn("No primary currency detected!");
    } else {
      if (getServer().getPluginManager().isPluginEnabled("Vault")) {
        vaultLayer = new VaultLayer(this, primary);
        Bukkit.getServicesManager().register(Economy.class, vaultLayer, this, ServicePriority.Low);
      }
    }
    if (getServer().getPluginManager().getPlugin("BossShopPro") != null) {
      Registries.CURRENCIES.forEach(BSPointsHook::new);
    }
  }

  public static @MonotonicNonNull Nomisma plugin() {
    return plugin;
  }

  public static @MonotonicNonNull String author() {
    return plugin.author;
  }

  public static @MonotonicNonNull String version() {
    return plugin.version;
  }

  public static @MonotonicNonNull Logger logger() {
    return plugin.logger;
  }

  public static @MonotonicNonNull ConfigManager configManager() {
    return plugin.configManager;
  }

  public static @MonotonicNonNull TranslationManager translationManager() {
    return plugin.translationManager;
  }

  public static @MonotonicNonNull CurrencyLoader currencyLoader() {
    return plugin.loader;
  }

  public static @MonotonicNonNull Leaderboard leaderboard() {
    return plugin.leaderboard;
  }
}
