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
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.perses.antlr.ParseTreeWithParser
import org.perses.grammar.AbstractParserFacade
import org.perses.spartree.LatraGeneralTreeEdit
import org.perses.spartree.SparTree
import org.perses.spartree.SparTreeBuilder
import org.perses.spartree.SparTreeNodeFactory

abstract class AbstractLatraTransformation<T : AbstractParserFacade>(
  protected val pattern: String,
  protected val patternRoot: String,
  protected val parsingRelatedArguments: ParsingRelatedArguments<T>,
) {

  protected val parser: Parser
    get() = parsingRelatedArguments.parser

  protected val lexer: Lexer
    get() = parsingRelatedArguments.lexer

  protected val sparTreeNodeFactory: SparTreeNodeFactory
    get() = parsingRelatedArguments.sparTreeNodeFactory

  protected val parserFacade: T
    get() = parsingRelatedArguments.parserFacade

  private fun buildPatternTree(): SparTree {
    val patternParseTree =
      parser.compileParseTreePattern(pattern, parser.getRuleIndex(patternRoot)).patternTree
    return SparTreeBuilder(
      sparTreeNodeFactory,
      ParseTreeWithParser(patternParseTree, parser, lexer),
    ).result
  }

  abstract fun createLatraRewriterBuilder(
    match: SparTreeMatch,
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): AbstractLatraRewriterBuilder

  fun rewriteAllMatches(
    sparAntlrTreeTriple: SparAntlrTreeTriple,
  ): List<LatraGeneralTreeEdit> {
    val edits = arrayListOf<LatraGeneralTreeEdit>()
    val matches: ImmutableList<SparTreeMatch> = if (pattern == "") {
      TransformationUtility.matchByType(
        sparAntlrTreeTriple.sparTree.root,
        parserFacade.ruleHierarchy.getRuleHierarchyEntryOrNull(patternRoot)!!,
      )
    } else {
      TransformationUtility.match(
        sparAntlrTreeTriple.antlrTree,
        parser.compileParseTreePattern(pattern, parser.getRuleIndex(patternRoot)),
        sparAntlrTreeTriple.sparAntlrNodeMapping,
      )
    }

    for (i in matches) {
      val rewriterBuilder = createLatraRewriterBuilder(i, sparAntlrTreeTriple)
      rewriterBuilder.internalBuild()
      val rewriter = rewriterBuilder.build() ?: continue
      edits.add(sparAntlrTreeTriple.sparTree.createLatraGeneralEdit(rewriter))
    }
    return edits
  }
}
