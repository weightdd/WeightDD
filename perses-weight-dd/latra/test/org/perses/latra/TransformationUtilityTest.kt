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

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.perses.TestUtility
import org.perses.grammar.AbstractParserFacade
import org.perses.grammar.c.CParserFacade
import org.perses.grammar.c.LanguageC
import org.perses.grammar.smtlibv2.LanguageSmtLibV2
import org.perses.grammar.smtlibv2.SmtLibV2ParserFacade
import org.perses.program.TokenizedProgramFactory
import org.perses.spartree.AbstractSparTreeNode
import org.perses.spartree.LexerRuleSparTreeNode
import org.perses.spartree.SparTreeBuilder
import org.perses.spartree.SparTreeNodeFactory

class TransformationUtilityTest {
  private val smtLibV2ParserFacade = SmtLibV2ParserFacade()
  private val smtLibV2ParseTree =
    smtLibV2ParserFacade.parseString("( assert  ( let ((a x)) (and a b) ) )")
  private val smtLibV2Parser = smtLibV2ParseTree.parser
  private val smtLibV2Lexer = smtLibV2ParseTree.lexer

  private val pattern =
    smtLibV2Parser.compileParseTreePattern("( + a b v )", smtLibV2Parser.getRuleIndex("term"))
  private val factory = TokenizedProgramFactory.createFactory(
    AbstractParserFacade.getTokens(pattern.patternTree),
    smtLibV2ParserFacade.language,
  )
  private val smtLibV2Hierarchy = smtLibV2ParserFacade.ruleHierarchy
  private val smtLibV2SparTreeNodeFactory = SparTreeNodeFactory(
    smtLibV2ParserFacade.metaTokenInfoDb,
    factory,
    smtLibV2Hierarchy,
  )
  private val subroot = TransformationUtility.createNodeFromString(
    "( + a b v )",
    "term",
    ParsingRelatedArguments(
      smtLibV2Lexer,
      smtLibV2Parser,
      smtLibV2ParserFacade,
      smtLibV2SparTreeNodeFactory,
    ),
  )

  private val cParserFacade = CParserFacade()
  private val cParseTree = cParserFacade.parseString("int main() { }")
  private val cParser = cParseTree.parser

  private val cFactory = TokenizedProgramFactory.createFactory(
    AbstractParserFacade.getTokens(cParseTree.tree),
    cParserFacade.language,
  )
  private val cSparTreeNodeFactory = SparTreeNodeFactory(
    cParserFacade.metaTokenInfoDb,
    cFactory,
    cParserFacade.ruleHierarchy,
  )

  @Test
  fun testCreateNodeFromString() {
    val text = TransformationUtility.createStringFromNode(subroot)
    assertThat(text.trim()).isEqualTo("( + a b v )")
  }

  @Test
  fun testCreateStringFromNode() {
    val tree = TestUtility.createSparTreeFromString("( assert ( + a b v ) )", LanguageSmtLibV2)
    val text = TransformationUtility.createStringFromNode(tree.root)
    assertThat(text.trim()).isEqualTo("( assert ( + a b v ) )")
  }

  @Test
  fun testCreateStringFromNodes() {
    val nodeList = arrayListOf(subroot, subroot)
    val listText = TransformationUtility.createStringFromNodes(nodeList)
    assertThat(listText.trim()).isEqualTo("( + a b v ) ( + a b v )")
  }

  @Test
  fun testCheckTypeInSESEPath() {
    val nodeAList = TransformationUtility.findLexerRuleNodesInSubtree(subroot, "a")
    assertThat(nodeAList.size).isEqualTo(1)
    assertThat(
      TransformationUtility.checkTypeInSESEPath(
        nodeAList[0],
        smtLibV2Hierarchy.getRuleHierarchyEntryWithNameOrThrow("term"),
      ),
    ).isTrue()
  }

  @Test
  fun testFindLexerRuleNodesInSubtree() {
    val nodeALocation = TransformationUtility.findLexerRuleNodesInSubtree(subroot, "a")
    assertThat(nodeALocation.size).isEqualTo(1)
  }

  @Test
  fun testFindInSubtreeType() {
    val termLocations = TransformationUtility.findInSubtree(
      subroot,
      smtLibV2Hierarchy.getRuleHierarchyEntryWithNameOrThrow("term"),
    )
    assertThat(termLocations.size).isEqualTo(3)
  }

  private fun testDeriveArgument(
    root: AbstractSparTreeNode,
    parserFacade: AbstractParserFacade,
  ): ImmutableList<AbstractSparTreeNode> {
    val rule1 = parserFacade.ruleHierarchy.getRuleHierarchyEntryOrNull("Identifier")

    check(rule1 != null)
    return ImmutableList.builder<AbstractSparTreeNode>().addAll(
      TransformationUtility.findInSubtree(
        root,
        rule1,
      ),
    ).build()
  }

  @Test
  fun testBuildFunctionCallTable() {
    val a = """
      typedef long int64_t;
      int64_t g_71;
      int main() { func_12(g_14, g_15);}
    """.trimIndent()

    val parseTreeWithParser = TestUtility.parseString(a, LanguageC)

    val builder = SparTreeBuilder(
      cSparTreeNodeFactory,
      parseTreeWithParser,
      simplifyTree = false,
    )
    val functionPattern =
      " <name:Identifier> ( <argumentList:optional__argumentExpressionList> ) "
    val functionPatternRule = "expression"

    val functionCalls = TransformationUtility.buildFunctionCallTable(
      cParser,
      cParserFacade,
      functionPattern,
      functionPatternRule,
      parseTreeWithParser.tree,
      builder.sparAntlrBiMap,
      ::testDeriveArgument,
    )
    assertThat(functionCalls.size).isEqualTo(1)

    assertThat(functionCalls["func_12"]!!.location.ruleName).isEqualTo("expression")
    assertThat(functionCalls["func_12"]!!.parameters.size).isEqualTo(2)
    assertThat(
      (functionCalls["func_12"]!!.parameters[0] as LexerRuleSparTreeNode).token.text,
    ).isEqualTo(
      "g_14",
    )
    assertThat(
      (functionCalls["func_12"]!!.parameters[1] as LexerRuleSparTreeNode).token.text,
    ).isEqualTo(
      "g_15",
    )
  }

  @Test
  fun testFindPatternInSubtree() {
    val source = """
    int main ( ) {
      func_1 ( g , "g" , g1 ) ;
      return 0 ; }
    """.trimIndent()

    val parseTreeWithParser = TestUtility.parseString(source, LanguageC)
    val builder = SparTreeBuilder(
      cSparTreeNodeFactory,
      parseTreeWithParser,
      simplifyTree = false,
    )
    val sparTree = builder.result
    val subtree = sparTree.root.getChild(0)
    val match = TransformationUtility.findPatternInSubtree(
      " <Identifier> ( <optional__argumentExpressionList> ) ",
      "expression",
      subtree,
      cParser,
      builder.sparAntlrBiMap,
    )
    assertThat(match.size).isEqualTo(1)
    assertThat(match.single().labelMap.containsKey("Identifier")).isTrue()
    assertThat(match.single().labelMap.containsKey("optional__argumentExpressionList")).isTrue()
  }

  @Test
  fun testgetCorrespondingNode() {
    val source = """
    int main ( ) {
      func_1 ( g , "g" , g1 ) ;
      return 0 ; }
    """.trimIndent()
    val parseTreeWithParser = TestUtility.parseString(source, LanguageC)
    val builder = SparTreeBuilder(
      cSparTreeNodeFactory,
      parseTreeWithParser,
      simplifyTree = false,
    )
    val root = builder.result.root
    val origNode = root.getChild(0)
    val correspondingNode = TransformationUtility.getCorrespondingNode(root, origNode)
    assertThat(correspondingNode.nodeId).isEqualTo(origNode.nodeId)
    assertThat(TransformationUtility.createStringFromNode(correspondingNode)).isEqualTo(
      TransformationUtility.createStringFromNode(origNode),
    )
  }

  @Test
  fun testMatchByType() {
    val termLocations = TransformationUtility.matchByType(
      subroot,
      smtLibV2Hierarchy.getRuleHierarchyEntryWithNameOrThrow("term"),
    )
    assertThat(termLocations.size).isEqualTo(3)
    assertThat(termLocations[0].labelMap["term"].size).isEqualTo(1)
    assertThat(
      (termLocations[0].labelMap["term"][0] as LexerRuleSparTreeNode).token.text,
    ).isEqualTo(
      "a",
    )
    assertThat(termLocations[1].labelMap["term"].size).isEqualTo(1)
    assertThat(
      (termLocations[1].labelMap["term"][0] as LexerRuleSparTreeNode).token.text,
    ).isEqualTo(
      "b",
    )
    assertThat(termLocations[2].labelMap["term"].size).isEqualTo(1)
    assertThat(
      (termLocations[2].labelMap["term"][0] as LexerRuleSparTreeNode).token.text,
    ).isEqualTo(
      "v",
    )
  }
}
