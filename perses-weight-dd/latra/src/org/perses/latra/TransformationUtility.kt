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
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern
import org.antlr.v4.runtime.tree.pattern.ParseTreePatternMatcher
import org.perses.antlr.ParseTreeWithParser
import org.perses.antlr.RuleHierarchyEntry
import org.perses.grammar.AbstractParserFacade
import org.perses.spartree.AbstractSparTreeNode
import org.perses.spartree.LexerRuleSparTreeNode
import org.perses.spartree.SparTreeBuilder
import org.perses.util.SimpleStack

object TransformationUtility {

  fun checkTypeInSESEPath(targetNode: AbstractSparTreeNode, type: RuleHierarchyEntry): Boolean {
    if (targetNode.payload != null) {
      for (i in targetNode.payload!!.asSinglePayloadList) {
        if (i.actualAntlrRuleType == type) {
          return true
        }
      }
    }
    return false
  }

  fun createNodeFromString(
    source: String,
    rule: String,
    parsingRelatedArguments: ParsingRelatedArguments<*>,
  ): AbstractSparTreeNode {
    val lexer = parsingRelatedArguments.lexer
    val parser = parsingRelatedArguments.parser
    val factory = parsingRelatedArguments.sparTreeNodeFactory
    val matcher = ParseTreePatternMatcher(lexer, parser)
    matcher.setDelimiters("N/A", "N/A", "\\")
    val pattern = matcher.compile(source, parser.getRuleIndex(rule))
    return SparTreeBuilder(
      factory,
      ParseTreeWithParser(pattern.patternTree, parser, lexer),
    ).result.root
  }

  fun createStringFromNode(node: AbstractSparTreeNode): String {
    val builder = StringBuilder()
    node.preOrderVisit { childNode ->
      if (childNode is LexerRuleSparTreeNode) {
        if (builder.isNotEmpty()) {
          builder.append(" ")
        }
        builder.append(childNode.token.text)
      }
      childNode.immutableChildView
    }
    return builder.toString()
  }

  /**
   * convert a list of Nodes into string
   * TODO(Gaosen): Formatting, need to record node token position
   */
  fun createStringFromNodes(nodeList: List<AbstractSparTreeNode>): String {
    val builder = StringBuilder()
    for (node in nodeList) {
      if (builder.isNotEmpty()) {
        builder.append(" ")
      }
      builder.append(createStringFromNode(node))
    }
    return builder.toString()
  }

  /**
   * find all nodes under sub-root with same token text as target node
   */
  fun findLexerRuleNodesInSubtree(subRoot: AbstractSparTreeNode, lexeme: String):
    ImmutableList<AbstractSparTreeNode> {
    require(lexeme.isNotEmpty() && !lexeme.first().isWhitespace() && !lexeme.last().isWhitespace())
    val builder = ImmutableList.Builder<AbstractSparTreeNode>()
    subRoot.preOrderVisit { node ->
      if (node.isTokenNode && ((node as LexerRuleSparTreeNode).token.text == lexeme)) {
        builder.add(node)
      }
      node.immutableChildView
    }
    return builder.build()
  }

  /**
   * find all nodes under sub-root with same token text as target node
   */
  fun findInSubtree(subRoot: AbstractSparTreeNode, targetType: RuleHierarchyEntry):
    ImmutableList<AbstractSparTreeNode> {
    val builder = ImmutableList.Builder<AbstractSparTreeNode>()
    subRoot.preOrderVisit { node ->
      if (checkTypeInSESEPath(node, targetType)) {
        builder.add(node)
      }
      node.immutableChildView
    }
    return builder.build()
  }

  fun matchByType(
    sparTreeRoot: AbstractSparTreeNode,
    type: RuleHierarchyEntry,
  ): ImmutableList<SparTreeMatch> {
    val matchListBuilder = ImmutableList.builder<SparTreeMatch>()
    val matches = findInSubtree(sparTreeRoot, type)
    for (match in matches) {
      matchListBuilder.add(SparTreeMatch(match, ImmutableListMultimap.of(type.ruleName, match)))
    }
    return matchListBuilder.build()
  }

  fun match(
    programParseTree: ParseTree,
    parseTreePattern: ParseTreePattern,
    sparAntlrBiMap: ImmutableBiMap<AbstractSparTreeNode, ParseTree>,
  ): ImmutableList<SparTreeMatch> {
    val matchListBuilder = ImmutableList.builder<SparTreeMatch>()

    val stack = SimpleStack.of(programParseTree)
    while (stack.isNotEmpty()) {
      val node = stack.remove()
      val parserTreeMatch = parseTreePattern.match(node)
      if (parserTreeMatch.succeeded()) {
        val labelMapBuilder = ImmutableListMultimap.builder<String, AbstractSparTreeNode>()
        for ((labelString, labelNodeList) in parserTreeMatch.labels) {
          for (labelNode in labelNodeList) {
            labelMapBuilder.put(labelString, sparAntlrBiMap.inverse()[labelNode]!!)
          }
        }
        matchListBuilder.add(
          SparTreeMatch(
            sparAntlrBiMap.inverse()[node]!!,
            labelMapBuilder.build(),
          ),
        )
      }
      for (i in 0 until node.childCount) {
        val child = node.getChild(i)
        stack.add(child)
      }
    }
    return matchListBuilder.build()
  }

  data class FunctionTableEntry(
    val parameters: ImmutableList<AbstractSparTreeNode>,
    val location: AbstractSparTreeNode,
  )

  fun buildFunctionCallTable(
    parser: Parser,
    parserFacade: AbstractParserFacade,
    functionPattern: String,
    functionPatternRule: String,
    parseTree: ParseTree,
    sparAntlrBiMap: ImmutableBiMap<AbstractSparTreeNode, ParseTree>,
    deriveArgument: (AbstractSparTreeNode, AbstractParserFacade)
    -> ImmutableList<AbstractSparTreeNode>,
  ): ImmutableMap<String, FunctionTableEntry> {
    val functionTable: HashMap<String, FunctionTableEntry> = HashMap()
    val builder = ImmutableMap.builder<String, FunctionTableEntry>()

    val targetPattern: ParseTreePattern =
      parser.compileParseTreePattern(functionPattern, parser.getRuleIndex(functionPatternRule))
    val generalMatchList = match(parseTree, targetPattern, sparAntlrBiMap)
    for (functionCall in generalMatchList) {
      val functionName = (functionCall.labelMap["name"].last() as LexerRuleSparTreeNode).token.text
      /**
       * record one call per function because rewrite too many function calls will increase the program size
       */
      if (functionName in functionTable) {
        continue
      }
      val parameters = deriveArgument(functionCall.labelMap["argumentList"].last(), parserFacade)
      functionTable[functionName] = FunctionTableEntry(parameters, functionCall.subtreeRoot)
    }
    return builder.putAll(functionTable).build()
  }

  fun findPatternInSubtree(
    pattern: String,
    ruleName: String,
    subtreeRoot: AbstractSparTreeNode,
    parser: Parser,
    sparAntlrBiMap: ImmutableBiMap<
      AbstractSparTreeNode,
      ParseTree,
      >,
  ): ImmutableList<SparTreeMatch> {
    val patternParseTree =
      parser.compileParseTreePattern(pattern, parser.getRuleIndex(ruleName))
    if (sparAntlrBiMap[subtreeRoot] == null) {
      return ImmutableList.of()
    }
    return match(sparAntlrBiMap[subtreeRoot]!!, patternParseTree, sparAntlrBiMap)
  }

/***
   * TODO: Find a better way to test the equality of two nodes.
   */
  fun getCorrespondingNode(
    copiedTreeRoot: AbstractSparTreeNode,
    origTreeRoot: AbstractSparTreeNode,
  ): AbstractSparTreeNode {
    val result = ArrayList<AbstractSparTreeNode>(1)
    copiedTreeRoot.preOrderVisit {
      if (it.nodeId == origTreeRoot.nodeId) {
        result.add(it)
        ImmutableList.of()
      } else {
        it.immutableChildView
      }
    }
    check(result.size == 1)
    return result.single()
  }
}
