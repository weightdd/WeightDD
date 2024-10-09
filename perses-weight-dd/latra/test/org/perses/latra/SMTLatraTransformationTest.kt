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
import org.perses.grammar.smtlibv2.OrigSmtLibV2ParserFacade
import org.perses.latra.smt.AbstractSMTLatraTransformation
import org.perses.latra.smt.AnnotationRemovalTransformation
import org.perses.latra.smt.BvConverstionTransformation
import org.perses.latra.smt.CheckSatAssuminTransformation
import org.perses.latra.smt.DoubleNegElimTransformation
import org.perses.latra.smt.DoubleNotElimTransformation
import org.perses.latra.smt.EvalFalseTransformation
import org.perses.latra.smt.ExistsRemovalTransformation
import org.perses.latra.smt.ForallRemovalTransformation
import org.perses.latra.smt.LetSubstitutionTransformation
import org.perses.latra.smt.MergeAndTransformation
import org.perses.latra.smt.MergePlusTransformation
import org.perses.latra.smt.MergeVariableTransformation
import org.perses.latra.smt.SubstituteWithConstTransformation

class SMTLatraTransformationTest {

  private val inputString = "( assert  ( let ((a x)) (and a b) ) )"
  private val facade = OrigSmtLibV2ParserFacade()
  private val parsingRelatedArguments = ParsingRelatedArguments.create(inputString, facade)
  private val sparTreeNodeFactory = parsingRelatedArguments.sparTreeNodeFactory

  private fun testTransformation(
    testingTransformation: AbstractSMTLatraTransformation,
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
  fun testLetSubstitutionTransformation() {
    val source = """
      ( assert ( let ( ( a  b ) ) (  =  a c ) ) )
    """.trimIndent()
    val expectOutput = "( assert ( = b c ) )"
    testTransformation(
      LetSubstitutionTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testAnnotationRemoval() {
    val source = """
      ( assert (! true :named b))
    """.trimIndent()

    val expectOutput = "( assert true )"

    testTransformation(
      AnnotationRemovalTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testMergePlusTransformation() {
    val source = """
      ( assert (  +  a ( + b c) ))
    """.trimIndent()

    val expectOutput = "( assert ( + a b c ) )"

    testTransformation(
      MergePlusTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testMergeAndTransformation() {
    val source = """
      ( assert (  and  a ( and b c) ))
    """.trimIndent()

    val expectOutput = "( assert ( and a b c ) )"

    testTransformation(
      MergeAndTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testForallRemovalTransformation() {
    val source = """
      (get-qe (forall ((a Int))(> 665 (div f1 850) )))
    """.trimIndent()

    val expectOutput = "( get-qe ( > 665 ( div f1 850 ) ) )"

    testTransformation(
      ForallRemovalTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testExistRemovalTransformation() {
    val source = """
      (assert ( exists ((a (_ BitVec 6))) true))
    """.trimIndent()

    val expectOutput = "( assert true )"

    testTransformation(
      ExistsRemovalTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testDoubleNotTransformation() {
    val source = """
      (assert ( bvnot ( bvnot ( exists ((a (_ BitVec 6))) true) )))
    """.trimIndent()

    val expectOutput = "( assert ( exists ( ( a ( _ BitVec 6 ) ) ) true ) )"

    testTransformation(
      DoubleNotElimTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testDoubleNegTransformation() {
    val source = """
      (assert ( bvneg ( bvneg ( exists ((a (_ BitVec 6))) true) )))
    """.trimIndent()

    val expectOutput = "( assert ( exists ( ( a ( _ BitVec 6 ) ) ) true ) )"

    testTransformation(
      DoubleNegElimTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testBvConverstionTransformation() {
    val source = """
      (assert (  bvnot  (  _  bv0  123  )  ) )
    """.trimIndent()

    val expectOutput = "( assert ( _ bv1 123 ) )"

    testTransformation(
      BvConverstionTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testEvalFalse() {
    val source = """
      ( assert ( = false ( _ bv1 123 )))
    """.trimIndent()

    val expectOutput = "( assert ( not ( _ bv1 123 ) ) )"

    testTransformation(
      EvalFalseTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  @Test
  fun testCheckSatAsummin() {
    val source = """
      ( check-sat-assuming ( x ))
    """.trimIndent()

    val expectOutput = "( check-sat )"

    testTransformation(
      CheckSatAssuminTransformation(parsingRelatedArguments),
      source,
      expectOutput,
    )
  }

  /**
   * In this test, two matches will be created but only 1 is expected to be applied to sparTree
   * Either a is changed to b or b is changed to a.
   */
  @Test
  fun testMergeVariable() {
    val source = """
      (declare-fun a () Int)
      (declare-fun b () Int)
      (assert (= a 10))
      (assert (= b 5))
      (assert (= (add a b) 15))
      (assert (= (subtract a b) 5))
      (check-sat)
    """.trimIndent()

    val expectOutput = """
      ( declare-fun b ( ) Int )
      ( assert ( = b 10 ) )
      ( assert ( = b 5 ) )
      ( assert ( = ( add b b ) 15 ) )
      ( assert ( = ( subtract b b ) 5 ) )
      ( check-sat )
    """.trimIndent()

    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple
    val sparTree = sparAntlrTreeTriple.sparTree

    val edits =
      MergeVariableTransformation(parsingRelatedArguments).rewriteAllMatches(
        sparAntlrTreeTriple,
      )
    Truth.assertThat(edits.size).isEqualTo(2)
    sparTree.applyEdit(edits[0])
    Truth.assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      expectOutput.replace("\n", " "),
    )
  }

  /**
   * In this test, multiple matches will be created, but they might not be able to be applied
   * together. It's Ok to generate invalid edits.
   */
  @Test
  fun testSubstituteWithConst() {
    val source = """
      ( assert ( = false ( _ bv1 123 )))
    """.trimIndent()

    val expectOutput = """
      ( assert ( _ bv0 1 ) )
    """.trimIndent()

    val sparAntlrTreeTriple = SparAntlrTreeTriple.create(source, facade, sparTreeNodeFactory).triple
    val sparTree = sparAntlrTreeTriple.sparTree

    val edits =
      SubstituteWithConstTransformation(
        parsingRelatedArguments,
      ).rewriteAllMatches(
        sparAntlrTreeTriple,
      )

    Truth.assertThat(edits.size).isEqualTo(3)
    sparTree.applyEdit(edits[0])

    Truth.assertThat(
      sparTree.programSnapshot.tokens.joinToString(" ") {
        it.text
      },
    ).isEqualTo(
      expectOutput.replace("\n", " "),
    )
  }
}
