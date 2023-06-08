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

package me.moros.nomisma.config;

import me.moros.nomisma.locale.Message;
import me.moros.nomisma.util.CurrencyUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record Config(String geyserUsernamePrefix, String brand, long leaderboardCacheMinutes, long saveIntervalMinutes) {
  public Config(@Nullable String geyserUsernamePrefix, @Nullable String brand, long leaderboardCacheMinutes, long saveIntervalMinutes) {
    this.geyserUsernamePrefix = geyserUsernamePrefix == null ? "." : geyserUsernamePrefix;
    this.brand = brand == null ? CurrencyUtil.MINI_SERIALIZER.serialize(Message.PREFIX) : brand;
    this.leaderboardCacheMinutes = leaderboardCacheMinutes > 0 ? leaderboardCacheMinutes : 5;
    this.saveIntervalMinutes = saveIntervalMinutes > 0 ? saveIntervalMinutes : 10;
  }

  Config() {
    this(".", CurrencyUtil.MINI_SERIALIZER.serialize(Message.PREFIX), 5, 10);
  }
}
