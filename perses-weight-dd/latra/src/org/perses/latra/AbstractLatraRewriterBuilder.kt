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

import org.perses.spartree.AbstractNodePayload
import org.perses.spartree.AbstractSparTreeNode
import org.perses.spartree.AbstractTreeNode.NodeIdCopyStrategy.ReuseNodeIdStrategy
import org.perses.spartree.LatraGeneralActionSet
import org.perses.spartree.LexerRuleSparTreeNode
import org.perses.spartree.SparTree

abstract class AbstractLatraRewriterBuilder(
  protected val match: SparTreeMatch,
  protected val sparTree: SparTree,
) {

  private val actionSetBuilder =
    LatraGeneralActionSet.Builder("Latra Transformation Builder: ${this::class.java}")

  protected fun substituteInTree(
    nodeList: List<AbstractSparTreeNode>,
    replacement: AbstractSparTreeNode,
  ) {
    for (i in nodeList) {
      if (i.nodeId == replacement.nodeId) {
        continue
      }
      actionSetBuilder.replaceNode(i, replacement.recursiveDeepCopy(ReuseNodeIdStrategy))
    }
  }

  protected fun substituteInTree(
    node: AbstractSparTreeNode,
    replacement: AbstractSparTreeNode,
  ) {
    actionSetBuilder.replaceNode(node, replacement.recursiveDeepCopy(ReuseNodeIdStrategy))
  }

  protected fun substituteInTreeDirectly(
    nodeList: List<AbstractSparTreeNode>,
    replacement: AbstractSparTreeNode,
  ) {
    for (targertNode in nodeList) {
      val replacementDuplicate = replacement.recursiveDeepCopy(ReuseNodeIdStrategy)
      targertNode.parent!!.replaceChild(
        targertNode,
        replacementDuplicate,
        AbstractNodePayload.SinglePayload(replacementDuplicate.antlrRule),
      )
    }
  }

  protected fun deleteInTree(node: AbstractSparTreeNode) {
    actionSetBuilder.deleteNode(node)
  }

  protected fun findLeafNodeInSubtree(
    node: AbstractSparTreeNode,
    targetNode: AbstractSparTreeNode,
  ): List<AbstractSparTreeNode> {
    check(targetNode is LexerRuleSparTreeNode)
    return TransformationUtility.findLexerRuleNodesInSubtree(node, targetNode.token.text)
  }

  abstract fun internalBuild()

  fun build(): LatraGeneralActionSet? {
    return actionSetBuilder.build()
  }
}
