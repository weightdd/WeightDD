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
import org.pluverse.jvm.fuzz.util.JavaType

class SimpleForLoop(
  loopStatement: LoopStatement,
  context: Context,
  globalContext: GlobalContext,
) : LoopImplementation(
  loopStatement = loopStatement,
  context = context,
  globalContext = globalContext,
) {
  val maxValue = LiteralExpression(
    loopStatement,
    context,
    0,
    globalContext,
    JavaType.INT,
    globalContext.randomGenerator.nextInt(
      Configuration.MAX_SIZE + 1,
    ).toString(),
  )

  val inductionVariable = generateInductionVar()

  override fun generate(): String {
    var maxValueString = maxValue.generate()
    val condition = inductionVariable.generateName() +
      (if (step > 0) " < " + maxValueString else " > 0") + "; "
    var firstLine = "for (" + inductionVariable.generateName() + " = " +
      (if (step > 0) "1" else maxValueString) + "; "

    firstLine += condition

    firstLine += generateInduction(inductionVariable) + "){"

    var returnResult = globalContext.printLine(firstLine)

    globalContext.updateIndentation(1)
    nestedStatement.forEach {
      returnResult += it.generate()
    }
    globalContext.updateIndentation(-1)
    return returnResult + globalContext.printLine("}")
  }
}
