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

package me.moros.nomisma.storage;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.Leaderboard.LeaderboardResult;
import me.moros.nomisma.model.User;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EconomyStorage {
  void close();

  User createProfile(UUID uuid, String name);

  @Nullable User loadProfile(UUID uuid);

  @Nullable User loadProfile(String name);

  Collection<User> loadAllProfiles();

  void saveProfileAsync(User user);

  boolean saveProfile(User user, Map<Currency, BigDecimal> balance);

  LeaderboardResult topBalances(Currency currency, int offset, int limit);

  boolean createColumn(Currency currency);
}
