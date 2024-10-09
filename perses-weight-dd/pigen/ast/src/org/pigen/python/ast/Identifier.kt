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
package org.pigen.python.ast

@JvmInline
value class Identifier(val value: String) {

  init {
    require(isIdentifier(value))
  }

  companion object {
    fun isIdentifier(string: String): Boolean {
      if (string.isEmpty()) {
        return false
      }
      if (!string[0].isJavaIdentifierStart()) {
        return false
      }
      for (i in 1 until string.length) {
        if (!string[i].isJavaIdentifierPart()) {
          return false
        }
      }
      return true
    }
  }
}

fun CharSequence.toIdentifier() = Identifier(this.toString())
