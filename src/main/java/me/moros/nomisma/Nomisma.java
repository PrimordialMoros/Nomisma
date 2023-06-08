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

package me.moros.nomisma;

import java.nio.file.Path;
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
import me.moros.tasker.bukkit.BukkitExecutor;
import me.moros.tasker.executor.CompositeExecutor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
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

  private CompositeExecutor executor;

  @Override
  public void onEnable() {
    plugin = this;
    author = getDescription().getAuthors().get(0);
    version = getDescription().getVersion();
    logger = LoggerFactory.getLogger(getClass().getSimpleName());
    executor = CompositeExecutor.of(new BukkitExecutor(this));
    Path dir = plugin.getDataFolder().toPath();
    configManager = new ConfigManager(logger, dir);
    translationManager = new TranslationManager(logger, dir);
    loader = CurrencyLoader.createInstance(this, dir);
    if (loader == null) {
      logger.error("Could not create Currencies folder! Aborting plugin load.");
      setEnabled(false);
      return;
    }
    final long startTime = System.currentTimeMillis();
    loader.loadAllCurrencies().thenRun(() -> {
      long delta = System.currentTimeMillis() - startTime;
      int size = Registries.CURRENCIES.size();
      logger.info("Successfully loaded " + size + (size == 1 ? " currency" : " currencies") + " (" + delta + "ms)");
    });
    storage = Objects.requireNonNull(StorageFactory.createInstance(this), "Unable to connect to database!");
    Registries.CURRENCIES.forEach(storage::createColumn);

    Registries.USERS.init(this, storage);
    leaderboard = new Leaderboard(this, storage);
    handleHooks();
    try {
      new CommandManager(this);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      setEnabled(false);
      return;
    }
    getServer().getPluginManager().registerEvents(new PlayerListener(logger), this);
    configManager.save();
  }

  @Override
  public void onDisable() {
    if (vaultLayer != null) {
      Bukkit.getServicesManager().unregister(Economy.class, vaultLayer);
    }
    Registries.USERS.saveAll();
    configManager.close();
    executor.shutdown();
    storage.close();
    getServer().getScheduler().cancelTasks(this);
  }

  private void handleHooks() {
    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new NomismaExpansion(this).register();
    }
    Currency primary = Registries.CURRENCIES.primary();
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

  public Nomisma plugin() {
    return plugin;
  }

  public Path path() {
    return getDataFolder().toPath();
  }

  public String author() {
    return plugin.author;
  }

  public String version() {
    return plugin.version;
  }

  public Logger logger() {
    return plugin.logger;
  }

  public ConfigManager configManager() {
    return plugin.configManager;
  }

  public TranslationManager translationManager() {
    return plugin.translationManager;
  }

  public CurrencyLoader currencyLoader() {
    return plugin.loader;
  }

  public Leaderboard leaderboard() {
    return plugin.leaderboard;
  }

  public CompositeExecutor executor() {
    return executor;
  }
}
