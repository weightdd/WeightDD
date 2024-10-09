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

class MainTest(
  methodClass: MainClass,
  globalContext: GlobalContext,
) : JavaMethod(
  methodClass = methodClass,
  globalContext = globalContext,
  type = JavaType.STRING,
  flagAttribute = 0,
) {
  init {
    globalContext.methodCaller[this] = arrayListOf<JavaMethod>(this)
    // Implement method caller
  }
  override var numStatementsGenerate = Configuration.MAX_STATEMENTS / 2
  override val name = "mainTest"
  override val argument = arrayListOf<Variable>(
    Array(
      methodContext,
      JavaType.STRING,
      NameAttribute.ARGUMENT.value,
      null,
      1,
      globalContext,
      0,
    ),
  )
  val rootStatement = RootStatement(numStatementsGenerate, methodContext, globalContext)
  override fun generate(): String {
    var returnResult = globalContext.printLine(
      "public " + type!!.stringValue + " " + name + " " +
        "(" + generateArguments(),
    ) + "\n"
    globalContext.updateIndentation(1)
    returnResult += methodContext.generateDeclaration()
    returnResult += rootStatement.generate()
    returnResult += globalContext.printLine("String returnResult = \"\";")
    returnResult += methodContext.generateResultPrint()
    returnResult += methodClass.classContext.generateResultPrint()
    returnResult += globalContext.printLine("return returnResult;")
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
