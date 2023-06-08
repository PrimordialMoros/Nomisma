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

package me.moros.nomisma.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.moros.nomisma.registry.Registries;
import net.kyori.adventure.identity.Identity;
import org.bukkit.OfflinePlayer;

public class User implements Identity, BalanceHolder {
  private final UUID uuid;
  private final String name;
  private final Map<Currency, BigDecimal> balance;

  public User(UUID uuid, String name) {
    this(uuid, name, new ConcurrentHashMap<>());
  }

  public User(OfflinePlayer player) {
    this(player.getUniqueId(), Objects.requireNonNull(player.getName()), new ConcurrentHashMap<>());
  }

  public User(UUID uuid, String name, Map<Currency, BigDecimal> balance) {
    this.uuid = uuid;
    this.name = name;
    this.balance = new ConcurrentHashMap<>();
    this.balance.putAll(balance);
  }

  @Override
  public UUID uuid() {
    return uuid;
  }

  public String name() {
    return name;
  }

  @Override
  public BigDecimal balance(Currency currency) {
    Objects.requireNonNull(currency);
    return balance.computeIfAbsent(currency, c -> BigDecimal.ZERO);
  }

  @Override
  public BigDecimal set(Currency currency, BigDecimal amount) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(amount);
    balance.put(currency, amount);
    Registries.USERS.addPending(this);
    return amount;
  }

  @Override
  public BigDecimal add(Currency currency, BigDecimal amount) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(amount);
    BigDecimal result = balance.compute(currency, (c, bd) -> bd == null ? amount : bd.add(amount));
    Registries.USERS.addPending(this);
    return result;
  }

  @Override
  public BigDecimal subtract(Currency currency, BigDecimal amount) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(amount);
    BigDecimal bal = balance.computeIfAbsent(currency, c -> BigDecimal.ZERO);
    if (bal.compareTo(amount) < 0) {
      return set(currency, BigDecimal.ZERO);
    }
    BigDecimal result = bal.subtract(amount);
    balance.put(currency, result);
    Registries.USERS.addPending(this);
    return result;
  }

  @Override
  public Map<Currency, BigDecimal> balanceSnapshot() {
    return Map.copyOf(balance);
  }
}
