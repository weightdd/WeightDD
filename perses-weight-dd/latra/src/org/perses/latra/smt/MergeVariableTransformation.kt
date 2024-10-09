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
package org.perses.latra.smt

import org.perses.grammar.smtlibv2.OrigSmtLibV2ParserFacade
import org.perses.latra.AbstractLatraRewriterBuilder
import org.perses.latra.ParsingRelatedArguments
import org.perses.latra.SparAntlrTreeTriple
import org.perses.latra.SparTreeMatch
import org.perses.latra.TransformationUtility
import org.perses.latra.TransformationUtility.createStringFromNode
import org.perses.latra.TransformationUtility.findLexerRuleNodesInSubtree

/**
 * When there are two variables have same type. Merge them into one by delete
 * one of the declaration and update the corresponding usage of variable.
 * Similar to the Identifier Replacement in Vulcan but more specific.
 *
 *
 */
class MergeVariableTransformation(
  parsingRelatedArguments: ParsingRelatedArguments<OrigSmtLibV2ParserFacade>,
) : AbstractSMTLatraTransformation(
  pattern =
  """
     ( declare-fun <name:symbol> () <sort> )
  """.trimIndent(),
  patternRoot = "command",
  parsingRelatedArguments,
) {

  override fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder {
    return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
      override fun internalBuild() {
        val parser = parsingRelatedArguments.parser

        val sort = createStringFromNode(match.labelMap["sort"].single())
        val name = createStringFromNode(match.labelMap["name"].single())
        val variableMatches = TransformationUtility.findPatternInSubtree(
          pattern = "( declare-fun <symbol> () $sort )",
          ruleName = "command",
          sparTree.root,
          parser,
          sparAntlrTreeTriple.sparAntlrNodeMapping,
        )
        for (variableMatch in variableMatches) {
          val funcName = createStringFromNode(variableMatch.labelMap["symbol"].single())
          if (funcName != name) {
            val locs = findLexerRuleNodesInSubtree(sparTree.root, funcName)
            substituteInTree(locs, match.labelMap["name"].single())
            deleteInTree(variableMatch.subtreeRoot)
          }
        }
      }
    }
  }
}
