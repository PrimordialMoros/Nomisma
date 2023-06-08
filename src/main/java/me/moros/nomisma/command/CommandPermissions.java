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

package me.moros.nomisma.command;

import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.Permission;

public final class CommandPermissions {
  private CommandPermissions() {
  }

  public static final CommandPermission HELP = create("help");
  public static final CommandPermission LIST = create("list");
  public static final CommandPermission VERSION = create("version");
  public static final CommandPermission RELOAD = create("reload");
  public static final CommandPermission MIGRATE = create("migrate");
  public static final CommandPermission IMPORT = create("import");

  private static Permission create(String node) {
    return Permission.of("nomisma.command." + node);
  }
}
