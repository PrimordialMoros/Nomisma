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

package me.moros.nomisma.hook.vault;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class VaultLayer implements Economy {
  private final Nomisma plugin;
  private final Currency primary;

  public VaultLayer(@NonNull Nomisma plugin, @NonNull Currency primary) {
    this.plugin = plugin;
    this.primary = primary;
  }

  @Override
  public boolean isEnabled() {
    return plugin.isEnabled();
  }

  @Override
  public String getName() {
    return "Nomisma Economy";
  }

  @Override
  public boolean hasBankSupport() {
    return false;
  }

  @Override
  public int fractionalDigits() {
    return -1;
  }

  @Override
  public String format(double amount) {
    return CurrencyUtil.SYMBOL + CurrencyUtil.format(BigDecimal.valueOf(amount));
  }

  @Override
  public String currencyNamePlural() {
    return primary.pluralPlain();
  }

  @Override
  public String currencyNameSingular() {
    return primary.singularPlain();
  }

  @Deprecated
  @Override
  public boolean hasAccount(String playerName) {
    return Registries.USERS.userSync(playerName) != null;
  }

  @Override
  public boolean hasAccount(OfflinePlayer player) {
    return Registries.USERS.userSync(player.getUniqueId()) != null;
  }

  @Deprecated
  @Override
  public boolean hasAccount(String playerName, String worldName) {
    return hasAccount(playerName);
  }

  @Override
  public boolean hasAccount(OfflinePlayer player, String worldName) {
    return hasAccount(player);
  }

  private double doubleValue(BigDecimal value) {
    double amount = value.doubleValue();
    return BigDecimal.valueOf(amount).compareTo(value) > 0 ? Math.nextAfter(amount, Double.NEGATIVE_INFINITY) : amount;
  }

  @Deprecated
  @Override
  public double getBalance(String playerName) {
    User user = Registries.USERS.userSync(playerName);
    return user == null ? 0 : doubleValue(user.balance(primary));
  }

  @Override
  public double getBalance(OfflinePlayer player) {
    User user = Registries.USERS.userSync(player.getUniqueId());
    return user == null ? 0 : doubleValue(user.balance(primary));
  }

  @Deprecated
  @Override
  public double getBalance(String playerName, String world) {
    return getBalance(playerName);
  }

  @Override
  public double getBalance(OfflinePlayer player, String world) {
    return getBalance(player);
  }

  @Deprecated
  @Override
  public boolean has(String playerName, double amount) {
    User user = Registries.USERS.userSync(playerName);
    return user != null && user.has(primary, BigDecimal.valueOf(amount));
  }

  @Override
  public boolean has(OfflinePlayer player, double amount) {
    User user = Registries.USERS.userSync(player.getUniqueId());
    return user != null && user.has(primary, BigDecimal.valueOf(amount));
  }

  @Deprecated
  @Override
  public boolean has(String playerName, String worldName, double amount) {
    return has(playerName, amount);
  }

  @Override
  public boolean has(OfflinePlayer player, String worldName, double amount) {
    return has(player, amount);
  }

  @Deprecated
  @Override
  public EconomyResponse withdrawPlayer(String playerName, double amount) {
    if (playerName == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player name cannot be null!");
    }
    if (amount < 0) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds!");
    }
    User user = Registries.USERS.userSync(playerName);
    if (user == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "User does not exist!");
    }
    BigDecimal balance = user.balance(primary);
    BigDecimal bdAmount = BigDecimal.valueOf(amount);
    if (balance.compareTo(bdAmount) < 0) {
      return new EconomyResponse(0, doubleValue(balance), ResponseType.FAILURE, "Loan was not permitted!");
    }
    BigDecimal result = user.subtract(primary, bdAmount);
    return new EconomyResponse(amount, doubleValue(result), ResponseType.SUCCESS, null);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
    if (player == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player name cannot be null!");
    }
    if (amount < 0) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds!");
    }
    User user = Registries.USERS.userSync(player.getUniqueId());
    if (user == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "User does not exist!");
    }
    BigDecimal balance = user.balance(primary);
    BigDecimal bdAmount = BigDecimal.valueOf(amount);
    if (balance.compareTo(bdAmount) < 0) {
      return new EconomyResponse(0, doubleValue(balance), ResponseType.FAILURE, "Loan was not permitted!");
    }
    BigDecimal result = user.subtract(primary, bdAmount);
    return new EconomyResponse(amount, doubleValue(result), ResponseType.SUCCESS, null);
  }

  @Deprecated
  @Override
  public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
    return withdrawPlayer(playerName, amount);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
    return withdrawPlayer(player, amount);
  }

  @Deprecated
  @Override
  public EconomyResponse depositPlayer(String playerName, double amount) {
    if (playerName == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player name cannot be null!");
    }
    if (amount < 0) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds!");
    }
    User user = Registries.USERS.userSync(playerName);
    if (user == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "User does not exist!");
    }
    BigDecimal balance = user.balance(primary);
    BigDecimal bdAmount = BigDecimal.valueOf(amount);
    if (balance.compareTo(bdAmount) < 0) {
      return new EconomyResponse(0, doubleValue(balance), ResponseType.FAILURE, "Loan was not permitted!");
    }
    BigDecimal result = user.add(primary, bdAmount);
    return new EconomyResponse(amount, doubleValue(result), ResponseType.SUCCESS, null);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
    if (player == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player name cannot be null!");
    }
    if (amount < 0) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds!");
    }
    User user = Registries.USERS.userSync(player.getUniqueId());
    if (user == null) {
      return new EconomyResponse(0, 0, ResponseType.FAILURE, "User does not exist!");
    }
    BigDecimal balance = user.balance(primary);
    BigDecimal bdAmount = BigDecimal.valueOf(amount);
    if (balance.compareTo(bdAmount) < 0) {
      return new EconomyResponse(0, doubleValue(balance), ResponseType.FAILURE, "Loan was not permitted!");
    }
    BigDecimal result = user.subtract(primary, bdAmount);
    return new EconomyResponse(amount, doubleValue(result), ResponseType.SUCCESS, null);
  }

  @Deprecated
  @Override
  public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
    return depositPlayer(playerName, amount);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
    return depositPlayer(player, amount);
  }

  @Deprecated
  @Override
  public boolean createPlayerAccount(String playerName) {
    return false;
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player) {
    return false;
  }

  @Deprecated
  @Override
  public boolean createPlayerAccount(String playerName, String worldName) {
    return createPlayerAccount(playerName);
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
    return createPlayerAccount(player);
  }

  private static final EconomyResponse noBank = new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank is closed!");

  @Deprecated
  @Override
  public EconomyResponse createBank(String name, String player) {
    return noBank;
  }

  @Override
  public EconomyResponse createBank(String name, OfflinePlayer player) {
    return noBank;
  }

  @Override
  public EconomyResponse deleteBank(String name) {
    return noBank;
  }

  @Override
  public EconomyResponse bankBalance(String name) {
    return noBank;
  }

  @Override
  public EconomyResponse bankHas(String name, double amount) {
    return noBank;
  }

  @Override
  public EconomyResponse bankWithdraw(String name, double amount) {
    return noBank;
  }

  @Override
  public EconomyResponse bankDeposit(String name, double amount) {
    return noBank;
  }

  @Deprecated
  @Override
  public EconomyResponse isBankOwner(String name, String playerName) {
    return noBank;
  }

  @Override
  public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
    return noBank;
  }

  @Deprecated
  @Override
  public EconomyResponse isBankMember(String name, String playerName) {
    return noBank;
  }

  @Override
  public EconomyResponse isBankMember(String name, OfflinePlayer player) {
    return noBank;
  }

  @Override
  public List<String> getBanks() {
    return Collections.emptyList();
  }
}
