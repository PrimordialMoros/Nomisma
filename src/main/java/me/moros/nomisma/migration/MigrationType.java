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

package me.moros.nomisma.migration;

import java.util.function.BiFunction;

import me.moros.nomisma.Nomisma;

public enum MigrationType {
  ESSENTIALS("Essentials", MigrationEssentials::new),
  GEMS("GemsEconomy", MigrationsGemsEconomy::new);

  private final String pluginName;
  private final BiFunction<Nomisma, MigrationType, MigrationUtility> supplier;

  MigrationType(String pluginName, BiFunction<Nomisma, MigrationType, MigrationUtility> supplier) {
    this.pluginName = pluginName;
    this.supplier = supplier;
  }

  public String plugin() {
    return pluginName;
  }

  public MigrationUtility utility(Nomisma parent) {
    return supplier.apply(parent, this);
  }
}
