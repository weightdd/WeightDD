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

import org.pluverse.jvm.fuzz.util.JavaType

class BooleanForLoop(
  loopStatement: LoopStatement,
  context: Context,
  globalContext: GlobalContext,
) : LoopImplementation(
  loopStatement = loopStatement,
  context = context,
  globalContext = globalContext,
) {
  val condition = JavaExpression.generateExpression(
    loopStatement,
    context,
    0,
    globalContext,
    JavaType.BOOLEAN,
  )

  val secondCondition = ScalarExpression(
    loopStatement,
    context,
    0,
    globalContext,
    JavaType.BOOLEAN,
  )

  val loopCounter = Variable(
    context = context,
    flagAttribute = NameAttribute.INDUCTION.value,
    type = JavaType.INT,
    globalContext = globalContext,
    value = "0",
  )

  override fun generate(): String {
    val condition = condition.generate() + " || " + secondCondition.generate()
    var returnResult = ""
    returnResult += globalContext.printLine(
      "for (; " + condition + " ;) {",
    )

    globalContext.updateIndentation(1)

    returnResult += generateCounter(loopCounter)

    nestedStatement.forEach {
      returnResult += it.generate()
    }
    globalContext.updateIndentation(-1)
    return returnResult + globalContext.printLine("}")
  }
}
