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
import java.util.Map;

public interface BalanceHolder {
  BigDecimal balance(Currency currency);

  BigDecimal set(Currency currency, BigDecimal amount);

  BigDecimal add(Currency currency, BigDecimal amount);

  BigDecimal subtract(Currency currency, BigDecimal amount);

  Map<Currency, BigDecimal> balanceSnapshot();

  default boolean has(Currency currency, BigDecimal amount) {
    return balance(currency).compareTo(amount) >= 0;
  }
}
