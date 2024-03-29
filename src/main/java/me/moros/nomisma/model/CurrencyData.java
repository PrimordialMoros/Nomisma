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

import java.util.Collection;

import me.moros.nomisma.util.CurrencyUtil;

public final class CurrencyData {
  private final String id;
  private final String formatRaw;
  private final String singularRaw;
  private final String pluralRaw;
  private final boolean decimal;
  private final boolean primary;
  private final String cmdPrefix;
  private final Collection<String> cmdAliases;

  public CurrencyData(Currency currency) {
    id = currency.identifier();
    formatRaw = currency.format();
    singularRaw = CurrencyUtil.MINI_SERIALIZER.serialize(currency.singular());
    pluralRaw = CurrencyUtil.MINI_SERIALIZER.serialize(currency.plural());
    decimal = currency.decimal();
    primary = currency.primary();
    cmdPrefix = currency.commandPrefix();
    cmdAliases = currency.commandAliases();
  }

  public String id() {
    return id;
  }

  public String formatRaw() {
    return formatRaw;
  }

  public String singularRaw() {
    return singularRaw;
  }

  public String pluralRaw() {
    return pluralRaw;
  }

  public boolean decimal() {
    return decimal;
  }

  public boolean primary() {
    return primary;
  }

  public String cmdPrefix() {
    return cmdPrefix;
  }

  public Collection<String> cmdAliases() {
    return cmdAliases;
  }
}
