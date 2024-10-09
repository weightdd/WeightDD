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

/**
 * Try to replace any node having type of "term" with "( _ bv0 1)"
 * This one might be expensive since "term" is recursively generated
 *
 */
class SubstituteWithConstTransformation(
  parsingRelatedArguments: ParsingRelatedArguments<OrigSmtLibV2ParserFacade>,
) : AbstractSMTLatraTransformation(
  pattern = "",
  patternRoot = "term",
  parsingRelatedArguments,
) {

  override fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder {
    return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
      override fun internalBuild() {
        return substituteInTree(
          match.subtreeRoot,
          TransformationUtility.createNodeFromString(
            "( _ bv0 1)",
            "term",
            parsingRelatedArguments,
          ),
        )
      }
    }
  }
}
