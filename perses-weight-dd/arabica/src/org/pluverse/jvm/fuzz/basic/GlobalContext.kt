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
import org.pluverse.jvm.fuzz.util.FuzzerUtilStubTemplate
import java.util.LinkedList
import java.util.Queue
import java.util.Random

class GlobalContext(
  val seed: Long,
) {
  val randomGenerator = Random(seed)
  var contextTracker = 1
  var indentationLevel = 0
  var classCounter = 0
  val kind = ContextAttribute.GLOBAL
  val variableMapping = HashMap<String, Int>()
  val methodCaller = HashMap<JavaMethod, ArrayList<JavaMethod>>()
  val classList = ArrayList<JavaClass>()
  val generateQueue: Queue<JavaClass> = LinkedList<JavaClass>()
  val mainClass = MainClass(
    globalContext = this,
  )
  var mainMethod = mainClass.mainTestMethod
  val utilTemplate = FuzzerUtilStubTemplate(seed)
  fun registerClass() {
    classCounter += 1
  }

  fun updateIndentation(indentation: Int) {
    indentationLevel += indentation
  }

  fun printLine(output: String): String {
    return " ".repeat(FuzzerConstants.TAB_NUMBER).repeat(indentationLevel) +
      output + "\n"
  }

  fun printNoLine(output: String): String {
    return " ".repeat(FuzzerConstants.TAB_NUMBER).repeat(indentationLevel) +
      output
  }

  fun addMethodCaller(
    caller: JavaMethod,
    callee: JavaMethod,
  ) {
    if (caller == callee || isCaller(callee, caller)) {
      throw RuntimeException("Trying to cycle a call chain")
    }
    if (!methodCaller.containsKey(callee)) {
      methodCaller[callee] = arrayListOf<JavaMethod>()
    }
    if (!methodCaller[callee]!!.contains(caller)) {
      methodCaller[callee]!!.add(caller)
    }

    for ((currentCallee, currentCallers) in methodCaller.entries) {
      if (currentCallers.contains(callee) && !currentCallers.contains(caller)) {
        addMethodCaller(caller, currentCallee)
      }
    }

    if (methodCaller.containsKey(caller)) {
      methodCaller[caller]!!.forEach {
        if (!methodCaller[callee]!!.contains(it)) {
          addMethodCaller(it, callee)
        }
      }
    }
  }

  fun getCallerHashDepth(callee: JavaMethod? = null): Int {
    var currentMax = 0
    if (callee == null) {
      for (currentCallee in methodCaller.keys) {
        val currentDepth = getCallerHashDepth(currentCallee)
        if (currentDepth > currentMax) {
          currentMax = currentDepth
        }
      }
      return currentMax
    } else {
      if (!methodCaller.containsKey(callee) || methodCaller[callee]!!.size <= 1) {
        return 0
      }
      methodCaller[callee]!!.forEach { caller ->
        if (caller != callee) {
          val currentDepth = getCallerHashDepth(caller)
          if (currentDepth > currentMax) {
            currentMax = currentDepth
          }
        }
      }
      return currentMax + 1
    }
  }

  fun isCaller(
    caller: JavaMethod,
    callee: JavaMethod,
  ): Boolean {
    if (!methodCaller.containsKey(callee) && caller != callee) {
      return false
    }
    return methodCaller[callee]!!.contains(caller) || callee == caller
  }

  fun classListForSelection(
    methodList: ArrayList<JavaMethod>,
  ): ArrayList<JavaClass> {
    return ArrayList(
      classList.filter { currentClass ->
        !methodList.any {
          isCaller(
            currentClass.constructorMethod,
            it,
          )
        }
      },
    )
  }

  fun getClass(
    context: Context,
    methodList: ArrayList<JavaMethod> = ArrayList<JavaMethod>(),
  ): JavaClass {
    methodList.add(context.recurseContextMethod())
    if (randomGenerator.nextInt(101)
      < Configuration.P_CLASS_REUSE || classCounter >= Configuration.MAX_CLASSES
    ) {
      val validArray = classListForSelection(methodList)
      if (validArray.size > 0) {
        return validArray[randomGenerator.nextInt(validArray.size)]
      }
      return GenericClass(methodList, this)
    }
    if (randomGenerator.nextInt(101) < Configuration.P_EXTENDS_CLASS) {
      val validArray = classListForSelection(methodList)
      if (validArray.size > 0) {
        return GenericClass(
          methodList,
          this,
          validArray[randomGenerator.nextInt(validArray.size)],
        )
      }
    }
    return GenericClass(methodList, this)
  }

  fun generate(): String {
    var outputResult = utilTemplate.generateCode()
    while (generateQueue.peek() != null) {
      generateQueue.remove().constructorMethod.instantiateClass()
    }
    classList.forEach {
      outputResult += it.generate()
    }
    return outputResult
  }
}
