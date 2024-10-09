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

class InvokeMethod(
  methodClass: MainClass,
  globalContext: GlobalContext,
) : JavaMethod(
  methodClass = methodClass,
  globalContext = globalContext,
  type = JavaType.STRING,
  flagAttribute = MethodAttribute.STATIC.value,
) {
  init {
    globalContext.methodCaller[this] = arrayListOf<JavaMethod>(this)
  }
  override val name = "invokeMethod"
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
  override fun generate(): String {
    var returnResult = globalContext.printLine(
      "public static " + type!!.stringValue + " " + name + " " +
        "(" + generateArguments(),
    ) + "\n"
    globalContext.updateIndentation(1)
    returnResult += globalContext.printLine(
      "String returnResult = \"\";",
    )
    returnResult += globalContext.printLine(
      methodClass.className +
        " _instance = new " + methodClass.className + "();",
    )
    returnResult += globalContext.printLine(
      "for (int i = 0; i < " +
        Configuration.MAIN_TEST_CALL_NUMBER.toString() + "; i++) {",
    )
    globalContext.updateIndentation(1)
    returnResult += globalContext.printLine(
      "returnResult +=  _instance." +
        (methodClass as MainClass).mainTestMethod.name + "(" + argument[0].name + ");",
    )
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    returnResult += globalContext.printLine("return returnResult;")
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
