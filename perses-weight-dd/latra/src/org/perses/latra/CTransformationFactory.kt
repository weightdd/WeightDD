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
import org.perses.grammar.c.OrigCParserFacade
import org.perses.latra.c.FunctionInliningLatraTransformation
import org.perses.latra.c.TypeDefTransform
import org.perses.latra.c.VoidReturnFunctionDeclLatraTransformation
import org.perses.latra.c.VoidReturnFunctionDefLatraTransformation

// TODO(cnsun): refactor
class CTransformationFactory(inputString: String) :
  AbstractTransformationFactory<OrigCParserFacade>(
    inputString,
    OrigCParserFacade(),
    ImmutableList.of(
      FunctionInliningLatraTransformation::class,
      TypeDefTransform::class,
      VoidReturnFunctionDeclLatraTransformation::class,
      VoidReturnFunctionDefLatraTransformation::class,
    ),
  )
