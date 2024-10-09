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
import org.pluverse.jvm.fuzz.util.FuzzerConstants

class MainClass(
  globalContext: GlobalContext,
  extendClass: JavaClass? = null,
) : JavaClass(
  globalContext = globalContext,
  extendClass = extendClass,
) {
  override val className = Configuration.MAIN_CLASS_NAME

  init {
    globalContext.registerClass()
  }

  override val constructorMethod = Constructor(
    this,
    outerCaller = arrayListOf<JavaMethod>(),
    globalContext = globalContext,
    flagAttribute = MethodAttribute.FICTIVE.value,
  )

  init {
    globalContext.classList.add(this)
  }

  val mainMethod = MainMethod(
    methodClass = this,
    outerCaller = arrayListOf<JavaMethod>(),
    globalContext = globalContext,
  )

  val mainTestMethod = MainTest(
    methodClass = this,
    globalContext = globalContext,
  )

  val invokeMethod = InvokeMethod(
    methodClass = this,
    globalContext = globalContext,
  )

  /* If we are using the stratgy of resetting program
  state after each repetition, we must load all classes
  from the start to have all static variable call constructors
  and avoid drifts in output. */
  private fun loadClass(): String {
    var returnResult = globalContext.printLine(
      "public static void loadClass() {",
    )
    globalContext.updateIndentation(1)
    globalContext.classList.forEach {
      if (it != this) {
        returnResult += globalContext.printLine(
          it.className + "." + it.className + "_check_sum = 0;",
        )
      }
    }
    returnResult += globalContext.printLine("resetStatic();")
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }

  override fun generate(): String {
    var returnResult = ""
    returnResult += globalContext.printLine(
      "public class " + className + " {",
    ) + "\n"
    globalContext.updateIndentation(1)
    returnResult += globalContext.printLine(
      "public static final int " +
        FuzzerConstants.MAX_TRIPNM + " = " + Configuration.MAX_SIZE.toString() + ";",
    ) + "\n"
    var methodResult = ""

    methodList.forEach { method ->
      methodResult += method.generate() + "\n"
    }
    methodResult += generateClassCheckSum()
    methodResult += mainTestMethod.generate() + "\n"

    returnResult += classContext.generateDeclaration()

    returnResult += methodResult
    returnResult += loadClass()
    returnResult += resetMethod.generate()
    returnResult += invokeMethod.generate()
    returnResult += mainMethod.generate()
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
