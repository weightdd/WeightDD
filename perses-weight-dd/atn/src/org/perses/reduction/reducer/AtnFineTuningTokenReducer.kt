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
package org.perses.reduction.reducer

import com.google.common.collect.ImmutableList
import org.perses.antlr.atn.LexerAtnWrapper
import org.perses.antlr.atn.tdtree.AbstractTDTreeNode
import org.perses.antlr.atn.tdtree.TDTree
import org.perses.reduction.AbstractTokenReducer
import org.perses.reduction.FixpointReductionState
import org.perses.reduction.ReducerAnnotation
import org.perses.reduction.ReducerContext
import org.perses.reduction.reducer.vulcan.TokenEditUtility
import org.perses.spartree.SparTree

class AtnFineTuningTokenReducer(
  reducerContext: ReducerContext,
) : AbstractTokenReducer(META, reducerContext) {

  override fun internalReduce(fixpointReductionState: FixpointReductionState) {
    val atnWrapper = configuration.parserFacade.lexerAtnWrapper
    val tree = fixpointReductionState.sparTree.getTreeRegardlessOfParsability()
    val tokenCount = tree.tokenCount
    for (i in 0 until tokenCount) {
      val tdTree = createTdTree(tree, i, atnWrapper) ?: continue
      var successEdit: Set<AbstractTDTreeNode>?
      do {
        val currentNode = tree.getLatestNthLeafNodeCostly(i)
        assert(!currentNode.isPermanentlyDeleted)
        successEdit = tdTree.deletableNodesFromTopToBottomSequence().firstOrNull { nodesToDelete ->
          val newLexeme = tdTree.root.toLexeme(nodesToDelete)
          assert(currentNode.token.text != newLexeme) {
            "${currentNode.token.text}, $newLexeme"
          }
          val testResult = testAllTreeEditsAndReturnTheBest(
            listOf(
              TokenEditUtility.createEditToReplaceSingleLexerNode(
                tree,
                newLexeme,
                currentNode,
              ),
              TokenEditUtility.createEditToReplaceAllLexerNodesHavingSameLexeme(
                tree,
                currentNode.token.text,
                newLexeme,
              ),
            ),
          ) ?: return@firstOrNull false
          tree.applyEdit(testResult.edit)

          with(tdTree) {
            deleteNodes(nodesToDelete)
            removeNodesWithNoCharLeaves()
          }
          assert(currentNode.isPermanentlyDeleted)
          check(
            tree.getLatestNthLeafNodeCostly(i)
              .token.text.length < currentNode.token.text.length,
          ) {
            "'${tree.getLatestNthLeafNodeCostly(i).token.text}', " +
              "'${currentNode.token.text}', ${testResult.edit}"
          }
          true
        }
      } while (successEdit != null)
    }
  }

  private fun createTdTree(tree: SparTree, nthToken: Int, atnWrapper: LexerAtnWrapper<*>): TDTree? {
    val nthLeafNode = tree.getLatestNthLeafNodeCostly(nthToken)
    return atnWrapper.createTDTree(
      lexeme = nthLeafNode.token.text,
      ruleType = nthLeafNode.token.type,
    )
  }

  companion object {

    const val NAME = "atn_token"

    val META = object : ReducerAnnotation(
      shortName = NAME,
      description = "",
      deterministic = true,
      reductionResultSizeTrend = ReductionResultSizeTrend.BEST_RESULT_SIZE_REMAIN,
    ) {
      override fun create(reducerContext: ReducerContext): ImmutableList<AbstractTokenReducer> {
        return ImmutableList.of(AtnFineTuningTokenReducer(reducerContext))
      }
    }
  }
}
