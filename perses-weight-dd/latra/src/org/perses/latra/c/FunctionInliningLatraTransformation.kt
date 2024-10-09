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
package org.perses.latra.c

import com.google.common.collect.ImmutableList
import org.perses.grammar.AbstractParserFacade
import org.perses.grammar.c.OrigCParserFacade
import org.perses.latra.AbstractLatraRewriterBuilder
import org.perses.latra.ParsingRelatedArguments
import org.perses.latra.SparAntlrTreeTriple
import org.perses.latra.SparTreeMatch
import org.perses.latra.TransformationUtility
import org.perses.spartree.AbstractSparTreeNode
import org.perses.spartree.AbstractTreeNode.NodeIdCopyStrategy.ReuseNodeIdStrategy
import org.perses.spartree.LexerRuleSparTreeNode

class FunctionInliningLatraTransformation(
  parsingRelatedArguments: ParsingRelatedArguments<OrigCParserFacade>,
) : AbstractCLatraTransformation(
  pattern =
  """
    <declarationSpecifier> <sym:Identifier> 
      ( <paramList:parameterList>) { <block:blockItemList> }
  """.trimIndent(),
  patternRoot = "functionDefinition",
  parsingRelatedArguments,
) {

  // TODO: https://github.com/chengniansun/perses-private/issues/652
  private fun deriveArgument(
    root: AbstractSparTreeNode,
    parserFacade: AbstractParserFacade,
  ): ImmutableList<AbstractSparTreeNode> {
    val rule1 = parserFacade.ruleHierarchy.getRuleHierarchyEntryOrNull("Identifier")
    val rule2 = parserFacade.ruleHierarchy.getRuleHierarchyEntryOrNull("StringLiteral")

    check(rule1 != null && rule2 != null)
    return ImmutableList.builder<AbstractSparTreeNode>().addAll(
      TransformationUtility.findInSubtree(
        root,
        rule1,
      ),
    )
      .addAll(TransformationUtility.findInSubtree(root, rule2)).build()
  }

  override fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder {
    return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
      override fun internalBuild() {
        val parser = parsingRelatedArguments.parser

        val functionCallTable = TransformationUtility.buildFunctionCallTable(
          parser,
          parserFacade,
          functionPattern =
          " <name:Identifier> ( <argumentList:argumentExpressionList> ) ",
          functionPatternRule = "expression",
          sparAntlrTreeTriple.antlrTree,
          sparAntlrTreeTriple.sparAntlrNodeMapping,
          ::deriveArgument,
        )
        val sym = (match.labelMap["sym"].single() as LexerRuleSparTreeNode).token.text
        val locations = functionCallTable[sym]?.location
        locations ?: return
        val callerParameters = functionCallTable[sym]?.parameters
        val parameters = deriveArgument(
          match.labelMap["paramList"].single(),
          parserFacade,
        )
        if (callerParameters != null && callerParameters.size == parameters.size) {
          val copyTreeRoot = match.labelMap["block"].single().recursiveDeepCopy(ReuseNodeIdStrategy)

          for (i in callerParameters.indices) {
            val locs = TransformationUtility.findLexerRuleNodesInSubtree(
              copyTreeRoot,
              (parameters[i] as LexerRuleSparTreeNode).token.text,
            )
            substituteInTreeDirectly(locs, callerParameters[i])
          }
          substituteInTree(locations, copyTreeRoot)
          deleteInTree(match.subtreeRoot)
        }
      }
    }
  }
}
