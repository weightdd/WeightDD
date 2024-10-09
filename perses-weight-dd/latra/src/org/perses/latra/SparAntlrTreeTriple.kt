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

import com.google.common.collect.ImmutableBiMap
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.perses.grammar.AbstractParserFacade
import org.perses.spartree.AbstractSparTreeNode
import org.perses.spartree.SparTree
import org.perses.spartree.SparTreeBuilder
import org.perses.spartree.SparTreeNodeFactory

data class SparAntlrTreeTriple(
  val sparTree: SparTree,
  val antlrTree: ParseTree,
  val sparAntlrNodeMapping: ImmutableBiMap<AbstractSparTreeNode, ParseTree>,
) {

  data class SparAntlrTreeTripleWithParser(
    val triple: SparAntlrTreeTriple,
    val parser: Parser,
  )

  companion object {

    fun create(
      program: String,
      facade: AbstractParserFacade,
      sparTreeNodeFactory: SparTreeNodeFactory,
    ): SparAntlrTreeTripleWithParser {
      val parseTreeWithParser = facade.parseString(program)
      val sparTreeBuilder = SparTreeBuilder(
        sparTreeNodeFactory,
        parseTreeWithParser,
        simplifyTree = false,
      )
      return SparAntlrTreeTripleWithParser(
        SparAntlrTreeTriple(
          sparTree = sparTreeBuilder.result,
          antlrTree = parseTreeWithParser.tree,
          sparAntlrNodeMapping = sparTreeBuilder.sparAntlrBiMap,
        ),
        parseTreeWithParser.parser,
      )
    }
  }
}
