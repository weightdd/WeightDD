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
import org.perses.grammar.smtlibv2.OrigSmtLibV2ParserFacade
import org.perses.latra.smt.AnnotationRemovalTransformation
import org.perses.latra.smt.BvConverstionTransformation
import org.perses.latra.smt.CheckSatAssuminTransformation
import org.perses.latra.smt.DoubleNegElimTransformation
import org.perses.latra.smt.DoubleNotElimTransformation
import org.perses.latra.smt.EvalFalseTransformation
import org.perses.latra.smt.ExistsRemovalTransformation
import org.perses.latra.smt.ForallRemovalTransformation
import org.perses.latra.smt.LetSubstitutionTransformation
import org.perses.latra.smt.MergeAndTransformation
import org.perses.latra.smt.MergePlusTransformation
import org.perses.latra.smt.MergeVariableTransformation
import org.perses.latra.smt.SubstituteWithConstTransformation

// TODO(cnsun): refactor
class SMTTransformationFactory(inputString: String) :
  AbstractTransformationFactory<OrigSmtLibV2ParserFacade>(
    inputString,
    OrigSmtLibV2ParserFacade(),
    ImmutableList.of(
      AnnotationRemovalTransformation::class,
      BvConverstionTransformation::class,
      CheckSatAssuminTransformation::class,
      DoubleNotElimTransformation::class,
      DoubleNegElimTransformation::class,
      EvalFalseTransformation::class,
      ExistsRemovalTransformation::class,
      ForallRemovalTransformation::class,
      LetSubstitutionTransformation::class,
      MergeAndTransformation::class,
      MergePlusTransformation::class,
      MergeVariableTransformation::class,
      SubstituteWithConstTransformation::class,
    ),
  )
