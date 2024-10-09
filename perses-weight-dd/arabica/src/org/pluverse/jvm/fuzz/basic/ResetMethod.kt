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

class ResetMethod(
  methodClass: JavaClass,
  globalContext: GlobalContext,
) : JavaMethod(
  methodClass = methodClass,
  globalContext = globalContext,
  type = JavaType.VOID,
  flagAttribute = MethodAttribute.STATIC.value,
) {
  init {
    globalContext.methodCaller[this] = arrayListOf<JavaMethod>(this)
  }

  /* Reset non objects first, can consider refactoring this */
  private fun resetObject(): String {
    var returnResult = globalContext.printLine(
      "public static void resetObject () {",
    )
    globalContext.updateIndentation(1)
    (methodClass.classContext.objectList)
      .forEach { variable ->
        if (variable.flagCheck(NameAttribute.STATIC)) {
          returnResult += variable.resetStaticValue()
        }
      }
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }

  override val name = "resetStatic"
  override val argument = arrayListOf<Variable>()
  override fun generate(): String {
    var returnResult = resetObject()

    returnResult += globalContext.printLine(
      "public static " + type!!.stringValue + " " + name + " " +
        "(" + generateArguments(),
    )
    globalContext.updateIndentation(1)
    (
      methodClass.classContext.arrayList + methodClass.classContext.variableList
      ).forEach { variable ->
      if (variable.flagCheck(NameAttribute.STATIC) ||
        variable.flagCheck(NameAttribute.CHECKSUM) || variable.flagCheck(NameAttribute.INDUCTION)
      ) {
        returnResult += variable.resetStaticValue()
      }
    }
    if (methodClass is MainClass) {
      globalContext.classList.forEach {
        if (it != methodClass) {
          returnResult += globalContext.printLine(it.className + ".resetStatic();")
        }
      }
      globalContext.classList.forEach {
        if (it != methodClass) {
          returnResult += globalContext.printLine(it.className + ".resetObject();")
        } else {
          returnResult += globalContext.printLine("resetObject();")
        }
      }
    }
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
