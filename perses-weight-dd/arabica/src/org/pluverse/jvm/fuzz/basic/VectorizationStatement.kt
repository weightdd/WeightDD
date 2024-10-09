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

 /*
 The approach for the vectorization is inspired from this article:
 http://daniel-strecker.com/blog/2020-01-14_auto_vectorization_in_java/
  */
package org.pluverse.jvm.fuzz.basic

import com.google.common.collect.ImmutableSet
import org.pluverse.jvm.fuzz.util.Configuration
import org.pluverse.jvm.fuzz.util.JavaType
import org.pluverse.jvm.fuzz.util.OperatorCategory

class VectorizationStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {

  private val arrayType = Configuration.TYPES.getRandom(
    null,
    ImmutableSet.of(
      JavaType.BOOLEAN,
      JavaType.OBJECT,
      JavaType.STRING,
      JavaType.ARRAY,
    ),
    globalContext.randomGenerator,
  )

  private val chosenArray = ScalarExpression(
    parentStatement = this,
    context = context,
    depth = 1,
    globalContext = globalContext,
    type = JavaType.ARRAY,
    flag = 0,
    arrayAttribute = Array.ArrayAttribute(
      arrayType,
      dimension = globalContext.randomGenerator.nextInt(Configuration.MAX_ARRAY_DIMENSION),
      size = globalContext.randomGenerator.nextInt(Configuration.ARRAY_DEFAULT_SIZE) + 1,
    ),
  )

  private val arrayExpression = ArrayExpression(
    parentStatement = this,
    context = context,
    depth = 1,
    globalContext = globalContext,
    type = arrayType,
    inputArray = chosenArray,
    indexInput = "vecI",
  )

  private val rightOperand = JavaExpression.generateExpression(
    parentStatement = this,
    context,
    depth = 1,
    globalContext,
    arrayType,
    ignoreSet = ImmutableSet.of(
      ExpressionType.INVOCATION_EXPRESSION,
      ExpressionType.OPERATOR_EXPRESSION,
      ExpressionType.ASSIGNMENT_EXPRESSION,
    ),
  )

  private fun generateOperator(): Operator {
    return Operator.getOperator(
      Configuration.OPERATORS[OperatorCategory.ARITH_ASSN]!!.getRandom(
        null,
        ImmutableSet.of("="),
        globalContext.randomGenerator,
      ),
      null,
      OperatorCategory.ARITH_ASSN,
      null,
    )
  }

  private val expressionOperator = generateOperator()

  private val vectorizationStatement = OperatorExpression(
    parentStatement = this,
    context,
    depth = 1,
    globalContext,
    arrayType,
    expressionOperator,
    arrayExpression,
    rightOperand,
  )

  override fun generate(): String {
    var returnResult = ""
    returnResult += globalContext.printLine(
      "for (int vecI = 0; vecI < " + (chosenArray.scalarVariable as Array).size + "; vecI++){",
    )

    globalContext.updateIndentation(1)
    returnResult += globalContext.printLine(vectorizationStatement.generate() + ";")
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
