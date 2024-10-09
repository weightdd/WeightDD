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
package org.perses.latra.reduction.reducer

import com.google.common.collect.ImmutableList
import org.perses.grammar.c.LanguageC
import org.perses.grammar.smtlibv2.LanguageSmtLibV2
import org.perses.latra.CTransformationFactory
import org.perses.latra.SMTTransformationFactory
import org.perses.reduction.AbstractTokenReducer
import org.perses.reduction.FixpointReductionState
import org.perses.reduction.ReducerAnnotation
import org.perses.reduction.ReducerContext
import org.perses.spartree.AbstractSparTreeEdit
import org.perses.spartree.AbstractSparTreeEdit.Companion.createLatraGeneralTreeEdit
import org.perses.spartree.LatraGeneralActionSet
import org.perses.spartree.SparTree

class LatraReducer constructor(
  val reducerContext: ReducerContext,
) : AbstractTokenReducer(META, reducerContext) {
  val facade = reducerContext.configuration.parserFacade

  override fun internalReduce(fixpointReductionState: FixpointReductionState) {
    val sparTree = fixpointReductionState.sparTree.getTreeRegardlessOfParsability()

    val transformationFactory = if (facade.language == LanguageC) {
      CTransformationFactory(ioManager.readBestMainFile())
    } else if (facade.language == LanguageSmtLibV2) {
      SMTTransformationFactory(ioManager.readBestMainFile())
    } else {
      return
    }

    val transformations = transformationFactory.createTransformations()
    val sparAntlrTreeTriple = transformationFactory.rebuildTrees()
    val newSparTree = sparAntlrTreeTriple.sparTree

    for (transformation in transformations) {
      val editListBuilder = ImmutableList.Builder<AbstractSparTreeEdit<*>>()
      editListBuilder.addAll(
        transformation.rewriteAllMatches(
          sparAntlrTreeTriple,
        ),
      )

      val bestEdit = testAllTreeEditsAndReturnTheBest(editListBuilder.build())
      if (bestEdit != null) {
        newSparTree.applyEdit(bestEdit.edit)
        replaceSparTreeWithNewTree(sparTree, newSparTree)
        return
      }
    }
  }

  companion object {

    const val NAME = "Latra"

    fun replaceSparTreeWithNewTree(sparTree: SparTree, newSparTree: SparTree) {
      val builder = LatraGeneralActionSet.Builder("Latra Reducer modify sparTree")
      val count = sparTree.root.childCount
      check(count > 0)
      builder.replaceNode(sparTree.root.getChild(0), newSparTree.root)
      for (i in 1 until count) {
        builder.deleteNode(sparTree.root.getChild(i))
      }
      val edit = createLatraGeneralTreeEdit(sparTree, builder.build()!!)
      sparTree.applyEdit(edit)
    }

    val META = object : ReducerAnnotation(
      shortName = NAME,
      description = "Perform program transformations with optional user-defined transformations.",
      deterministic = true,
      reductionResultSizeTrend = ReductionResultSizeTrend.BEST_RESULT_SIZE_INCREASE,
    ) {
      override fun create(reducerContext: ReducerContext): ImmutableList<AbstractTokenReducer> {
        return ImmutableList.of(LatraReducer(reducerContext))
      }
    }
  }
}
