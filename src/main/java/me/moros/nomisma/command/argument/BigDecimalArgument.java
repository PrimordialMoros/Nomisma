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

package me.moros.nomisma.command.argument;

import java.math.BigDecimal;
import java.util.Queue;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.moros.nomisma.util.CurrencyUtil;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BigDecimalArgument extends CommandArgument<CommandSender, BigDecimal> {
  private BigDecimalArgument(String name, boolean integerOnly) {
    super(true, name, new BigDecimalParser(integerOnly), BigDecimal.class);
  }

  public static @NonNull CommandArgument<CommandSender, BigDecimal> of(@NonNull String name, boolean decimal) {
    return new BigDecimalArgument(name, !decimal);
  }

  private static final class BigDecimalParser implements ArgumentParser<CommandSender, BigDecimal> {
    private final boolean integerOnly;

    private BigDecimalParser(boolean integerOnly) {
      this.integerOnly = integerOnly;
    }

    @Override
    public @NonNull ArgumentParseResult<BigDecimal> parse(@NonNull CommandContext<CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
      final String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.failure(new NoInputProvidedException(BigDecimalParser.class, commandContext));
      }
      BigDecimal value = CurrencyUtil.parse(input);
      if (value == null) {
        return ArgumentParseResult.failure(new Throwable("Could not parse " + input));
      }
      if (value.signum() <= 0) {
        return ArgumentParseResult.failure(new Throwable("Cannot specify a non positive amount!"));
      }
      if (integerOnly && value.scale() > 0) {
        return ArgumentParseResult.failure(new Throwable("Cannot specify a decimal amount!"));
      }
      inputQueue.remove();
      return ArgumentParseResult.success(value);
    }

    @Override
    public boolean isContextFree() {
      return true;
    }
  }
}

