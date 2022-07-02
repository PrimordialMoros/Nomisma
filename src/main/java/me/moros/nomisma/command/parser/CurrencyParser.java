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

package me.moros.nomisma.command.parser;

import java.util.List;
import java.util.Queue;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CurrencyParser implements ArgumentParser<CommandSender, Currency> {
  @Override
  public @NonNull ArgumentParseResult<Currency> parse(@NonNull CommandContext<CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
    String input = CurrencyUtil.sanitizeInput(inputQueue.peek());
    if (input == null) {
      return ArgumentParseResult.failure(new NoInputProvidedException(CurrencyParser.class, commandContext));
    }
    inputQueue.remove();
    Currency primary = Registries.CURRENCIES.primary();
    if (primary != null && input.equalsIgnoreCase("primary")) {
      return ArgumentParseResult.success(primary);
    }
    Currency currency = Registries.CURRENCIES.currency(input);
    if (currency != null) {
      return ArgumentParseResult.success(currency);
    }
    return ArgumentParseResult.failure(new NoInputProvidedException(Currency.class, commandContext));
  }

  @Override
  public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<CommandSender> commandContext, @NonNull String input) {
    return Registries.CURRENCIES.stream().map(Currency::identifier).toList();
  }
}
