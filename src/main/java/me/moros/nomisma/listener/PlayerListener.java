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

package me.moros.nomisma.listener;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;

public final class PlayerListener implements Listener {
  private final Logger logger;

  public PlayerListener(Logger logger) {
    this.logger = logger;
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();
    String name = event.getName();
    long startTime = System.currentTimeMillis();
    try {
      Registries.USERS.forceLoad(uuid, name, 1000); // Timeout after 1000ms to not block the login thread excessively
      long deltaTime = System.currentTimeMillis() - startTime;
      if (deltaTime > 500) {
        logger.warn("Processing login for " + uuid + " took " + deltaTime + "ms.");
      }
    } catch (TimeoutException e) {
      logger.warn("Timed out while retrieving data for " + uuid);
    } catch (CancellationException | ExecutionException | InterruptedException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    String name = player.getName();
    User profile = Registries.USERS.forceLoad(uuid, name);
    Registries.USERS.register(profile);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    Registries.USERS.invalidate(event.getPlayer().getUniqueId());
  }
}
