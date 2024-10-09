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

import com.google.common.collect.ImmutableSet
import org.pluverse.jvm.fuzz.util.Configuration
import org.pluverse.jvm.fuzz.util.JavaType

class Constructor(
  methodClass: JavaClass,
  outerCaller: ArrayList<JavaMethod>,
  globalContext: GlobalContext,
  flagAttribute: Int,
) : JavaMethod(
  methodClass = methodClass,
  type = null,
  outerCaller = outerCaller,
  globalContext = globalContext,
  flagAttribute = flagAttribute,
) {
  override var numStatementsGenerate = Configuration.MAX_STATEMENTS / 2
  override val name = methodClass.className
  override val argument = ArrayList<Variable>()
  var rootStatement: JavaStatement? = null

  init {
    methodClass.classContext.contextMethod = this
    globalContext.methodCaller[this] = arrayListOf<JavaMethod>(this)
    outerCaller.forEach {
      globalContext.addMethodCaller(it, this)
    }

    if (!flagCheck(MethodAttribute.FICTIVE)) {
      repeat(globalContext.randomGenerator.nextInt(Configuration.MAX_ARGUMENT + 1)) {
        val argumentType = Configuration.TYPES.getRandom(
          null,
          ImmutableSet.of(JavaType.ARRAY, JavaType.OBJECT),
          globalContext.randomGenerator,
        )
        val currentVariable = Variable(
          context = methodContext,
          flagAttribute = NameAttribute.ARGUMENT.value or NameAttribute.LOCAL.value,
          type = argumentType,
          name = null,
          globalContext = globalContext,
        )
        argument.add(currentVariable)
      }
    }

    Variable(
      context = methodClass.classContext,
      flagAttribute = NameAttribute.PUBLIC.value or NameAttribute.STATIC.value
        or NameAttribute.CHECKSUM.value or NameAttribute.CLASS.value,
      type = JavaType.LONG,
      name = name + "_check_sum",
      value = "0",
      globalContext = globalContext,
    )
  }

  fun instantiateClass() {
    rootStatement = if (flagCheck(MethodAttribute.STATIC)) {
      RootStatement(
        0,
        methodContext,
        globalContext,
      )
    } else {
      RootStatement(numStatementsGenerate, methodContext, globalContext)
    }
  }

  fun generateSuper(extendClass: JavaClass): String {
    var returnResult = "super("
    val argumentList = ArrayList<JavaExpression>()
    methodContext.withinConstructorCall = true
    extendClass.constructorMethod.argument.forEach { arg ->
      argumentList.add(
        LiteralExpression(
          FictiveStatement(methodContext, null, globalContext),
          methodContext,
          1,
          globalContext,
          arg.type,
          ExpressionAttribute.CAST.value,
        ),
      )
    }
    methodContext.withinConstructorCall = false
    argumentList.forEachIndexed { index, arg ->
      if (index == argumentList.size - 1) {
        returnResult += arg.generate()
      } else {
        returnResult += arg.generate() + ", "
      }
    }
    return globalContext.printLine(returnResult + ");")
  }

  override fun generate(): String {
    if (flagCheck(MethodAttribute.FICTIVE)) {
      return ""
    }
    var returnResult = globalContext.printLine(
      "public " + name +
        "(" + generateArguments(),
    ) + "\n"
    globalContext.updateIndentation(1)
    if (methodClass is GenericClass &&
      methodClass.extendClass != null
    ) {
      returnResult += generateSuper(methodClass.extendClass)
    }
    returnResult += methodContext.generateDeclaration()
    returnResult += rootStatement!!.generate()
    returnResult += generateEnding()
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }

  override val resultFieldName = name + "_check_sum"
}
