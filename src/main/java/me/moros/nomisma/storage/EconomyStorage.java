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

package me.moros.nomisma.storage;

import java.util.UUID;

import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.Leaderboard.LeaderboardResult;
import me.moros.nomisma.model.User;
import me.moros.storage.Storage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EconomyStorage extends Storage {
  @NonNull User createProfile(@NonNull UUID uuid, @NonNull String name);

  @Nullable User loadProfile(@NonNull UUID uuid);

  @Nullable User loadProfile(@NonNull String name);

  void saveProfileAsync(@NonNull User user);

  @NonNull LeaderboardResult topBalances(@NonNull Currency currency, int offset, int limit);

  boolean createColumn(@NonNull Currency currency);
}
