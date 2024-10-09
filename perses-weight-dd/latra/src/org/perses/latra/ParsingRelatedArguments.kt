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

import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.perses.grammar.AbstractParserFacade
import org.perses.program.TokenizedProgramFactory
import org.perses.spartree.SparTreeNodeFactory

data class ParsingRelatedArguments<T : AbstractParserFacade>(
  val lexer: Lexer,
  val parser: Parser,
  val parserFacade: T,
  val sparTreeNodeFactory: SparTreeNodeFactory,
) {

  companion object {
    fun <T : AbstractParserFacade> create(
      inputString: String,
      parserFacade: T,
    ): ParsingRelatedArguments<T> {
      val parseTreeWithParser = parserFacade.parseString(inputString)
      val parser = parseTreeWithParser.parser
      val lexer = parseTreeWithParser.lexer
      val tokenizedProgramFactory = TokenizedProgramFactory.createFactory(
        AbstractParserFacade.getTokens(parseTreeWithParser.tree),
        parserFacade.language,
      )
      val sparTreeNodeFactory = SparTreeNodeFactory(
        parserFacade.metaTokenInfoDb,
        tokenizedProgramFactory,
        parserFacade.ruleHierarchy,
      )
      return ParsingRelatedArguments(lexer, parser, parserFacade, sparTreeNodeFactory)
    }
  }
}
