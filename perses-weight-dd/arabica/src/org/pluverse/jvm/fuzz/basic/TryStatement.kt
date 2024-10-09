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

class TryStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {

  init {
    // parent should not be null based on generation strategy
    require(parent != null)
  }

  val chosenException = Configuration.P_EXCEPTION_TYPE.getRandom(
    subset = null,
    exclude = null,
    globalContext.randomGenerator,
  )

  private val name = context.genUniqueName("var", JavaType.EXCEPTION, isArray = false)

  private fun generateTryBlock(): ArrayList<Pair<String, ArrayList<JavaStatement>>> {
    var outputBlock = arrayListOf<Pair<String, ArrayList<JavaStatement>>>()
    outputBlock.add(
      Pair(
        "try {",
        JavaStatement.generateStatementSequence(
          currentStatement = this,
          context,
          globalContext,
          Configuration.MAX_TRY_STATEMENT,
        ),
      ),
    )

    outputBlock.add(
      Pair(
        "} catch (" + chosenException.stringValue + " " +
          name + ") {",
        JavaStatement.generateStatementSequence(
          currentStatement = parent!!,
          context,
          globalContext,
          Configuration.MAX_TRY_STATEMENT,
        ),
      ),
    )

    if (globalContext.randomGenerator.nextInt(101) >
      Configuration.P_FINALLY
    ) {
      return outputBlock
    }

    outputBlock.add(
      Pair(
        "} finally {",
        JavaStatement.generateStatementSequence(
          currentStatement = parent,
          context,
          globalContext,
          Configuration.MAX_TRY_STATEMENT,
        ),
      ),
    )
    return outputBlock
  }

  private val outputBlock = generateTryBlock()

  override fun generate(): String {
    var returnResult = ""
    outputBlock.forEach { block ->
      returnResult += globalContext.printLine(block.first)
      globalContext.updateIndentation(1)
      block.second.forEach {
        returnResult += it.generate()
      }
      if (block.first == "try {" &&
        globalContext.randomGenerator.nextInt(101) < Configuration.P_THROW_EXCEPTION
      ) {
        returnResult += globalContext.printLine(
          "throw new " + chosenException.stringValue + "(\"Error\");",
        )
      }
      globalContext.updateIndentation(-1)
    }
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
