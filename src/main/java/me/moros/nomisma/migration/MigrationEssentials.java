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

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.earth2me.essentials.Essentials;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.Tasker;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MigrationEssentials implements MigrationUtility {
  private final Essentials plugin;

  MigrationEssentials(MigrationType type) {
    this.plugin = (Essentials) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(type.plugin()));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Boolean> apply(@NonNull Currency currency) {
    return Tasker.async(() -> migrate(currency));
  }

  private boolean migrate(Currency currency) {
    if (plugin.getSettings().isEcoDisabled()) {
      return false;
    }
    Collection<UUID> essUsers = plugin.getUserMap().getAllUniqueUsers();
    int count = 0;
    for (UUID uuid : essUsers) {
      com.earth2me.essentials.User essUser = plugin.getUserMap().getUser(uuid);
      if (essUser == null) {
        continue;
      }
      String name = essUser.getName();
      if (name == null) {
        continue;
      }
      User user = Registries.USERS.userWithoutCache(uuid, name);
      user.set(currency, essUser.getMoney());
      count++;
    }
    int delta = essUsers.size() - count;
    if (delta > 0) {
      Nomisma.logger().warn(delta + " Essentials account balances were NOT migrated due to missing data.");
    }
    return count > 0;
  }
}
