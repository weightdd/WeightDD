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

class ConditionalStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {
  private fun generateElseBlock(): ArrayList<Pair<String, ArrayList<JavaStatement>>> {
    var numberElseIf = 0
    var outputBlock = arrayListOf<Pair<String, ArrayList<JavaStatement>>>()
    while (globalContext.randomGenerator.nextInt(101) < Configuration.P_ELSE_IF) {
      numberElseIf += 1
    }
    if (numberElseIf > 0) {
      for (i in 0..numberElseIf) {
        val elseIfExpression = JavaExpression.generateExpression(
          this,
          context,
          0,
          globalContext,
          JavaType.BOOLEAN,
        )
        val elseIfBlock = JavaStatement.generateStatementSequence(
          this,
          context,
          globalContext,
          Configuration.MAX_IF_STATEMENT,
        )
        val elseIfBoolean = "else if (" + elseIfExpression.generate() + "){"
        outputBlock.add(Pair(elseIfBoolean, elseIfBlock))
      }
    }

    if (globalContext.randomGenerator.nextInt(101) < Configuration.P_ELSE) {
      val elseBlock = JavaStatement.generateStatementSequence(
        this,
        context,
        globalContext,
        Configuration.MAX_IF_STATEMENT,
      )
      outputBlock.add(Pair("else {", elseBlock))
    }
    return outputBlock
  }

  val ifBlock = JavaStatement.generateStatementSequence(
    this,
    context,
    globalContext,
    Configuration.MAX_IF_STATEMENT,
  )

  val ifExpression = JavaExpression.generateExpression(
    this,
    context,
    0,
    globalContext,
    JavaType.BOOLEAN,
  )

  val elseBlock = generateElseBlock()

  override fun generate(): String {
    var returnResult = globalContext.printLine(
      "if(" +
        ifExpression.generate() + ")" + "{",
    )
    globalContext.updateIndentation(1)
    ifBlock.forEach {
      returnResult += it.generate()
    }
    globalContext.updateIndentation(-1)
    if (elseBlock.size > 0) {
      returnResult += globalContext.printNoLine("} ")
    } else {
      returnResult += globalContext.printLine("}")
    }
    elseBlock.forEachIndexed { index, block ->
      returnResult += block.first + "\n"
      globalContext.updateIndentation(1)
      block.second.forEach {
        returnResult += it.generate()
      }
      globalContext.updateIndentation(-1)
      if (index == elseBlock.size - 1) {
        returnResult += globalContext.printLine("}")
      } else {
        returnResult += globalContext.printNoLine("} ")
      }
    }
    return returnResult
  }
}
