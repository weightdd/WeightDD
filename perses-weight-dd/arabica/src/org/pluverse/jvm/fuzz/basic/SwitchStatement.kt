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

class SwitchStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {
  private val switchSize = globalContext.randomGenerator.nextInt(11) + 10

  private val minValue = globalContext.randomGenerator.nextInt(129 - switchSize) - 64

  private val maxValue = minValue + switchSize

  private fun generateSwitchExpression(): JavaExpression {
    return JavaExpression.generateExpression(
      parentStatement = this,
      context = context,
      depth = 0,
      globalContext = globalContext,
      type = JavaType.BYTE,
    )
  }

  private val switchExpression = generateSwitchExpression()

  private fun generateSwitchBlock(): ArrayList<Pair<String, ArrayList<JavaStatement>>> {
    val blockOutput = arrayListOf<Pair<String, ArrayList<JavaStatement>>>()

    for (counter in minValue..maxValue) {
      val caseExpression = "case " + counter + ": "
      val currentStatementBlock = JavaStatement.generateStatementSequence(
        currentStatement = this,
        context = context,
        globalContext = globalContext,
        Configuration.MAX_SWITCH_STATEMENT,
      )

      blockOutput.add(Pair(caseExpression, currentStatementBlock))
    }

    if (globalContext.randomGenerator.nextInt(101) < 50) {
      val currentStatementBlock = JavaStatement.generateStatementSequence(
        currentStatement = this,
        context = context,
        globalContext = globalContext,
        Configuration.MAX_SWITCH_STATEMENT,
      )
      blockOutput.add(Pair("default: ", currentStatementBlock))
    }
    return blockOutput
  }

  private val switchBlock = generateSwitchBlock()

  override fun generate(): String {
    var blockString = ""
    blockString += globalContext.printLine("switch (" + switchExpression.generate() + "){")
    globalContext.updateIndentation(1)

    for (entry in switchBlock) {
      blockString += globalContext.printLine(entry.first)
      globalContext.updateIndentation(1)
      for (currentStatement in entry.second) {
        blockString += currentStatement.generate()
      }
      if (globalContext.randomGenerator.nextInt(101) < 50) {
        blockString += globalContext.printLine("break;")
      }
      globalContext.updateIndentation(-1)
    }
    globalContext.updateIndentation(-1)
    blockString += globalContext.printLine("}")
    return blockString
  }
}
