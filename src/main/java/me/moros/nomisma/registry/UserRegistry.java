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

package me.moros.nomisma.registry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.storage.EconomyStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Registry for all valid users.
 */
public final class UserRegistry implements Registry<User> {
  private final Map<UUID, User> onlineUsers;
  private final Map<User, Map<Currency, BigDecimal>> pending;

  private Nomisma parent;
  private EconomyStorage storage;
  private AsyncLoadingCache<UUID, User> cache;

  UserRegistry() {
    onlineUsers = new ConcurrentHashMap<>();
    pending = new ConcurrentHashMap<>();
  }

  public void init(Nomisma plugin, EconomyStorage storage) {
    if (cache == null) {
      this.parent = Objects.requireNonNull(plugin);
      this.storage = Objects.requireNonNull(storage);
      cache = Caffeine.newBuilder().maximumSize(100).expireAfterAccess(Duration.ofMinutes(20))
        .buildAsync(this.storage::loadProfile);
      long ticks = 1200 * Math.max(1, plugin.configManager().config().saveIntervalMinutes());
      plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::processTasks, 1, ticks);
    }
  }

  private void processTasks() {
    if (pending.isEmpty()) {
      return;
    }
    var copy = new HashMap<>(pending);
    pending.clear();
    copy.forEach(storage::saveProfile);
  }

  public @Nullable User userSync(String name) {
    OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
    if (player != null) {
      return userSync(player.getUniqueId());
    } else {
      if (cache != null) {
        User user = storage.loadProfile(name);
        if (user != null) {
          cache.synchronous().put(user.uuid(), user);
          return user;
        }
      }
    }
    return null;
  }

  public @Nullable User userSync(UUID uuid) {
    return cache == null ? null : cache.synchronous().get(uuid);
  }

  public User userWithoutCache(UUID uuid, String name) {
    return storage.createProfile(uuid, name);
  }

  public CompletableFuture<@Nullable User> user(String name) {
    OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
    if (player != null) {
      return user(player.getUniqueId());
    } else {
      if (cache != null) {
        return parent.executor().async().submit(() -> storage.loadProfile(name)).thenApply(user -> {
          if (user != null) {
            cache.synchronous().put(user.uuid(), user);
            return user;
          }
          return null;
        });
      }
    }
    return CompletableFuture.completedFuture(null);
  }

  public CompletableFuture<@Nullable User> user(UUID uuid) {
    return cache == null ? CompletableFuture.completedFuture(null) : cache.get(uuid);
  }

  public User forceLoad(UUID uuid, String name) {
    onlineUsers.remove(uuid);
    cache.synchronous().invalidate(uuid);
    return cache.synchronous().get(uuid, id -> storage.createProfile(uuid, name));
  }

  public User forceLoad(UUID uuid, String name, long timeout) throws ExecutionException, InterruptedException, TimeoutException {
    onlineUsers.remove(uuid);
    cache.synchronous().invalidate(uuid);
    return cache.get(uuid, id -> storage.createProfile(uuid, name)).get(timeout, TimeUnit.MILLISECONDS);
  }

  public User onlineUser(Player player) {
    return Objects.requireNonNull(onlineUsers.get(player.getUniqueId()));
  }

  public void invalidate(UUID uuid) {
    User user = onlineUsers.remove(uuid);
    if (user != null && cache != null) {
      cache.synchronous().invalidate(uuid);
      storage.saveProfileAsync(user);
    }
  }

  public void saveAll() {
    onlineUsers.values().forEach(u -> pending.put(u, u.balanceSnapshot()));
    processTasks();
  }

  public void register(User user) {
    onlineUsers.putIfAbsent(user.uuid(), user);
  }

  public Stream<User> stream() {
    return onlineUsers.values().stream();
  }

  @Override
  public Iterator<User> iterator() {
    return Collections.unmodifiableCollection(onlineUsers.values()).iterator();
  }

  public void addPending(User user) {
    pending.put(user, user.balanceSnapshot());
  }
}
