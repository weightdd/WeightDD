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
package org.perses.latra.reduction.reducer

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.TestUtility
import org.perses.grammar.c.LanguageC
import org.perses.program.printer.SingleTokenPerLinePrinter

@RunWith(JUnit4::class)
class LatraReducerTest {
  val source1 = """
#include <stdio.h>

int main() {
    typedef int int64_t;
    int64_t b = 42;
    if (b == 42){
        printf("hello");
    }
    return -1;
  }
  """.trimIndent()

  val source2 = """
      typedef long int64_t;
  """.trimIndent()

  val sparTree = TestUtility.createSparTreeFromString(source1, LanguageC, true)
  val newSparTree = TestUtility.createSparTreeFromString(source2, LanguageC, false)

  @Test
  fun testRebuildSparTree() {
    LatraReducer.replaceSparTreeWithNewTree(sparTree, newSparTree)
    assertThat(sparTree.root.childCount).isEqualTo(1)
    assertThat(SingleTokenPerLinePrinter.print(sparTree.programSnapshot).sourceCode).isEqualTo(
      """
    typedef
    long
    int64_t
    ;
    
      """.trimIndent(),
    )
  }
}
