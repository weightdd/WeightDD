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
package org.perses.latra

import com.google.common.truth.Truth
import org.junit.Test
import org.perses.grammar.rust.OrigRustParserFacade
import org.perses.latra.rust.AbstractRustLatraTransformation
import org.perses.latra.rust.ClearBlockTransformation
import org.perses.latra.rust.ClearStructTransformation
import org.perses.latra.rust.NoneDefaultingTransformation

class RustLatraTransformationTest {

  private val inputString = "fn main() { }"
  private val facade = OrigRustParserFacade()
  private val parsingRelatedArguments = ParsingRelatedArguments.create(inputString, facade)
  private val sparTreeNodeFactory = parsingRelatedArguments.sparTreeNodeFactory

  private fun testTransformation(
    testingTransformation: AbstractRustLatraTransformation,
    source: String,
    expectOutput: String,
  ) {
    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple
    val sparTree = sparAntlrTreeTriple.sparTree

    val edits =
      testingTransformation.rewriteAllMatches(
        sparAntlrTreeTriple,
      )
    Truth.assertThat(edits.size).isEqualTo(1)
    sparTree.applyEdit(edits.single())
    Truth.assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      expectOutput,
    )
  }

  @Test
  fun testClearStruct() {
    val source = """
      struct Person {
      name: String,
      age: u32,
     }
    """.trimIndent()
    val expectOutput = """
      struct Person ;
    """.trimIndent()
    testTransformation(
      ClearStructTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testClearBlock() {
    val source = """
      fn g (f: impl c<e, j>) -> impl c<e, e::b> { f }
    """.trimIndent()
    val expectOutput = """
      fn g ( f : impl c < e , j > ) -> impl c < e , e :: b > { unimplemented ! ( ) }
    """.trimIndent()
    testTransformation(
      ClearBlockTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testNoneDefaulting() {
    val source = """
      fn main (){
        let x = (20 * 5) + (8 / 2) - (4 % 3);
      }
    """.trimIndent()
    val expectOutput = """
      fn main ( ) { let x = None . unwrap ( ) ; }
    """.trimIndent()
    testTransformation(
      NoneDefaultingTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }
}
