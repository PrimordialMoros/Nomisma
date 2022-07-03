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

package me.moros.nomisma.storage.sql;

import java.util.Collection;

import me.moros.nomisma.model.Currency;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum SqlQueries {
  PLAYER_INSERT("INSERT INTO nomisma_players (player_uuid, player_name) VALUES(?, ?)"),
  PLAYER_SELECT_BY_UUID("SELECT * FROM nomisma_players WHERE player_uuid=? LIMIT 1"),
  PLAYER_SELECT_BY_NAME("SELECT * FROM nomisma_players WHERE player_name=? LIMIT 1"),
  PLAYER_SELECT_ALL("SELECT * FROM nomisma_players");

  private final String query;

  SqlQueries(String query) {
    this.query = query;
  }

  /**
   * @return The SQL query for this enumeration.
   */
  public @NonNull String query() {
    return query;
  }

  public static @NonNull String selectTop(@NonNull Currency currency, int offset, int limit) {
    StringBuilder sb = new StringBuilder("SELECT player_name, ");
    sb.append(currency.identifier()).append(" AS balance FROM nomisma_players ORDER BY balance DESC LIMIT ");
    if (offset > 0) {
      sb.append(offset).append(", ");
    }
    sb.append(Math.max(1, Math.min(limit, 100)));
    return sb.toString();
  }

  public static @NonNull String updateProfile(@NonNull Collection<@NonNull String> currencies) {
    StringBuilder sb = new StringBuilder("UPDATE nomisma_players SET player_name = :player_name");
    for (String id : currencies) {
      sb.append(", ").append(id).append(" = :").append(id);
    }
    sb.append(" WHERE player_uuid = :player_uuid");
    return sb.toString();
  }

  public static @NonNull String addColumn(@NonNull Currency currency) {
    return "ALTER TABLE nomisma_players ADD " + currency.identifier() + " DECIMAL(12,2) NOT NULL DEFAULT 0";
  }
}
