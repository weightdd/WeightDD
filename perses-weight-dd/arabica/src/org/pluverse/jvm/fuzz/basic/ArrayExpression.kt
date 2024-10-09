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

import org.pluverse.jvm.fuzz.util.Configuration
import org.pluverse.jvm.fuzz.util.ExceptionType
import org.pluverse.jvm.fuzz.util.JavaType

class ArrayExpression(
  parentStatement: JavaStatement,
  context: Context,
  depth: Int,
  globalContext: GlobalContext,
  type: JavaType = Util.generateType(globalContext),
  flag: Int = 0,
  arrayAttribute: Array.ArrayAttribute? = Util.generateArrayAttribute(globalContext, type),
  classAttribute: JavaClass? = Util.generateClassAttribute(globalContext, type, context),
) : JavaExpression(
  parentStatement = parentStatement,
  context = context,
  depth = depth,
  type = type,
  flag = flag,
  globalContext = globalContext,
  arrayAttribute = arrayAttribute,
  classAttribute = classAttribute,
) {
  private var arrayScalar = ScalarExpression(
    parentStatement = parentStatement,
    context = context,
    depth = 1,
    globalContext = globalContext,
    type = JavaType.ARRAY,
    flag = 0,
    arrayAttribute = Array.ArrayAttribute(
      arrayType = type,
      dimension = 1,
      size = globalContext.randomGenerator.nextInt(Configuration.ARRAY_DEFAULT_SIZE) + 1,
    ),
  )

  private fun generateIndex(): String {
    // If catching array index exception, bigger range of indices
    if (parentStatement.findParentTryStatement(ExceptionType.ARRAY_INDEX_EXCEPTION)) {
      return globalContext.randomGenerator.nextInt(
        (arrayScalar.scalarVariable as Array).size * 10,
      ).toString()
    }
    return globalContext.randomGenerator.nextInt(
      (arrayScalar.scalarVariable as Array).size,
    ).toString()
  }

  private var indexElement = generateIndex()

  private fun generateArrayExpression(): String {
    return arrayScalar.generate() + "[" + indexElement + "]"
  }

  /*Considering refactoring this code
  calling secondary constructor invoke primary constructor first
  this is redundant*/
  constructor(
    parentStatement: JavaStatement,
    context: Context,
    depth: Int,
    globalContext: GlobalContext,
    type: JavaType,
    inputArray: ScalarExpression,
    indexInput: String,
  ) : this(
    parentStatement,
    context,
    depth,
    globalContext,
    type,
  ) {
    arrayScalar = inputArray
    indexElement = indexInput
  }

  override fun generate(): String {
    if (flagCheck(ExpressionAttribute.CAST)) {
      return "(" + type.stringValue + ")" + generateArrayExpression()
    }
    return generateArrayExpression()
  }
}
