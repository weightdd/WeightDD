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
package org.pluverse.chaoty.ast

import org.perses.util.plus
import org.perses.util.toImmutableList
import org.pigen.python.ast.toIdentifier

class TaichiPrimitiveType(
  val category: TypeCategory,
  val numOfBits: PrecisionBits,
) {

  val taichiTypeName = (category.prefix + numOfBits.value).toIdentifier()

  enum class TypeCategory(val prefix: String) {
    INT("i"),
    UNSIGNED("u"),
    FLOAT("f"),
  }

  enum class PrecisionBits(val value: Int) {
    B_8(8),
    B_16(16),
    B_32(32),
    B_64(64),
  }

  companion object {
    val INT_TYPES = PrecisionBits.values().asSequence()
      .map { TaichiPrimitiveType(TypeCategory.INT, it) }
      .toImmutableList()

    val UNSIGNED_TYPES = PrecisionBits.values().asSequence()
      .map { TaichiPrimitiveType(TypeCategory.UNSIGNED, it) }
      .toImmutableList()

    val FLOAT_TYPES = PrecisionBits.values().asSequence()
      .map { TaichiPrimitiveType(TypeCategory.FLOAT, it) }
      .toImmutableList()

    val ALL_TYPES = INT_TYPES + UNSIGNED_TYPES + FLOAT_TYPES
  }
}
