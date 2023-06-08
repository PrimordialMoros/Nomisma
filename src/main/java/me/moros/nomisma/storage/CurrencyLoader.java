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

package me.moros.nomisma.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.CurrencyData;
import me.moros.nomisma.model.ValidatedCurrency;
import me.moros.nomisma.registry.Registries;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CurrencyLoader {
  public static final String CURRENCY_SUFFIX = ".json";

  private final Nomisma parent;
  private final Path currencyDir;
  private final Gson gson;

  private CurrencyLoader(Nomisma parent, Path currencyDir) {
    this.parent = parent;
    this.currencyDir = currencyDir;
    gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    if (!currencyFileExists("example")) {
      saveCurrency("example", ValidatedCurrency.EXAMPLE);
    }
  }

  public static @Nullable CurrencyLoader createInstance(Nomisma parent, Path parentDirectory) {
    try {
      Path currencyDir = parentDirectory.resolve("Currencies");
      Files.createDirectories(currencyDir);
      return new CurrencyLoader(parent, currencyDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public CompletableFuture<?> loadAllCurrencies() {
    return parent.executor().async().submit(() -> {
      try (Stream<Path> stream = Files.walk(currencyDir, 1)) {
        Collection<Currency> currencies = stream.filter(this::isJson).map(this::loadCurrency)
          .filter(Objects::nonNull).toList();
        Registries.CURRENCIES.registerAndLock(currencies);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private @Nullable Currency loadCurrency(Path path) {
    try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
      Currency currency = ValidatedCurrency.validatedCopy(gson.fromJson(reader, CurrencyData.class));
      if (currency == null) {
        parent.logger().warn("Invalid currency data: " + path);
      } else {
        return currency;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private CompletableFuture<Boolean> saveCurrency(String name, Currency currency) {
    return parent.executor().async().submit(() -> {
      Path path = Paths.get(currencyDir.toString(), name + CURRENCY_SUFFIX);
      try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
        gson.toJson(new CurrencyData(currency), writer);
        parent.logger().info(currency.identifier() + " has been stored successfully.");
        return true;
      } catch (IOException ignore) {
      }
      return false;
    }).exceptionally(e -> {
      e.printStackTrace();
      return false;
    });
  }

  private boolean isJson(Path path) {
    return path.getFileName().toString().endsWith(CURRENCY_SUFFIX);
  }

  public boolean currencyFileExists(String name) {
    Path file = Paths.get(currencyDir.toString(), name + CURRENCY_SUFFIX);
    return Files.exists(file);
  }
}
