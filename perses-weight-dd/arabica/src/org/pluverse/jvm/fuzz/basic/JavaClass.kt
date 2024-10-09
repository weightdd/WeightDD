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
import org.pluverse.jvm.fuzz.util.MethodType

abstract class JavaClass(
  val globalContext: GlobalContext,
  val extendClass: JavaClass?,
) {
  val classContext = Context(
    parent = null,
    kind = ContextAttribute.CLASS,
    globalContext = globalContext,
    contextMethod = null,
    contextClass = this,
  )

  val resetMethod = ResetMethod(
    methodClass = this,
    globalContext = globalContext,
  )
  var methodNumber = 0
  abstract val className: String
  abstract val constructorMethod: Constructor
  val methodList = ArrayList<JavaMethod>()

  fun generateClassCheckSum(): String {
    var returnResult = ""
    returnResult += globalContext.printLine("public int checkSum() {")
    globalContext.updateIndentation(1)
    returnResult += globalContext.printLine("int checkSum = 0;")
    classContext.variableList.forEach {
      returnResult += globalContext.printLine("checkSum += " + it.generateCheckSum() + ";")
    }
    classContext.arrayList.forEach {
      returnResult += globalContext.printLine("checkSum += " + it.generateCheckSum() + ";")
    }
    classContext.objectList.forEach {
      if (it.flagCheck(NameAttribute.NOTNULL)) {
        returnResult += globalContext.printLine("checkSum += " + it.generateCheckSum() + ";")
      }
    }
    returnResult += globalContext.printLine("return checkSum;")
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }

  fun findOverrideMethod(
    callerMethod: JavaMethod,
    type: JavaType,
    arrayAttribute: Array.ArrayAttribute?,
    classAttribute: JavaClass?,
  ): JavaMethod? {
    require(extendClass != null)
    val validMethod = arrayListOf<JavaMethod>()
    extendClass.methodList.forEach {
      if (
        it.type == type &&
        !globalContext.isCaller(it, callerMethod) &&
        (it is GenericMethod) &&
        !globalContext.isCaller(it.methodClass.constructorMethod, callerMethod) &&
        !it.flagCheck(MethodAttribute.STATIC) &&
        it.arrayAttribute == arrayAttribute &&
        it.classAttribute == classAttribute
      ) {
        validMethod.add(it)
      }
    }
    if (validMethod.size == 0) {
      return null
    }
    val chosenMethod = validMethod[globalContext.randomGenerator.nextInt(validMethod.size)]

    methodList.forEach {
      if (it.name == chosenMethod.name) {
        return it
      }
    }

    return GenericMethod(
      methodClass = this,
      globalContext = globalContext,
      flagAttribute = MethodAttribute.OVERRIDE.value,
      type = type,
      outerCaller = arrayListOf<JavaMethod>(callerMethod),
      inputName = chosenMethod.name,
      overrideArg = chosenMethod.argument,
      arrayAttribute = arrayAttribute,
      classAttribute = classAttribute,
    )
  }

  /** Returns a method instance for invocation,
   if null is returned then we cannot generate a new method
   due to the callerHashDepth */
  fun getMethod(
    callerMethod: JavaMethod?,
    type: JavaType,
    callContext: Context,
    arrayAttribute: Array.ArrayAttribute?,
    classAttribute: JavaClass?,
  ): JavaMethod? {
    if (callerMethod == null ||
      globalContext.getCallerHashDepth(callerMethod) >= Configuration.MAX_CALLER_CHAIN
    ) {
      return null
    }

    if (globalContext.randomGenerator.nextInt(101) < Configuration.P_METHOD_REUSE) {
      if (globalContext.randomGenerator.nextInt(101) < Configuration.P_METHOD_OVERRIDE &&
        extendClass != null && !callerMethod.flagCheck(MethodAttribute.STATIC)
      ) {
        val validCandidate = findOverrideMethod(callerMethod, type, arrayAttribute, classAttribute)
        if (validCandidate != null) {
          return validCandidate
        }
      }
      val validMethod = arrayListOf<JavaMethod>()
      globalContext.classList.forEach { currentClass ->
        currentClass.methodList.forEach {
          if (
            it.type == type &&
            !globalContext.isCaller(it, callerMethod) &&
            (it is GenericMethod) && (
              it.flagCheck(MethodAttribute.STATIC) ||
                !globalContext.isCaller(it.methodClass.constructorMethod, callerMethod)
              ) &&
            (
              it.flagCheck(MethodAttribute.STATIC) ||
                !callerMethod.flagCheck(MethodAttribute.STATIC)
              ) && it.arrayAttribute == arrayAttribute &&
            it.classAttribute == classAttribute
          ) {
            validMethod.add(it)
          }
        }
      }

      if (validMethod.size > 0) {
        val chosenMethod = validMethod[globalContext.randomGenerator.nextInt(validMethod.size)]
        globalContext.addMethodCaller(callerMethod, chosenMethod)
        return chosenMethod
      }
    }

    if (methodList.size <= Configuration.MAX_METHOD) {
      val currentMethod = callerMethod
      val validClass = classContext.checkValidClass(currentMethod)
      val excludeTypes = mutableSetOf<MethodType>()
      if (validClass == null) {
        excludeTypes.add(MethodType.NON_STATIC_OTHER)
        excludeTypes.add(MethodType.STATIC_OTHER)
      }
      if (currentMethod.flagCheck(MethodAttribute.STATIC)) {
        excludeTypes.add(MethodType.SAME_CLASS_NON_STATIC)
        excludeTypes.add(MethodType.NON_STATIC_OTHER)
      }
      if (callContext.withinConstructorCall) {
        excludeTypes.add(MethodType.NON_STATIC_OTHER)
      }
      val methodType = Configuration.METHOD_TYPES.getRandom(
        null,
        excludeTypes,
        globalContext.randomGenerator,
      )
      when (methodType) {
        MethodType.SAME_CLASS_STATIC -> {
          return GenericMethod(
            methodClass = this,
            globalContext = globalContext,
            type = type,
            flagAttribute = MethodAttribute.STATIC.value,
            outerCaller = arrayListOf<JavaMethod>(callerMethod),
            arrayAttribute = arrayAttribute,
            classAttribute = classAttribute,
          )
        }
        MethodType.SAME_CLASS_NON_STATIC -> {
          return GenericMethod(
            methodClass = this,
            globalContext = globalContext,
            type = type,
            flagAttribute = 0,
            outerCaller = arrayListOf<JavaMethod>(callerMethod),
            arrayAttribute = arrayAttribute,
            classAttribute = classAttribute,
          )
        }
        MethodType.STATIC_OTHER -> {
          if (!globalContext.isCaller(validClass!!.constructorMethod, currentMethod)) {
            globalContext.addMethodCaller(currentMethod, validClass.constructorMethod)
          } else {
            globalContext.addMethodCaller(constructorMethod, validClass.constructorMethod)
          }
          return GenericMethod(
            methodClass = validClass,
            globalContext = globalContext,
            type = type,
            flagAttribute = MethodAttribute.STATIC.value,
            outerCaller = arrayListOf<JavaMethod>(callerMethod),
            arrayAttribute = arrayAttribute,
            classAttribute = classAttribute,
          )
        }
        MethodType.NON_STATIC_OTHER -> {
          if (!globalContext.isCaller(validClass!!.constructorMethod, currentMethod)) {
            globalContext.addMethodCaller(currentMethod, validClass.constructorMethod)
          } else {
            globalContext.addMethodCaller(constructorMethod, validClass.constructorMethod)
          }
          return GenericMethod(
            methodClass = validClass,
            globalContext = globalContext,
            type = type,
            flagAttribute = 0,
            outerCaller = arrayListOf<JavaMethod>(callerMethod),
            arrayAttribute = arrayAttribute,
            classAttribute = classAttribute,
          )
        }
      }
    }
    return null
  }

  abstract fun generate(): String
}
