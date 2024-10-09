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

import org.perses.grammar.c.OrigCParserFacade
import org.perses.latra.AbstractLatraRewriterBuilder
import org.perses.latra.ParsingRelatedArguments
import org.perses.latra.SparAntlrTreeTriple
import org.perses.latra.SparTreeMatch
import org.perses.latra.TransformationUtility
import org.perses.spartree.AbstractTreeNode.NodeIdCopyStrategy.ReuseNodeIdStrategy

class VoidReturnFunctionDefLatraTransformation(
  parsingRelatedArguments: ParsingRelatedArguments<OrigCParserFacade>,
) : AbstractCLatraTransformation(
  pattern =
  """
    <declarationSpecifier> <name:Identifier> 
      ( <parameterList>) { <block:blockItemList> }
  """.trimIndent(),
  patternRoot = "functionDefinition",
  parsingRelatedArguments,
) {

  override fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder {
    return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
      override fun internalBuild() {
        if (TransformationUtility.createStringFromNode(
            match.labelMap["declarationSpecifier"].single(),
          ) != "void"
        ) {
          val parser = parsingRelatedArguments.parser

          val block = match.labelMap["block"].single()
          val matches = TransformationUtility.findPatternInSubtree(
            pattern = "return <stm:expression> ; ",
            ruleName = "blockItem",
            block,
            parser,
            sparAntlrTreeTriple.sparAntlrNodeMapping,
          )
          val blockCopy = block.recursiveDeepCopy(ReuseNodeIdStrategy)
          for (subTreeMatch in matches) {
            TransformationUtility.getCorrespondingNode(blockCopy, subTreeMatch.subtreeRoot).delete()
          }

          val name = TransformationUtility.createStringFromNode(match.labelMap["name"].single())
          val parameterList =
            TransformationUtility.createStringFromNode(match.labelMap["parameterList"].single())
          val newBlock = TransformationUtility.createStringFromNode(blockCopy)

          val newTreeRoot = TransformationUtility.createNodeFromString(
            source = "void $name ( $parameterList ) { $newBlock } ",
            rule = "functionDefinition",
            parsingRelatedArguments,
          )
          substituteInTree(match.subtreeRoot, newTreeRoot)
        }
      }
    }
  }
}
