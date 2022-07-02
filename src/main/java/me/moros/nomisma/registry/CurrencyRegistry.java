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

package me.moros.nomisma.registry;

import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import me.moros.nomisma.model.Currency;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CurrencyRegistry implements Registry<Currency> {
  private final Map<String, Currency> currencies;
  private Currency primary;
  private boolean locked = false;

  CurrencyRegistry() {
    currencies = new ConcurrentHashMap<>();
  }

  public int registerAndLock(@NonNull Iterable<@NonNull Currency> currencies) {
    if (locked) {
      return 0;
    }
    int counter = 0;
    for (Currency currency : currencies) {
      if (register(currency)) {
        counter++;
      }
    }
    locked = true;
    primary = stream().filter(Currency::primary).findFirst().orElse(null);
    return counter;
  }

  private boolean register(@NonNull Currency currency) {
    if (!contains(currency)) {
      currencies.put(currency.identifier(), currency);
      return true;
    }
    return false;
  }

  public boolean contains(@NonNull Currency currency) {
    return currencies.containsKey(currency.identifier());
  }

  public @Nullable Currency currency(@Nullable String id) {
    return (id == null || id.isEmpty()) ? null : currencies.get(id.toLowerCase(Locale.ROOT));
  }

  public @Nullable Currency primary() {
    return primary;
  }

  public int size() {
    return currencies.size();
  }

  public @NonNull Stream<Currency> stream() {
    return currencies.values().stream();
  }

  @Override
  public @NonNull Iterator<Currency> iterator() {
    return Collections.unmodifiableCollection(currencies.values()).iterator();
  }
}
