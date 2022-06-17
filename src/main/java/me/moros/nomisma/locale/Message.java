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

package me.moros.nomisma.locale;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.ConsoleCommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

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

  Args0 TARGET_SELF = () -> translatable("nomisma.command.target-self", RED);
  Args0 NO_CURRENCIES = () -> translatable("nomisma.command.list.no-currencies", RED);
  Args0 CURRENCIES_HEADER = () -> translatable("nomisma.command.list.header", DARK_AQUA);

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
    .args(Component.text(maxPage));

  Args0 BALANCETOP_EMPTY = () -> translatable("nomisma.command.balancetop.empty", RED);

  Args1<Integer> BALANCETOP_HEADER = page -> translatable("nomisma.command.balancetop.header", DARK_AQUA)
    .args(Component.text(page, AQUA));

  Args0 RELOAD = () -> brand(translatable("nomisma.command.reload", GREEN));

  Args2<String, String> VERSION_COMMAND_HOVER = (author, link) -> translatable("nomisma.command.version.hover", DARK_AQUA)
    .args(text(author, GREEN), text(link, GREEN));

  static @NonNull Component brand(@NonNull ComponentLike message) {
    return text().append(PREFIX).append(message).build();
  }

  interface Args0 {
    @NonNull Component build();

    default void send(@NonNull Audience audience) {
      if (audience instanceof ConsoleCommandSender) {
        audience.sendMessage(GlobalTranslator.render(build(), TranslationManager.DEFAULT_LOCALE));
        return;
      }
      audience.sendMessage(build());
    }
  }

  interface Args1<A0> {
    @NonNull Component build(@NonNull A0 arg0);

    default void send(@NonNull Audience audience, @NonNull A0 arg0) {
      if (audience instanceof ConsoleCommandSender) {
        audience.sendMessage(GlobalTranslator.render(build(arg0), TranslationManager.DEFAULT_LOCALE));
        return;
      }
      audience.sendMessage(build(arg0));
    }
  }

  interface Args2<A0, A1> {
    @NonNull Component build(@NonNull A0 arg0, @NonNull A1 arg1);

    default void send(@NonNull Audience audience, @NonNull A0 arg0, @NonNull A1 arg1) {
      if (audience instanceof ConsoleCommandSender) {
        audience.sendMessage(GlobalTranslator.render(build(arg0, arg1), TranslationManager.DEFAULT_LOCALE));
        return;
      }
      audience.sendMessage(build(arg0, arg1));
    }
  }
}
