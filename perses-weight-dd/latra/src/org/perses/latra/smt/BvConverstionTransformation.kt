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
 * (bvnot (_ bv0 <numeral>)) represents the bitwise negation (NOT) operation applied to
 *  the bit vector constant bv0 with a width of <>. In binary, bv0 is 0.
 * (bv1 1) represents the bit vector constant bv1 with a width of <numeral>.
 * They have the same meaning.
 */
class BvConverstionTransformation(
  parsingRelatedArguments: ParsingRelatedArguments<OrigSmtLibV2ParserFacade>,
) : AbstractSMTLatraTransformation(
  pattern =
  """
    (  bvnot  (  _  bv0  <numeral>  )  )
  """.trimIndent(),
  patternRoot = "term",
  parsingRelatedArguments,
) {
  override fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder {
    return object : AbstractLatraRewriterBuilder(match, sparAntlrTreeTriple.sparTree) {
      override fun internalBuild() {
        val num = TransformationUtility.createStringFromNode(match.labelMap["numeral"].single())
        return substituteInTree(
          match.subtreeRoot,
          TransformationUtility.createNodeFromString(
            "(  _  bv1  $num  )",
            "term",
            parsingRelatedArguments,
          ),
        )
      }
    }
  }
}
