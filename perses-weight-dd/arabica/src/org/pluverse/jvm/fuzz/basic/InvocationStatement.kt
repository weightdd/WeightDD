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
package org.pluverse.jvm.fuzz.basic

import com.google.common.collect.ImmutableSet
import org.pluverse.jvm.fuzz.util.Configuration
import org.pluverse.jvm.fuzz.util.JavaType

class InvocationStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {
  private fun generateInvocation(): InvocationExpression {
    if (globalContext.randomGenerator.nextInt(101) < 90) {
      return InvocationExpression(
        this,
        context,
        0,
        globalContext,
        JavaType.VOID,
      )
    }
    return InvocationExpression(
      this,
      context,
      0,
      globalContext,
      Configuration.TYPES.getRandom(
        null,
        ImmutableSet.of(JavaType.OBJECT, JavaType.ARRAY),
        globalContext.randomGenerator,
      ),
    )
  }

  val invocationInfo = generateInvocation()

  override fun generate(): String {
    return globalContext.printLine(invocationInfo.generate() + ";")
  }
}
