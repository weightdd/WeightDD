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

/** Control flow shouldn't get here if
maximum caller depth is reached, the comment
in the Ruby fuzzer suggests otherwise it seems.
Do more investigation here */
class InvocationExpression(
  parentStatement: JavaStatement,
  context: Context,
  depth: Int = 0,
  globalContext: GlobalContext,
  type: JavaType = Util.generateType(globalContext),
  flag: Int = 0,
  arrayAttribute: Array.ArrayAttribute? = Util.generateArrayAttribute(globalContext, type),
  classAttribute: JavaClass? = Util.generateClassAttribute(globalContext, type, context),
) : JavaExpression(
  parentStatement = parentStatement,
  context = context,
  depth = depth,
  type = type,
  flag = flag,
  globalContext = globalContext,
  arrayAttribute = arrayAttribute,
  classAttribute = classAttribute,
) {
  data class MethodInfo(
    val invocationMethod: JavaMethod?,
    val backUpExpression: JavaExpression?,
    val argument: ArrayList<JavaExpression>,
    val variableName: String?,
  )

  private fun generateMethod(): MethodInfo {
    val generateClass = context.contextClass
    val currentMethod = generateClass.getMethod(
      context.contextMethod,
      type,
      context,
      arrayAttribute,
      classAttribute,
    )
    if (currentMethod == null) {
      if (type == JavaType.VOID) {
        return MethodInfo(
          null,
          AssignmentExpression(
            parentStatement = parentStatement,
            context = context,
            depth = depth,
            globalContext = globalContext,
            flag = flag,
          ),
          ArrayList<JavaExpression>(),
          null,
        )
      }
      return MethodInfo(
        null,
        AssignmentExpression(
          parentStatement,
          context,
          depth,
          globalContext,
          type,
          flag,
          arrayAttribute = arrayAttribute,
          classAttribute = classAttribute,
        ),
        ArrayList<JavaExpression>(),
        null,
      )
    }
    val argumentList = ArrayList<JavaExpression>()
    currentMethod.argument.forEach { arg ->
      argumentList.add(
        JavaExpression.generateExpression(
          parentStatement,
          context,
          depth + 1,
          globalContext,
          arg.type,
          ExpressionAttribute.CAST.value,
          arrayAttribute,
          classAttribute,
        ),
      )
    }
    if (currentMethod.methodClass != context.contextClass &&
      !currentMethod.flagCheck(MethodAttribute.STATIC) &&
      context.contextClass.extendClass != currentMethod.methodClass
    ) {
      val variable = context.getObject(
        Configuration.P_VAR_REUSE,
        false,
        true,
        currentMethod.methodClass,
      )
      // If using non-static field of another class, invoc
      if (!variable.flagCheck(NameAttribute.STATIC)) {
        globalContext.addMethodCaller(
          context.contextMethod!!,
          currentMethod.methodClass.constructorMethod,
        )
      }
      return MethodInfo(
        currentMethod,
        null,
        argumentList,
        variable.generateName(),
      )
    }
    return MethodInfo(
      currentMethod,
      null,
      argumentList,
      null,
    )
  }

  val currentInfo = generateMethod()

  override fun generate(): String {
    if (currentInfo.invocationMethod == null) {
      if (flagCheck(ExpressionAttribute.CAST)) {
        return "(" + type.stringValue + ") " + currentInfo.backUpExpression!!.generate()
      }
      return currentInfo.backUpExpression!!.generate()
    }
    var prefix = ""

    if (currentInfo.invocationMethod.flagCheck(MethodAttribute.STATIC)) {
      if (context.contextClass.extendClass == currentInfo.invocationMethod.methodClass) {
        prefix += context.contextClass.className + "."
      } else {
        prefix += currentInfo.invocationMethod.methodClass.className + "."
      }
    } else if (currentInfo.variableName != null) {
      prefix += currentInfo.variableName + "."
    }

    var returnResult = prefix + currentInfo.invocationMethod.name + "("
    currentInfo.argument.forEachIndexed { index, arg ->
      if (index == currentInfo.argument.size - 1) {
        returnResult += arg.generate()
      } else {
        returnResult += arg.generate() + ", "
      }
    }
    if (flagCheck(ExpressionAttribute.CAST)) {
      returnResult = "(" + type.stringValue + ") " + returnResult
    }
    return returnResult + ")"
  }
}
