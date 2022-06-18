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

package me.moros.nomisma.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

import me.moros.nomisma.model.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CurrencyUtil {
  private static final DecimalFormat FORMAT;
  private static final Pattern ILLEGAL_IDENTIFIER = Pattern.compile("[^_A-Za-z0-9]");
  private static final Pattern NON_ALPHABETICAL = Pattern.compile("[^A-Za-z]");

  public static final MiniMessage MINI_SERIALIZER = MiniMessage.miniMessage();

  static {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator(',');
    symbols.setDecimalSeparator('.');

    FORMAT = new DecimalFormat("#0.##", symbols);
    FORMAT.setRoundingMode(RoundingMode.FLOOR);
    FORMAT.setParseBigDecimal(true);
    FORMAT.setGroupingUsed(true);
    FORMAT.setMinimumFractionDigits(0);
    FORMAT.setMaximumFractionDigits(2);
  }

  private CurrencyUtil() {
  }

  public static double doubleValue(@NonNull BigDecimal value) {
    double amount = value.doubleValue();
    return BigDecimal.valueOf(amount).compareTo(value) > 0 ? Math.nextAfter(amount, Double.NEGATIVE_INFINITY) : amount;
  }

  public static @Nullable String sanitizeInput(@NonNull String input) {
    String output = ILLEGAL_IDENTIFIER.matcher(input).replaceAll("").toLowerCase(Locale.ROOT);
    if (output.isEmpty()) {
      return null;
    }
    return output.length() > 16 ? output.substring(0, 16) : output;
  }

  public static @NonNull String sanitizeCmdString(@NonNull String input) {
    String output = NON_ALPHABETICAL.matcher(input).replaceAll("").toLowerCase(Locale.ROOT);
    return output.length() > 16 ? output.substring(0, 16) : output;
  }

  public static @Nullable BigDecimal parse(@NonNull String input) {
    try {
      BigDecimal value = (BigDecimal) FORMAT.parse(input);
      return value.stripTrailingZeros();
    } catch (Exception e) {
      return null;
    }
  }

  public static @NonNull Component format(@NonNull Currency currency, @NonNull BigDecimal value) {
    Component form = value.compareTo(BigDecimal.ONE) > 0 ? currency.plural() : currency.singular();
    TagResolver resolver = TagResolver.builder()
      .resolver(Placeholder.component("currency", form))
      .resolver(Placeholder.unparsed("amount", FORMAT.format(value))).build();
    return MINI_SERIALIZER.deserialize(currency.format(), resolver);
  }

  public static @NonNull String formatPlain(@NonNull Currency currency, @NonNull BigDecimal value) {
    return PlainTextComponentSerializer.plainText().serialize(format(currency, value));
  }

  public static @NonNull Component createInfo(@NonNull Currency c) {
    final Component infoDetails = Component.text()
      .append(Component.text("Id: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(c.identifier(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Singular: ", NamedTextColor.DARK_AQUA))
      .append(format(c, BigDecimal.ONE)).append(Component.newline())
      .append(Component.text("Plural: ", NamedTextColor.DARK_AQUA))
      .append(format(c, BigDecimal.valueOf(c.decimal() ? 6.66 : 666))).append(Component.newline())
      .append(Component.text("Primary: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(c.primary(), c.primary() ? NamedTextColor.GREEN : NamedTextColor.RED)).append(Component.newline())
      .append(Component.text("Decimal: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(c.decimal(), c.decimal() ? NamedTextColor.GREEN : NamedTextColor.RED)).build();
    return Component.text()
      .append(Component.text("> ", NamedTextColor.DARK_GRAY).append(c.singular()))
      .hoverEvent(HoverEvent.showText(infoDetails)).build();
  }
}
