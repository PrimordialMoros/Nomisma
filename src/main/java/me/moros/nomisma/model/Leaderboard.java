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
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.storage.EconomyStorage;

public class Leaderboard {
  public static final int MAX_PAGE = 10;
  public static final int ENTRIES_PER_PAGE = 10;

  private final AsyncLoadingCache<Currency, LeaderboardResult> cache;

  public Leaderboard(Nomisma plugin, EconomyStorage storage) {
    long time = plugin.configManager().config().leaderboardCacheMinutes();
    cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(time)).buildAsync(c -> storage.topBalances(c, 0, 100));
  }

  public CompletableFuture<LeaderboardResult> getTop(Currency currency) {
    return cache.get(currency);
  }

  public record LeaderboardResult(List<LeaderboardEntry> entries) {
    public Stream<LeaderboardEntry> stream() {
      return entries.stream();
    }

    @Override
    public List<LeaderboardEntry> entries() {
      return List.copyOf(entries);
    }
  }

  public record LeaderboardEntry(String name, BigDecimal balance) {
  }
}
