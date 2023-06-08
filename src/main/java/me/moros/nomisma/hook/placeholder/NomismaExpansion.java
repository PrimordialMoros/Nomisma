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

package me.moros.nomisma.hook.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NomismaExpansion extends PlaceholderExpansion {
  private final Nomisma plugin;
  private final PlaceholderProvider provider;

  public NomismaExpansion(Nomisma plugin) {
    this.plugin = plugin;
    this.provider = new PlaceholderProvider();
  }

  @Override
  public @NonNull String getAuthor() {
    return plugin.author();
  }

  @Override
  public @NonNull String getIdentifier() {
    return "nomisma";
  }

  @Override
  public @NonNull String getVersion() {
    return plugin.version();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onPlaceholderRequest(@Nullable Player player, String params) {
    if (player == null) {
      return "";
    }
    User user = Registries.USERS.userSync(player.getUniqueId());
    return user == null ? "" : provider.onPlaceholderRequest(user, params);
  }
}
