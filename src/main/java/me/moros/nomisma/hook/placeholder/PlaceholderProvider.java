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

package me.moros.nomisma.hook.placeholder;

import java.util.LinkedHashMap;
import java.util.Map;

import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PlaceholderProvider {
  private final Map<String, Placeholder> placeholders;

  PlaceholderProvider() {
    this.placeholders = setup();
  }

  private static Map<String, Placeholder> setup() {
    Builder builder = new Builder();
    builder.addDynamic("balance", (player, cur) -> {
      Currency currency = Registries.CURRENCIES.currency(cur);
      return currency == null ? "" : CurrencyUtil.format(player.balance(currency));
    });
    return builder.build();
  }

  public @Nullable String onPlaceholderRequest(@NonNull User player, @NonNull String placeholder) {
    for (var entry : placeholders.entrySet()) {
      String id = entry.getKey();
      Placeholder p = entry.getValue();
      if (p instanceof DynamicPlaceholder dp && placeholder.startsWith(id) && placeholder.length() > id.length()) {
        return dp.handle(player, placeholder.substring(id.length()));
      } else if (p instanceof StaticPlaceholder sp && placeholder.equals(id)) {
        return sp.handle(player);
      }
    }
    return null;
  }

  private static final class Builder {
    private final Map<String, Placeholder> placeholders;

    private Builder() {
      placeholders = new LinkedHashMap<>();
    }

    public void addStatic(String id, StaticPlaceholder placeholder) {
      this.placeholders.put(id, placeholder);
    }

    public void addDynamic(String id, DynamicPlaceholder placeholder) {
      this.placeholders.put(id + "_", placeholder);
    }

    public Map<String, Placeholder> build() {
      return Map.copyOf(placeholders);
    }
  }

  private interface Placeholder {
  }

  @FunctionalInterface
  private interface StaticPlaceholder extends Placeholder {
    @NonNull String handle(User player);
  }

  @FunctionalInterface
  private interface DynamicPlaceholder extends Placeholder {
    @NonNull String handle(User player, String argument);
  }
}
