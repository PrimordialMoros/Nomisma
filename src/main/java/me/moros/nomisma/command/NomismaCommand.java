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

import java.util.Collection;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArgument.StringMode;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.locale.Message;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class NomismaCommand {
  private final CommandManager manager;
  private final MinecraftHelp<CommandSender> help;

  NomismaCommand(@NonNull CommandManager manager) {
    this.manager = manager;
    this.help = MinecraftHelp.createNative("/nomisma help", manager);
    this.help.setMaxResultsPerPage(8);
    construct();
  }

  private void construct() {
    var builder = manager.commandBuilder("nomisma")
      .meta(CommandMeta.DESCRIPTION, "Base command for Nomisma");
    //noinspection ConstantConditions
    manager.command(builder.handler(c -> help.queryCommands("", c.getSender())))
      .command(builder.literal("reload")
        .meta(CommandMeta.DESCRIPTION, "Reload the plugin")
        .permission(CommandPermissions.RELOAD)
        .handler(c -> onReload(c.getSender()))
      ).command(builder.literal("version")
        .meta(CommandMeta.DESCRIPTION, "View version info about Nomisma")
        .permission(CommandPermissions.VERSION)
        .handler(c -> onVersion(c.getSender()))
      ).command(builder.literal("list")
        .meta(CommandMeta.DESCRIPTION, "View all loaded currencies")
        .permission(CommandPermissions.LIST)
        .handler(c -> onListCurrencies(c.getSender()))
      ).command(builder.literal("help", "h")
        .meta(CommandMeta.DESCRIPTION, "View info about a command")
        .permission(CommandPermissions.HELP)
        .argument(StringArgument.optional("query", StringMode.GREEDY))
        .handler(c -> help.queryCommands(c.getOrDefault("query", ""), c.getSender()))
      );
  }

  public static void onReload(CommandSender sender) {
    Nomisma.translationManager().reload();
    Nomisma.configManager().reload();
    Message.RELOAD.send(sender);
  }

  public static void onVersion(CommandSender user) {
    String link = "https://github.com/PrimordialMoros/Nomisma";
    Component version = Message.brand(Component.text("Version: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(Nomisma.version(), NamedTextColor.GREEN))
      .hoverEvent(HoverEvent.showText(Message.VERSION_COMMAND_HOVER.build(Nomisma.author(), link)))
      .clickEvent(ClickEvent.openUrl(link));
    user.sendMessage(version);
  }

  public static void onListCurrencies(CommandSender user) {
    Collection<Component> currencies = Registries.CURRENCIES.stream().map(CurrencyUtil::createInfo).toList();
    int count = currencies.size();
    if (count == 0) {
      Message.NO_CURRENCIES.send(user);
      return;
    }
    Message.CURRENCIES_HEADER.send(user);
    currencies.forEach(user::sendMessage);
  }
}
