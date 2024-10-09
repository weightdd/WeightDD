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
package org.perses.reduction.reducer

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.TestUtility
import org.perses.antlr.TokenType
import org.perses.antlr.ast.PersesTerminalAst
import org.perses.antlr.toTokenType
import org.perses.grammar.AbstractParserFacade
import org.perses.grammar.c.CParserFacade
import org.perses.grammar.c.OptCLexer
import org.perses.program.TokenizedProgramFactory
import org.perses.spartree.LexerRuleSparTreeNode
import org.perses.spartree.ParserRuleSparTreeNode
import org.perses.spartree.SparTreeNodeFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively

@RunWith(JUnit4::class)
class MinimalSparTreeGeneratorTest {

  val tempDir: Path = Files.createTempDirectory(this::javaClass.name)

  val START = "start"

  @Test
  fun testKleeneStarNode() {
    val generator = createGenerator(
      """
      $START: ';' *;
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).isEmpty()
    assertThat(result.acceptEpsilon).isTrue()
  }

  @Test
  fun testOptionalWithSemi() {
    val generator = createGenerator(
      """
      $START: ';'? ';';
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(1)
    val root = result.nonEmptyNodes.single()
    assertThat(root.asParserRule().ruleName).isEqualTo(START)
    assertThat(root.childCount).isEqualTo(1)
    val child = root.getChild(0).asLexerRule()
    assertThat(child.token.text).isEqualTo(";")
    assertThat(result.acceptEpsilon).isFalse()
  }

  @Test
  fun testOptionalNode() {
    val generator = createGenerator(
      """
      $START: ':' ?;
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).isEmpty()
    assertThat(result.acceptEpsilon).isTrue()
  }

  @Test
  fun testKleenePlusNode() {
    val generator = createGenerator(
      """
      $START: ':'+;
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(1)
    val node = result.nonEmptyNodes.single()
    assertThat(node.isKleenePlusRuleNode).isTrue()
  }

  @Test
  fun testRuleRefNode() {
    val generator = createGenerator(
      """
        $START : Semi;
        Semi: ';';
        """,
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(1)
    val node = result.nonEmptyNodes.single()
    assertThat(node.ruleName).isEqualTo(START)
    assertThat(node.childCount).isEqualTo(1)
    val child = node.getChild(0)
    assertThat(child.ruleName).isEqualTo("Semi")
    assertThat(child.asLexerRule().token.text).isEqualTo(";")
  }

  @Test
  fun testRecursiveRuleNode() {
    val generator = createGenerator(
      """
      $START: $START Semi
            | Semi;
      Semi: ';';      
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(2)
    val longCandidate = result.nonEmptyNodes.single { it.childCount == 2 }
    assertThat(longCandidate.ruleName).isEqualTo(START)
    longCandidate.getChild(0).let {
      assertThat(it.asParserRule().ruleName).isEqualTo(START)
      assertThat(it.childCount).isEqualTo(1)
      assertThat(it.getChild(0).asLexerRule().token.text).isEqualTo(";")
    }
    assertThat(longCandidate.getChild(1).asLexerRule().token.text).isEqualTo(";")
    val shortCandidate = result.nonEmptyNodes.single { it.childCount == 1 }
    assertThat(shortCandidate.getChild(0).asLexerRule().token.text).isEqualTo(";")
  }

  @Test
  fun testAltAst() {
    val generator = createGenerator(
      """
      $START: Semi | Colon;
      Semi: ';';
      Colon: ':';
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(2)
  }

  @Test
  fun testSequenceAst() {
    val generator = createGenerator(
      """
      $START: Semi Semi;
      Semi: ';';
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(1)
    val node = result.nonEmptyNodes.single() as ParserRuleSparTreeNode
    assertThat(node.ruleName).isEqualTo(START)
    assertThat(node.childCount).isEqualTo(2)
    node.getChild(0).let {
      val first = it as LexerRuleSparTreeNode
      assertThat(first.ruleName).isEqualTo("Semi")
      assertThat(first.token.text).isEqualTo(";")
    }
    node.getChild(1).let {
      val second = it as LexerRuleSparTreeNode
      assertThat(second.ruleName).isEqualTo("Semi")
      assertThat(second.token.text).isEqualTo(";")
    }
  }

  @Test
  fun testParserRuleWithStringLiteral() {
    val generator = createGenerator(
      """
      $START: ';';
      """.trimIndent(),
    )
    val result = generator.generateForRule(START)
    assertThat(result.nonEmptyNodes).hasSize(1)
    val node = result.nonEmptyNodes.single()
    assertThat(node.ruleName).isEqualTo(START)
    assertThat(node.childCount).isEqualTo(1)
    val child = node.getChild(0)
    assertThat(child.asLexerRule().token.text).isEqualTo(";")
  }

  private fun createGenerator(combinedGrammarContent: String): MinimalSparTreeGenerator {
    val facade = createAdhocFacade(
      combinedGrammarContent = combinedGrammarContent,
    )
    return createGenerator(facade)
  }

  private fun createGenerator(facade: AbstractParserFacade): MinimalSparTreeGenerator {
    val sparTreeNodeFactory = SparTreeNodeFactory(
      facade.metaTokenInfoDb,
      TokenizedProgramFactory.createEmptyFactory(facade.language),
      facade.ruleHierarchy,
    )
    return MinimalSparTreeGenerator(facade, sparTreeNodeFactory)
  }

  @Test
  fun testGenerateTerminalNode() {
    val generator = createGenerator(CParserFacade())
    internalTestTerminalNode(generator, "Auto", "auto", OptCLexer.Auto.toTokenType())
    internalTestTerminalNode(generator, "Semi", ";", OptCLexer.Semi.toTokenType())
  }

  @Test
  fun testStructOrUnion() {
    val generator = createGenerator(CParserFacade())
    val result = generator.generateForRule("structOrUnion")
    assertThat(result.acceptEpsilon).isFalse()
    assertThat(result.nonEmptyNodes).hasSize(2)
    val tokens = result.nonEmptyNodes.asSequence()
      .map { it.leafNodeSequence().single().token.text }
      .toList()
    assertThat(tokens).containsExactly("struct", "union")
  }

  @Test
  fun testStatement() {
    val generator = createGenerator(CParserFacade())
    val result = generator.generateForRule("expressionStatement")
    assertThat(result.nonEmptyNodes).hasSize(1)
    assertThat(result.acceptEpsilon).isFalse()
    val root = result.nonEmptyNodes.single()
    assertThat(root.ruleName).isEqualTo("expressionStatement")
    assertThat(root.childCount).isEqualTo(1)
    val child = root.getChild(0)
    assertThat(child.asLexerRule().token.text).isEqualTo(";")
  }

  private fun internalTestTerminalNode(
    generator: MinimalSparTreeGenerator,
    tokenName: String,
    expectedLexeme: String,
    expectedTokenType: TokenType,
  ) {
    val list = generator.generateForLexerTerminalAst(
      generator.grammarHierarchy
        .getRuleHierarchyEntryWithNameOrThrow(tokenName).ruleDef.body as PersesTerminalAst,
    )
    assertThat(list).hasSize(1)
    val value = list.single().value as LexerRuleSparTreeNode
    assertThat(value.token.text).isEqualTo(expectedLexeme)
    assertThat(value.token.type).isEqualTo(expectedTokenType)
  }

  fun createAdhocFacade(
    combinedGrammarContent: String,
  ): AbstractParserFacade {
    return TestUtility.generateAdhocFacade(
      "MSTGrammar",
      combinedGrammarContent,
      startRule = "start",
      tokenNamesOfIdentifiers = ImmutableList.of(),
      workingDir = tempDir,
      enablePnfNormalization = false,
    )
  }

  @After fun teardown() {
    tempDir.deleteRecursively()
  }
}
