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

import com.google.common.collect.ImmutableSet
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProbabilityTableTest {

  @Test
  fun test() {
    val probabilityTable = ProbabilityTable<JavaType>(
      arrayListOf(JavaType.INT to 5, JavaType.LONG to 4),
    )
    val allKeys = ImmutableSet.of<JavaType>(JavaType.INT, JavaType.LONG)
    val intOnly = ImmutableSet.of<JavaType>(JavaType.INT)

    assertThat(allKeys).contains(
      probabilityTable.getRandom(),
    )
    assertThat(probabilityTable.getRandom(intOnly)).isEqualTo(JavaType.INT)

    probabilityTable.setValue(JavaType.LONG, 0)
    assertThat(probabilityTable.getRandom()).isEqualTo(JavaType.INT)
  }
}
