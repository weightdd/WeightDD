/*
 * Copyright (C) 2018-2024 University of Waterloo.
 *
 * This file is part of Perses.
 *
 * Perses is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * Perses is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Perses; see the file LICENSE.  If not see <http://www.gnu.org/licenses/>.
 */
package org.pluverse.jvm.fuzz.util

enum class JavaType(val stringValue: String, val weight: Int) {
  ARRAY("array", 0),
  VARIABLE("var", 0), // Temporary java type to signify a variable is required not literal
  OBJECT("object", 0),
  BOOLEAN("boolean", 0),
  STRING("String", 0),
  BYTE("byte", 1),
  CHAR("char", 2),
  SHORT("short", 3),
  INT("int", 4),
  LONG("long", 5),
  FLOAT("float", 6),
  DOUBLE("double", 7),
  VOID("void", -1),
  EXCEPTION("Exception", -1), // Used to avoid same exception name in nested try catch
}
