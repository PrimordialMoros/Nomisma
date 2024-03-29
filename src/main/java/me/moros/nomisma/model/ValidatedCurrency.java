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

import java.util.Collection;
import java.util.Set;

import me.moros.nomisma.util.CurrencyUtil;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;

public non-sealed class ValidatedCurrency implements Currency {
  public static final ValidatedCurrency EXAMPLE;

  static {
    StringMeta meta = new StringMeta("money", "<dark_green>$<amount></dark_green>", "<dark_green>$</dark_green>");
    EXAMPLE = new ValidatedCurrency(meta, true, true, new CmdMeta("money"));
  }

  private final String identifier;

  private final String format;
  private final String singularPlain;
  private final Component singular;
  private final String pluralPlain;
  private final Component plural;

  private final boolean decimal;
  private final boolean primary;

  private final String commandPrefix;
  private final Collection<String> commandAliases;

  private ValidatedCurrency(StringMeta meta, boolean decimal, boolean primary, CmdMeta cmdMeta) {
    identifier = meta.identifier();
    format = meta.formatRaw();
    singularPlain = CurrencyUtil.MINI_SERIALIZER.stripTags(meta.singularRaw());
    singular = CurrencyUtil.MINI_SERIALIZER.deserialize(meta.singularRaw());
    pluralPlain = CurrencyUtil.MINI_SERIALIZER.stripTags(meta.pluralRaw());
    plural = CurrencyUtil.MINI_SERIALIZER.deserialize(meta.pluralRaw());
    this.decimal = decimal;
    this.primary = primary;
    commandPrefix = cmdMeta.commandPrefix();
    commandAliases = Set.copyOf(cmdMeta.commandAliases());
  }

  @Override
  public String identifier() {
    return identifier;
  }

  @Override
  public String format() {
    return format;
  }

  @Override
  public String singularPlain() {
    return singularPlain;
  }

  @Override
  public Component singular() {
    return singular;
  }

  @Override
  public String pluralPlain() {
    return pluralPlain;
  }

  @Override
  public Component plural() {
    return plural;
  }

  @Override
  public boolean decimal() {
    return decimal;
  }

  @Override
  public boolean primary() {
    return primary;
  }

  @Override
  public String commandPrefix() {
    return commandPrefix;
  }

  @Override
  public Collection<String> commandAliases() {
    return Set.copyOf(commandAliases);
  }

  public static @Nullable Currency validatedCopy(@Nullable CurrencyData currency) {
    if (currency == null) {
      return null;
    }
    String id = CurrencyUtil.sanitizeInput(currency.id());
    if (id == null) {
      return null;
    }
    String cmdPrefix = CurrencyUtil.sanitizeCmdString(currency.cmdPrefix());
    Collection<String> aliases = currency.cmdAliases().stream()
      .map(CurrencyUtil::sanitizeCmdString).filter(s -> !s.isEmpty()).toList();
    StringMeta meta = new StringMeta(id, currency.formatRaw(), currency.singularRaw(), currency.pluralRaw());
    CmdMeta cmdMeta = new CmdMeta(cmdPrefix, aliases);
    return new ValidatedCurrency(meta, currency.decimal(), currency.primary(), cmdMeta);
  }

  private record StringMeta(String identifier, String formatRaw, String singularRaw, String pluralRaw) {
    private StringMeta(String identifier, String formatRaw, String raw) {
      this(identifier, formatRaw, raw, raw);
    }
  }

  private record CmdMeta(String commandPrefix, Collection<String> commandAliases) {
    private CmdMeta(String commandPrefix) {
      this(commandPrefix, Set.of());
    }
  }
}
