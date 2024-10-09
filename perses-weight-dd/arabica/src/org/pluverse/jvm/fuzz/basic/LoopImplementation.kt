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

abstract class LoopImplementation(
  val loopStatement: LoopStatement,
  val context: Context,
  val globalContext: GlobalContext,
) {
  fun generateInductionVar(): Variable {
    return Variable(
      context = context,
      flagAttribute = NameAttribute.INDUCTION.value,
      type = Configuration.P_INDUCTION_TYPE.getRandom(
        null,
        null,
        globalContext.randomGenerator,
      ),
      globalContext = globalContext,
    )
  }

  fun generateInduction(inductionVariable: Variable): String {
    if (step > 0) {
      if (step == 1) {
        if (globalContext.randomGenerator.nextInt(101) < 50) {
          return inductionVariable.generateName() + "++"
        }
        return "++" + inductionVariable.generateName()
      }
      return inductionVariable.generateName() + " += " +
        step.toString()
    }

    if (step == -1) {
      if (globalContext.randomGenerator.nextInt(101) < 50) {
        return inductionVariable.generateName() + "--"
      }
      return "--" + inductionVariable.generateName()
    }
    return inductionVariable.generateName() + " -= " +
      (step * -1).toString()
  }

  fun generateCounter(loopCounter: Variable): String {
    var returnResult = ""

    returnResult += globalContext.printLine(
      loopCounter.generateName() +
        "++;",
    )
    returnResult += globalContext.printLine(
      "if(" + loopCounter.generateName() + " > 100){",
    )
    globalContext.updateIndentation(1)
    returnResult += globalContext.printLine("break;")
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }

  val nestedStatement = JavaStatement.generateStatementSequence(
    loopStatement,
    context,
    globalContext,
    Configuration.MAX_LOOP_STATEMENT,
  )

  val step = Configuration.FOR_STEP.getRandom(
    null,
    null,
    globalContext.randomGenerator,
  )

  abstract fun generate(): String
}
