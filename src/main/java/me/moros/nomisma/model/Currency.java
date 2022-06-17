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

import java.util.Collection;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public sealed interface Currency permits ValidatedCurrency {
  @NonNull String identifier();

  @NonNull String singularPlain();

  @NonNull Component singular();

  @NonNull String pluralPlain();

  @NonNull Component plural();

  boolean decimal();

  boolean primary();

  @NonNull String commandPrefix();

  @NonNull Collection<@NonNull String> commandAliases();

  default @NonNull String permission() {
    return "nomisma.command." + identifier();
  }
}
