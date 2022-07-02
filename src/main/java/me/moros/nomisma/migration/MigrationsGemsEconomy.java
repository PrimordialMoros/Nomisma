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

package me.moros.nomisma.migration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.Tasker;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MigrationsGemsEconomy implements MigrationUtility {
  private final GemsEconomy plugin;

  MigrationsGemsEconomy(MigrationType type) {
    this.plugin = (GemsEconomy) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(type.plugin()));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Boolean> apply(@NonNull Currency currency) {
    return Tasker.async(() -> migrate(currency));
  }

  private boolean migrate(Currency currency) {
    me.xanium.gemseconomy.currency.Currency gemsCurrency = plugin.getCurrencyManager().getDefaultCurrency();
    if (gemsCurrency == null) {
      return false;
    }
    List<Account> gemAccounts = plugin.getAccountManager().getAllAccounts();
    int count = 0;
    for (Account acc : gemAccounts) {
      UUID uuid = acc.getUuid();
      String name = acc.getNickname();
      if (uuid == null || name == null) {
        continue;
      }
      User user = Registries.USERS.userWithoutCache(uuid, name);
      user.set(currency, BigDecimal.valueOf(acc.getBalance(gemsCurrency)));
      count++;
    }
    int delta = gemAccounts.size() - count;
    if (delta > 0) {
      Nomisma.logger().warn(delta + " GemsEconomy account balances were NOT migrated due to missing data.");
    }
    return count > 0;
  }
}
