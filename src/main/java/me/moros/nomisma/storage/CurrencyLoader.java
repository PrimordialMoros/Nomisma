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
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import me.moros.nomisma.Nomisma;
import me.moros.nomisma.model.Currency;
import me.moros.nomisma.model.CurrencyData;
import me.moros.nomisma.model.ValidatedCurrency;
import me.moros.nomisma.registry.Registries;
import me.moros.nomisma.util.Tasker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CurrencyLoader {
  public static final String CURRENCY_SUFFIX = ".json";

  private final Path currencyDir;
  private final Gson gson;

  private CurrencyLoader(Path currencyDir) {
    this.currencyDir = currencyDir;
    gson = new GsonBuilder().setPrettyPrinting().create();
    if (!currencyFileExists("example")) {
      saveCurrency("example", ValidatedCurrency.EXAMPLE);
    }
  }

  public static @Nullable CurrencyLoader createInstance(@NonNull String parentDirectory) {
    try {
      Path currencyDir = Paths.get(parentDirectory, "Currencies");
      Files.createDirectories(currencyDir);
      return new CurrencyLoader(currencyDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public @NonNull CompletableFuture<Void> loadAllCurrencies() {
    return Tasker.async(() -> {
      try {
        Files.walk(currencyDir, 1).filter(this::isJson).forEach(this::loadCurrency);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private void loadCurrency(@NonNull Path path) {
    final long time = System.currentTimeMillis();
    try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
      Currency currency = ValidatedCurrency.validatedCopy(gson.fromJson(reader, CurrencyData.class));
      if (currency == null) {
        Nomisma.logger().warn("Invalid currency data: " + path);
        return;
      }
      if (Registries.CURRENCIES.register(currency)) {
        Nomisma.logger().info("Successfully loaded " + currency.identifier());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private @NonNull CompletableFuture<@NonNull Boolean> saveCurrency(@NonNull String name, @NonNull Currency currency) {
    return Tasker.async(() -> {
      Path path = Paths.get(currencyDir.toString(), name + CURRENCY_SUFFIX);
      try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
        gson.toJson(currency, writer);
        Nomisma.logger().info(currency.identifier() + " has been stored successfully.");
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

  public boolean currencyFileExists(@NonNull String name) {
    Path file = Paths.get(currencyDir.toString(), name + CURRENCY_SUFFIX);
    return Files.exists(file);
  }
}
