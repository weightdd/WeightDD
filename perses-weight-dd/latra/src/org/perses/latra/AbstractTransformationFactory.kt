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
import org.perses.grammar.AbstractParserFacade
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

// TODO(cnsun): refactor
abstract class AbstractTransformationFactory<T : AbstractParserFacade>(
  private val inputString: String,
  val parserFacade: T,
  private val transformationClass: ImmutableList<KClass<out AbstractLatraTransformation<*>>>,
) {

  internal val parsingRelatedArguments = ParsingRelatedArguments.create(inputString, parserFacade)
  internal val sparTreeNodeFactory = parsingRelatedArguments.sparTreeNodeFactory

  fun createTransformations(): ImmutableList<AbstractLatraTransformation<*>> {
    val listBuilder = ImmutableList.builder<AbstractLatraTransformation<*>>()
    for (transformationClass in transformationClass) {
      val transformationInstance = transformationClass.primaryConstructor!!.call(
        parsingRelatedArguments,
      )
      listBuilder.add(transformationInstance)
    }
    return listBuilder.build()
  }

  fun rebuildTrees(): SparAntlrTreeTriple {
    return SparAntlrTreeTriple.create(inputString, parserFacade, sparTreeNodeFactory).triple
  }
}
