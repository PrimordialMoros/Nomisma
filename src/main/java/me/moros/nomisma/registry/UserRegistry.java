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

package me.moros.nomisma.registry;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.moros.nomisma.model.User;
import me.moros.nomisma.storage.EconomyStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Registry for all valid users.
 */
public final class UserRegistry implements Registry<User> {
  private AsyncLoadingCache<UUID, User> cache;
  private final Map<UUID, User> onlineUsers;

  UserRegistry() {
    onlineUsers = new ConcurrentHashMap<>();
  }

  public void init(@NonNull EconomyStorage storage) {
    if (cache == null) {
      Objects.requireNonNull(storage);
      cache = Caffeine.newBuilder().maximumSize(100).expireAfterAccess(Duration.ofMinutes(20))
        .buildAsync(storage::loadProfile);
    }
  }

  public @Nullable User userSync(@NonNull String name) {
    OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
    return player == null ? null : userSync(player.getUniqueId());
  }

  public @Nullable User userSync(@NonNull UUID uuid) {
    return cache == null ? null : cache.synchronous().get(uuid);
  }

  public @NonNull CompletableFuture<@Nullable User> user(@NonNull String name) {
    OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
    return player == null ? CompletableFuture.completedFuture(null) : user(player.getUniqueId());
  }

  public @NonNull CompletableFuture<@Nullable User> user(@NonNull UUID uuid) {
    return cache == null ? CompletableFuture.completedFuture(null) : cache.get(uuid);
  }

  public @NonNull User onlineUser(@NonNull Player player) {
    return Objects.requireNonNull(onlineUsers.get(player.getUniqueId()));
  }

  public void invalidate(@NonNull UUID uuid) {
    onlineUsers.remove(uuid);
  }

  public void register(@NonNull User user) {
    onlineUsers.putIfAbsent(user.uuid(), user);
  }

  public @NonNull Stream<User> stream() {
    return onlineUsers.values().stream();
  }

  @Override
  public @NonNull Iterator<User> iterator() {
    return Collections.unmodifiableCollection(onlineUsers.values()).iterator();
  }
}
