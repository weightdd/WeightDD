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

class ComplexForLoop(
  loopStatement: LoopStatement,
  context: Context,
  globalContext: GlobalContext,
) : LoopImplementation(
  loopStatement = loopStatement,
  context = context,
  globalContext = globalContext,
) {
  val maxValue = JavaExpression.generateExpression(
    loopStatement,
    context,
    0,
    globalContext,
    type = Configuration.P_INDUCTION_TYPE.getRandom(
      null,
      null,
      globalContext.randomGenerator,
    ),
    0,
    null,
    null,
    ImmutableSet.of<ExpressionType>(ExpressionType.ASSIGNMENT_EXPRESSION),
  )

  val initValue = JavaExpression.generateExpression(
    loopStatement,
    context,
    0,
    globalContext,
    type = Configuration.P_INDUCTION_TYPE.getRandom(
      null,
      null,
      globalContext.randomGenerator,
    ),
    ignoreSet = ImmutableSet.of<ExpressionType>(ExpressionType.ASSIGNMENT_EXPRESSION),
  )

  val loopCounter = Variable(
    context = context,
    flagAttribute = NameAttribute.INDUCTION.value,
    type = JavaType.INT,
    globalContext = globalContext,
    value = "0",
  )

  val inductionVariable = generateInductionVar()

  override fun generate(): String {
    var maxValueString = maxValue.generate()

    if (maxValue.type.weight > inductionVariable.type.weight) {
      maxValueString = "(" + inductionVariable.type.stringValue + ")" +
        maxValueString
    }

    var initializationResult = inductionVariable.generateName() + "="

    if (initValue.type.weight > inductionVariable.type.weight) {
      initializationResult += "(" + inductionVariable.type.stringValue + ")"
    }

    val condition = inductionVariable.generateName() +
      (if (step > 0) " < " + maxValueString else " > " + maxValueString) + "; "
    var firstLine = "for(" + initializationResult + initValue.generate() + "; "

    firstLine += condition

    firstLine += generateInduction(inductionVariable) + "){"

    var returnResult = globalContext.printLine(firstLine)

    globalContext.updateIndentation(1)
    returnResult += generateCounter(loopCounter)
    nestedStatement.forEach {
      returnResult += it.generate()
    }
    globalContext.updateIndentation(-1)
    return returnResult + globalContext.printLine("}")
  }
}
