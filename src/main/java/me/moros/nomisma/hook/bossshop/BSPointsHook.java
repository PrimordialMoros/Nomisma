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

package me.moros.nomisma.hook.bossshop;

import java.math.BigDecimal;

import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import org.black_ixx.bossshop.pointsystem.BSPointsPlugin;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BSPointsHook extends BSPointsPlugin {
  private final Currency currency;

  public BSPointsHook(@NonNull Currency currency) {
    super("nomisma:" + currency.identifier());
    this.currency = currency;
    register();
  }

  @Override
  public boolean isAvailable() {
    return Registries.CURRENCIES.contains(currency);
  }

  @Override
  public double getPoints(OfflinePlayer player) {
    User user = Registries.USERS.userSync(player.getUniqueId());
    return user == null ? 0 : CurrencyUtil.doubleValue(user.balance(currency));
  }

  @Override
  public double setPoints(OfflinePlayer player, double points) {
    User user = Registries.USERS.userSync(player.getUniqueId());
    if (user == null) {
      return 0;
    } else {
      BigDecimal result = user.set(currency, BigDecimal.valueOf(points));
      return CurrencyUtil.doubleValue(result);
    }
  }

  @Override
  public double takePoints(OfflinePlayer player, double points) {
    User user = Registries.USERS.userSync(player.getUniqueId());
    if (user == null) {
      return 0;
    } else {
      BigDecimal result = user.subtract(currency, BigDecimal.valueOf(points));
      return CurrencyUtil.doubleValue(result);
    }
  }

  @Override
  public double givePoints(OfflinePlayer player, double points) {
    User user = Registries.USERS.userSync(player.getUniqueId());
    if (user == null) {
      return 0;
    } else {
      BigDecimal result = user.add(currency, BigDecimal.valueOf(points));
      return CurrencyUtil.doubleValue(result);
    }
  }

  @Override
  public boolean usesDoubleValues() {
    return currency.decimal();
  }
}
