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
import java.util.List;

import cloud.commandframework.Command.Builder;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.meta.CommandMeta;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.locale.Message;
import me.moros.nomisma.model.BalanceHolder;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.Leaderboard;
import me.moros.nomisma.model.Leaderboard.LeaderboardEntry;
import me.moros.nomisma.model.User;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.CurrencyUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class DynamicCurrencyCommand {
  private final CommandManager manager;
  private final Currency currency;
  private Builder<CommandSender> nonPrimaryBuilder;

  DynamicCurrencyCommand(@NonNull CommandManager manager, @NonNull Currency currency) {
    this.manager = manager;
    this.currency = currency;
    construct();
  }

  private void construct() {
    var pageArg = IntegerArgument.<CommandSender>newBuilder("page").withMin(1)
      .asOptionalWithDefault(1);
    var userArg = manager.argumentBuilder(User.class, "target")
      .asOptionalWithDefault("me");

    manager.command(builder("pay", "transfer")
      .meta(CommandMeta.DESCRIPTION, "Transfer the specified amount to another player")
      .permission(currency.permission() + ".pay")
      .argument(userArg.asRequired().build())
      .argument(manager.argumentBuilder(BigDecimal.class, "amount"))
      .senderType(Player.class)
      .handler(c -> onPay(c.getSender(), c.get("target"), c.get("amount")))
    ).command(builder("balancemodify", "balmod")
      .meta(CommandMeta.DESCRIPTION, "Modify the specified player's balance")
      .permission(currency.permission() + ".modify")
      .argument(EnumArgument.of(ModifierOperation.class, "operation"))
      .argument(userArg.build())
      .argument(manager.argumentBuilder(BigDecimal.class, "amount"))
      .handler(c -> onModify(c.getSender(), c.get("operation"), c.get("target"), c.get("amount")))
    ).command(builder("balance", "bal")
      .meta(CommandMeta.DESCRIPTION, "View the specified player's balance")
      .permission(currency.permission() + ".balance")
      .argument(userArg.build())
      .handler(c -> onBalance(c.getSender(), c.get("target")))
    ).command(builder("balancetop", "baltop", "lead")
      .meta(CommandMeta.DESCRIPTION, "View a page of the balance leaderboard")
      .permission(currency.permission() + ".balancetop")
      .argument(pageArg.build())
      .handler(c -> onBalanceTop(c.getSender(), c.get("page")))
    );
  }

  private void onPay(CommandSender commandSender, User target, BigDecimal amount) {
    User sender = Registries.USERS.onlineUser((Player) commandSender);
    if (!sender.uuid().equals(target.uuid())) {
      if (!sender.has(currency, amount)) {
        Message.INSUFFICIENT_FUNDS.send(commandSender, currency.plural());
      }
      Component value = Component.text(CurrencyUtil.format(amount), NamedTextColor.GOLD);
      BigDecimal newBalanceSender = sender.subtract(currency, amount);
      BigDecimal newBalanceReceiver = target.add(currency, amount);
      Message.PAID_SENDER.send(commandSender, value, target.name());
      Player onlineTarget = Bukkit.getPlayer(target.uuid());
      if (onlineTarget != null) {
        Message.PAID_RECEIVER.send(onlineTarget, value, commandSender.getName());
      }
    } else {
      Message.TARGET_SELF.send(commandSender);
    }
  }

  private void onModify(CommandSender commandSender, ModifierOperation operation, User target, BigDecimal amount) {
    BigDecimal result = operation.apply(target, currency, amount);
    Message.MODIFY_BALANCE_SUCCESS.send(commandSender, target.name(), CurrencyUtil.format(currency, result));
  }

  private void onBalance(CommandSender commandSender, User target) {
    Message.BALANCE.send(commandSender, target.name(), CurrencyUtil.format(currency, target.balance(currency)));
  }

  private void onBalanceTop(CommandSender commandSender, Integer page) {
    if (page > Leaderboard.MAX_PAGE) {
      Message.BALANCETOP_MAX_PAGE.send(commandSender, Leaderboard.MAX_PAGE);
      return;
    }
    Nomisma.leaderboard().getTop(currency).thenAccept(result -> {
      int offset = (page - 1) * Leaderboard.ENTRIES_PER_PAGE;
      List<LeaderboardEntry> filteredEntries = result.stream().skip(offset).limit(Leaderboard.ENTRIES_PER_PAGE).toList();
      if (filteredEntries.isEmpty()) {
        Message.BALANCETOP_EMPTY.send(commandSender);
        return;
      }
      Message.BALANCETOP_HEADER.send(commandSender, page);
      var it = filteredEntries.listIterator();
      String pf = page >= 10 ? "> %3d. " : "> %2d. ";
      while (it.hasNext()) {
        LeaderboardEntry entry = it.next();
        int position = offset + it.nextIndex();
        Component text = Component.text().append(Component.text(String.format(pf, position)))
          .append(Component.text(entry.name())).append(Component.text(" - "))
          .append(CurrencyUtil.format(currency, entry.balance())).build();
        commandSender.sendMessage(text);
      }
    });
  }

  private Builder<CommandSender> builder(String cmd, String... aliases) {
    if (currency.primary()) {
      return manager.commandBuilder(cmd, aliases);
    } else {
      if (nonPrimaryBuilder == null) {
        nonPrimaryBuilder = manager.commandBuilder(
          currency.commandPrefix(), currency.commandAliases(), CommandMeta.simple().build()
        );
      }
      return nonPrimaryBuilder.literal(cmd, aliases);
    }
  }

  private enum ModifierOperation implements BalanceOperation {
    SET(BalanceHolder::set),
    ADD(BalanceHolder::add),
    SUBTRACT(BalanceHolder::subtract);

    private final BalanceOperation function;

    ModifierOperation(BalanceOperation function) {
      this.function = function;
    }

    @Override
    public BigDecimal apply(BalanceHolder holder, Currency currency, BigDecimal value) {
      return function.apply(holder, currency, value);
    }
  }

  private interface BalanceOperation {
    BigDecimal apply(BalanceHolder holder, Currency currency, BigDecimal value);
  }
}
