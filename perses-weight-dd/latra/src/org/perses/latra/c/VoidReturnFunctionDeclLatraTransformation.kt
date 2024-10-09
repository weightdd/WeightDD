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

class VoidReturnFunctionDeclLatraTransformation(
  parsingRelatedArguments: ParsingRelatedArguments<OrigCParserFacade>,
) : AbstractCLatraTransformation(
  "<retType:declarationSpecifiers> <name:Identifier> ( <parameterList> );",
  "declaration",
  parsingRelatedArguments,
) {
  override fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder {
    return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
      override fun internalBuild() {
        val name = TransformationUtility.createStringFromNode(match.labelMap["name"].single())
        val parameterList =
          TransformationUtility.createStringFromNode(match.labelMap["parameterList"].single())
        val newTreeRoot = TransformationUtility.createNodeFromString(
          source = "void $name ( $parameterList ); ",
          rule = "externalDeclaration",
          parsingRelatedArguments,
        )
        substituteInTree(match.subtreeRoot, newTreeRoot)
      }
    }
  }
}
