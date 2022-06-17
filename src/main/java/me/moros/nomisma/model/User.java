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

package me.moros.nomisma.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.identity.Identity;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class User implements Identity, BalanceHolder {
  private final UUID uuid;
  private final String name;
  private final Map<Currency, BigDecimal> balance;

  public User(@NonNull UUID uuid, @NonNull String name) {
    this(uuid, name, new ConcurrentHashMap<>());
  }

  public User(@NonNull OfflinePlayer player) {
    this(player.getUniqueId(), Objects.requireNonNull(player.getName()), new ConcurrentHashMap<>());
  }

  public User(@NonNull UUID uuid, @NonNull String name, @NonNull Map<@NonNull Currency, @NonNull BigDecimal> balance) {
    this.uuid = uuid;
    this.name = name;
    this.balance = new ConcurrentHashMap<>();
    this.balance.putAll(balance);
  }

  @Override
  public @NonNull UUID uuid() {
    return uuid;
  }

  public @NonNull String name() {
    return name;
  }

  @Override
  public @NonNull BigDecimal balance(@NonNull Currency currency) {
    Objects.requireNonNull(currency);
    return balance.getOrDefault(currency, BigDecimal.ZERO);
  }

  @Override
  public @NonNull BigDecimal set(@NonNull Currency currency, @NonNull BigDecimal amount) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(amount);
    balance.replace(currency, amount);
    return amount;
  }

  @Override
  public @NonNull BigDecimal add(@NonNull Currency currency, @NonNull BigDecimal amount) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(amount);
    BigDecimal result = balance.computeIfPresent(currency, (c, bd) -> bd.add(amount));
    return result == null ? BigDecimal.ZERO : result;
  }

  @Override
  public @NonNull BigDecimal subtract(@NonNull Currency currency, @NonNull BigDecimal amount) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(amount);
    BigDecimal bal = balance.get(currency);
    if (bal == null) {
      return BigDecimal.ZERO;
    }
    if (bal.compareTo(amount) < 0) {
      return bal;
    }
    BigDecimal result = bal.subtract(amount);
    balance.put(currency, result);
    return result;
  }

  @Override
  public @NonNull Map<@NonNull Currency, @NonNull BigDecimal> balanceSnapshot() {
    return Map.copyOf(balance);
  }
}
