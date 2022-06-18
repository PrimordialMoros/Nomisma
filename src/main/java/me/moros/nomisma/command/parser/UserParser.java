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
import java.util.function.Predicate;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.parsers.PlayerArgument.PlayerParseException;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.moros.nomisma.command.CommandManager;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UserParser implements ArgumentParser<CommandSender, User> {
  @Override
  public @NonNull ArgumentParseResult<User> parse(@NonNull CommandContext<CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
    String input = CommandManager.sanitizeName(inputQueue.peek());
    if (input == null) {
      return ArgumentParseResult.failure(new NoInputProvidedException(UserParser.class, commandContext));
    }
    inputQueue.remove();
    if (input.equalsIgnoreCase("me") && commandContext.getSender() instanceof Player player) {
      return ArgumentParseResult.success(Registries.USERS.onlineUser(player));
    }
    Player player = Bukkit.getPlayer(input);
    if (player != null) {
      return ArgumentParseResult.success(Registries.USERS.onlineUser(player));
    }
    User user = Registries.USERS.userSync(input);
    if (user != null) {
      return ArgumentParseResult.success(user);
    }
    return ArgumentParseResult.failure(new PlayerParseException(input, commandContext));
  }

  @Override
  public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<CommandSender> commandContext, @NonNull String input) {
    Predicate<Player> canSee;
    if (commandContext.getSender() instanceof Player sender) {
      canSee = sender::canSee;
    } else {
      canSee = x -> true;
    }
    return List.copyOf(Bukkit.getOnlinePlayers()).stream().filter(canSee).map(Player::getName).toList();
  }
}
