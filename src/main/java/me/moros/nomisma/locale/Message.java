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

package me.moros.nomisma.locale;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.ConsoleCommandSender;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * @see TranslationManager
 */
public interface Message {
  Component PREFIX = text("[", DARK_GRAY)
    .append(text("Nomisma", DARK_AQUA))
    .append(text("] ", DARK_GRAY));

  Args0 CONFIRM_REQUIRED = () -> translatable("nomisma.command.confirm.required", YELLOW);
  Args0 CONFIRM_NO_PENDING = () -> translatable("nomisma.command.confirm.no-pending", RED);

  Args0 NO_CURRENCIES = () -> translatable("nomisma.command.list.no-currencies", RED);
  Args0 CURRENCIES_HEADER = () -> translatable("nomisma.command.list.header", DARK_AQUA);

  Args0 TARGET_SELF = () -> translatable("nomisma.command.pay.target-self", RED);
  Args1<Component> INSUFFICIENT_FUNDS = currency -> translatable("nomisma.command.pay.insufficient-funds", YELLOW)
    .args(currency);

  Args2<Component, String> PAID_SENDER = (currency, target) -> translatable("nomisma.command.pay.success.sender", GREEN)
    .args(currency, text(target, GRAY));

  Args2<Component, String> PAID_RECEIVER = (currency, target) -> translatable("nomisma.command.pay.success.receiver", GREEN)
    .args(currency, text(target, GRAY));

  Args2<String, Component> BALANCE = (target, balance) -> translatable("nomisma.command.balance", GREEN)
    .args(text(target, GRAY), balance);

  Args2<String, Component> MODIFY_BALANCE_SUCCESS = (target, balance) -> translatable("nomisma.command.modifybalance.success", GREEN)
    .args(text(target, GRAY), balance);

  Args1<Integer> BALANCETOP_MAX_PAGE = maxPage -> translatable("nomisma.command.balancetop.max-page", YELLOW)
    .args(text(maxPage));

  Args0 BALANCETOP_EMPTY = () -> translatable("nomisma.command.balancetop.empty", RED);

  Args1<Integer> BALANCETOP_HEADER = page -> translatable("nomisma.command.balancetop.header", DARK_AQUA)
    .args(text(page, AQUA));

  Args0 RELOAD = () -> brand(translatable("nomisma.command.reload", GREEN));

  Args2<String, String> MIGRATE_STARTED = (plugin, currency) -> translatable("nomisma.command.migrate.started", GREEN)
    .args(text(plugin), text(currency, DARK_AQUA));
  Args2<String, String> MIGRATE_SUCCESS = (plugin, currency) -> translatable("nomisma.command.migrate.success", GREEN)
    .args(text(plugin), text(currency, DARK_AQUA));
  Args1<String> MIGRATE_NOT_LOADED = plugin -> translatable("nomisma.command.migrate.not-loaded", RED)
    .args(text(plugin));
  Args1<String> MIGRATE_ERROR = plugin -> translatable("nomisma.command.migrate.error", RED)
    .args(text(plugin));
  Args0 IMPORT_SUCCESS = () -> translatable("nomisma.command.import.success", GREEN);
  Args0 IMPORT_ERROR = () -> translatable("nomisma.command.import.error", RED);

  Args2<String, String> VERSION_COMMAND_HOVER = (author, link) -> translatable("nomisma.command.version.hover", DARK_AQUA)
    .args(text(author, GREEN), text(link, GREEN));

  static Component brand(ComponentLike message) {
    return text().append(PREFIX).append(message).build();
  }

  interface Args0 {
    Component build();

    default void send(Audience audience) {
      if (audience instanceof ConsoleCommandSender) {
        audience.sendMessage(GlobalTranslator.render(build(), TranslationManager.DEFAULT_LOCALE));
        return;
      }
      audience.sendMessage(build());
    }
  }

  interface Args1<A0> {
    Component build(A0 arg0);

    default void send(Audience audience, A0 arg0) {
      if (audience instanceof ConsoleCommandSender) {
        audience.sendMessage(GlobalTranslator.render(build(arg0), TranslationManager.DEFAULT_LOCALE));
        return;
      }
      audience.sendMessage(build(arg0));
    }
  }

  interface Args2<A0, A1> {
    Component build(A0 arg0, A1 arg1);

    default void send(Audience audience, A0 arg0, A1 arg1) {
      if (audience instanceof ConsoleCommandSender) {
        audience.sendMessage(GlobalTranslator.render(build(arg0, arg1), TranslationManager.DEFAULT_LOCALE));
        return;
      }
      audience.sendMessage(build(arg0, arg1));
    }
  }
}
