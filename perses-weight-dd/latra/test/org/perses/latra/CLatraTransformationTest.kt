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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.perses.grammar.c.OrigCParserFacade
import org.perses.latra.c.AbstractCLatraTransformation
import org.perses.latra.c.FunctionInliningLatraTransformation
import org.perses.latra.c.VoidReturnFunctionDeclLatraTransformation
import org.perses.latra.c.VoidReturnFunctionDefLatraTransformation
import org.perses.spartree.SparTreeBuilder

class CLatraTransformationTest {
  private val inputString = "int main() { }"
  private val facade = OrigCParserFacade()
  private val parsingRelatedArguments = ParsingRelatedArguments.create(inputString, facade)
  private val parser = parsingRelatedArguments.parser
  private val sparTreeNodeFactory = parsingRelatedArguments.sparTreeNodeFactory

  class TestTransformation(
    parsingRelatedArguments: ParsingRelatedArguments<OrigCParserFacade>,
  ) : AbstractCLatraTransformation(
    "typedef <Specifier1:typeSpecifier> <idt:Identifier>;",
    "declaration",
    parsingRelatedArguments,
  ) {
    override fun createLatraRewriterBuilder(
      match: SparTreeMatch,
      sparAntlrTreeTriple: SparAntlrTreeTriple,
    ): AbstractLatraRewriterBuilder {
      return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
        override fun internalBuild() {
          deleteInTree(match.subtreeRoot)
        }
      }
    }
  }

  @Test
  fun testMatch() {
    val source = """
      typedef long int64_t;
      int64_t g;
      int main() {func_2(func_12(g_14));}
    """.trimIndent()

    val parseTreeWithParser = facade.parseString(source)

    val builder = SparTreeBuilder(
      sparTreeNodeFactory,
      parseTreeWithParser,
      simplifyTree = false,
    )

    val match = TransformationUtility.match(
      parseTreeWithParser.tree,
      parser.compileParseTreePattern(
        "int main () { <blockItem> }",
        parser.getRuleIndex("functionDefinition"),
      ),
      builder.sparAntlrBiMap,
    )

    assertThat(match.size).isEqualTo(1)
    assertThat(match.single().labelMap.containsKey("blockItem")).isTrue()
    assertThat(match.single().labelMap.size()).isEqualTo(1)
  }

  @Test
  fun testTransformation() {
    val source = """
      typedef long int64_t;
      int64_t g;
      int main() {func_2(func_12(g_14));}
    """.trimIndent()
    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple

    val transformation =
      TestTransformation(parsingRelatedArguments)
    val edits =
      transformation.rewriteAllMatches(sparAntlrTreeTriple)
    assertThat(edits.size).isEqualTo(1)
    val sparTree = sparAntlrTreeTriple.sparTree
    sparTree.applyEdit(edits.single())
    assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      "int64_t g ; int main ( ) { func_2 ( func_12 ( g_14 ) ) ; }",
    )
  }

  @Test
  fun testFunctionInlining1() {
    val source = """
    void func_1 ( int a , char b , int c ) { d = a ; }
    int main ( ) {
    func_1 ( g , "g" , g1 ) ;
    return 0 ; }
    """.trimIndent()

    val expected = """
      int main ( ) { d = g ; ; return 0 ; }
    """.trimIndent()

    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple
    val sparTree = sparAntlrTreeTriple.sparTree

    val transformation =
      FunctionInliningLatraTransformation(parsingRelatedArguments)
    val edits =
      transformation.rewriteAllMatches(
        sparAntlrTreeTriple,
      )
    assertThat(edits.size).isEqualTo(1)
    sparTree.applyEdit(edits.single())
    assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      expected,
    )
  }

  @Test
  fun testVoidFunctionDefReturn() {
    val source = """
    int func_1 ( int a , char b , int c ) { for ( p_131 . f0 = 0 ; p_131 . f0 << 2 ; p_131 . f0 += 1 ) { } d = a ; return a; }
    int main ( ) {
      func_1(1, '2', 3) ;
    return 0 ; }
    """.trimIndent()

    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple
    val sparTree = sparAntlrTreeTriple.sparTree

    val transformation =
      VoidReturnFunctionDefLatraTransformation(parsingRelatedArguments)

    val edits = transformation.rewriteAllMatches(
      sparAntlrTreeTriple,
    )
    assertThat(edits.size).isEqualTo(1)

    sparTree.applyEdit(edits.single())
    assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      """
      void func_1 ( int a , char b , int c ) { for ( p_131 . f0 = 0 ; p_131 . f0 << 2 ; p_131 . f0 += 1 ) { } d = a ; }
      int main ( ) { func_1 ( 1 , '2' , 3 ) ; return 0 ; }
      """.trimIndent().replace("\n", " "),
    )
  }

  @Test
  fun testVoidFunctionCallReturn() {
    val source = """
    int func_1 ( int a , char b , int c );
    int main ( ) {
    return 0 ; }
    """.trimIndent()

    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple

    val sparTree = sparAntlrTreeTriple.sparTree

    val transformation =
      VoidReturnFunctionDeclLatraTransformation(parsingRelatedArguments)

    val edits =
      transformation.rewriteAllMatches(
        sparAntlrTreeTriple,
      )
    assertThat(edits.size).isEqualTo(1)
    sparTree.applyEdit(edits.single())
    assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      "void func_1 ( int a , char b , int c ) ; int main ( ) { return 0 ; }",
    )
  }
}
