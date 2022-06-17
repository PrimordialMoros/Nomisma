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

package me.moros.nomisma.command;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import io.leangen.geantyref.TypeToken;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.command.parser.BigDecimalParser;
import me.moros.nomisma.command.parser.UserParser;
import me.moros.nomisma.locale.Message;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandManager extends PaperCommandManager<CommandSender> {
  public CommandManager(@NonNull Nomisma plugin) throws Exception {
    super(plugin, AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().withSynchronousParsing().build(), Function.identity(), Function.identity());
    registerParsers();
    registerExceptionHandler();
    registerAsynchronousCompletions();
    setCommandSuggestionProcessor(this::suggestionProvider);
    new NomismaCommand(this);
    for (Currency currency : Registries.CURRENCIES) {
      new DynamicCurrencyCommand(this, currency);
    }
  }

  private void registerParsers() {
    getParserRegistry().registerParserSupplier(TypeToken.get(BigDecimal.class), options -> new BigDecimalParser());
    getParserRegistry().registerParserSupplier(TypeToken.get(User.class), options -> new UserParser());
  }

  private void registerExceptionHandler() {
    new MinecraftExceptionHandler<CommandSender>()
      .withInvalidSyntaxHandler()
      .withInvalidSenderHandler()
      .withNoPermissionHandler()
      .withArgumentParsingHandler()
      .withCommandExecutionHandler()
      .withDecorator(Message::brand)
      .apply(this, AudienceProvider.nativeAudience());
  }

  private List<String> suggestionProvider(CommandPreprocessingContext<CommandSender> context, List<String> strings) {
    String input;
    if (context.getInputQueue().isEmpty()) {
      input = "";
    } else {
      input = context.getInputQueue().peek().toLowerCase(Locale.ROOT);
    }
    List<String> suggestions = new LinkedList<>();
    for (String suggestion : strings) {
      if (suggestion.toLowerCase(Locale.ROOT).startsWith(input)) {
        suggestions.add(suggestion);
      }
    }
    return suggestions;
  }
}
