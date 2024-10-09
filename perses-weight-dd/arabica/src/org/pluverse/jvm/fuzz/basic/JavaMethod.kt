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

abstract class JavaMethod(
  val methodClass: JavaClass,
  var type: JavaType?,
  val outerCaller: ArrayList<JavaMethod> = ArrayList<JavaMethod>(),
  val innerCallee: ArrayList<JavaMethod> = ArrayList<JavaMethod>(),
  val globalContext: GlobalContext,
  var flagAttribute: Int = 0,
) {
  fun setFlag(flagInput: Int) {
    flagAttribute = flagInput
  }

  fun flagCheck(flags: MethodAttribute): Boolean {
    return flagAttribute and flags.value > 0
  }

  fun generateMethodCheckSum(): String {
    val checkSum = arrayListOf<String>()

    (methodContext.variableList + methodContext.arrayList).forEach {
      checkSum.add(it.generateCheckSum())
    }
    if (checkSum.isNullOrEmpty()) {
      return "0"
    }
    return checkSum.joinToString(separator = " + ")
  }

  open fun generateEnding(): String {
    if (type == JavaType.VOID || this is Constructor) {
      return globalContext.printLine(
        resultFieldName + "+= " +
          generateMethodCheckSum() + ";",
      )
    }
    var returnResult = globalContext.printLine(
      "long meth_res = " +
        generateMethodCheckSum() + ";",
    )

    returnResult += globalContext.printLine(
      resultFieldName + " += meth_res;",
    )

    if (type == JavaType.BOOLEAN) {
      return returnResult + globalContext.printLine(
        "return meth_res % 2 > 0;",
      )
    }

    return returnResult + globalContext.printLine(
      "return (" + type!!.stringValue + ")meth_res;",
    )
  }

  val methodContext = Context(
    parent = methodClass.classContext,
    kind = ContextAttribute.METHOD,
    globalContext = globalContext,
    contextMethod = this,
    contextClass = methodClass,
  )

  fun generateArguments(): String {
    val returnResult = argument.map {
      var returnResult = ""
      if (it is JavaObject) {
        returnResult += it.classAttribute
      } else {
        returnResult += it.type.stringValue
      }

      if (it is Array) {
        returnResult += "[]".repeat(it.dimension)
      }

      return@map returnResult + " " + it.name
    }.joinToString(", ")
    return returnResult + ") {"
  }
  open val resultFieldName = ""
  open var numStatementsGenerate = 0
  abstract val argument: ArrayList<Variable>
  abstract val name: String
  abstract fun generate(): String
}
