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

package me.moros.nomisma.command;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import io.leangen.geantyref.TypeToken;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.command.parser.CurrencyParser;
import me.moros.nomisma.command.parser.UserParser;
import me.moros.nomisma.locale.Message;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import static net.kyori.adventure.text.Component.text;

public final class CommandManager extends PaperCommandManager<CommandSender> {
  private static Pattern INVALID_NAMES;

  private final Nomisma plugin;
  private final MinecraftHelp<CommandSender> help;
  private final CommandConfirmationManager<CommandSender> confirmationManager;

  public CommandManager(Nomisma plugin) throws Exception {
    super(plugin, AsynchronousCommandExecutionCoordinator.<CommandSender>builder().withSynchronousParsing().build(), Function.identity(), Function.identity());
    this.plugin = plugin;
    String prefix = plugin.configManager().config().geyserUsernamePrefix();
    INVALID_NAMES = Pattern.compile("[^" + prefix + "_A-Za-z0-9]");
    registerParsers();
    registerExceptionHandler(plugin);
    registerAsynchronousCompletions();
    commandSuggestionProcessor(this::suggestionProvider);

    help = MinecraftHelp.createNative("/nomisma help", this);
    help.setMaxResultsPerPage(8);

    confirmationManager = createConfirmationManager();
    confirmationManager.registerConfirmationProcessor(this);

    new NomismaCommand(this);
    for (Currency currency : Registries.CURRENCIES) {
      new DynamicCurrencyCommand(this, currency);
    }
  }

  public MinecraftHelp<CommandSender> help() {
    return help;
  }

  public static @Nullable String sanitizeName(@Nullable String input) {
    if (input == null || INVALID_NAMES == null) {
      return null;
    }
    String output = INVALID_NAMES.matcher(input).replaceAll("");
    if (output.isEmpty()) {
      return null;
    }
    return output.length() > 17 ? output.substring(0, 17) : output;
  }

  private void registerParsers() {
    parserRegistry().registerParserSupplier(TypeToken.get(User.class), options -> new UserParser());
    parserRegistry().registerParserSupplier(TypeToken.get(Currency.class), options -> new CurrencyParser());
  }

  private void registerExceptionHandler(Nomisma plugin) {
    String prefix = plugin.configManager().config().brand();
    Component prefixComponent = CurrencyUtil.MINI_SERIALIZER.deserializeOr(prefix, Message.PREFIX);
    new MinecraftExceptionHandler<CommandSender>()
      .withInvalidSyntaxHandler()
      .withInvalidSenderHandler()
      .withNoPermissionHandler()
      .withArgumentParsingHandler()
      .withCommandExecutionHandler()
      .withDecorator(c -> text().append(prefixComponent).append(c).build())
      .apply(this, AudienceProvider.nativeAudience());
  }

  private CommandConfirmationManager<CommandSender> createConfirmationManager() {
    return new CommandConfirmationManager<>(30L,
      TimeUnit.SECONDS,
      ctx -> Message.CONFIRM_REQUIRED.send(ctx.getCommandContext().getSender()),
      Message.CONFIRM_NO_PENDING::send
    );
  }

  CommandExecutionHandler<CommandSender> confirmationHandler() {
    return confirmationManager.createConfirmationExecutionHandler();
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

  @Override
  public Nomisma getOwningPlugin() {
    return plugin;
  }
}
