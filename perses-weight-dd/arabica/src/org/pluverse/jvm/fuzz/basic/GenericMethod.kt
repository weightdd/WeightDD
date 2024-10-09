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

class GenericMethod(
  methodClass: JavaClass,
  globalContext: GlobalContext,
  flagAttribute: Int,
  type: JavaType,
  outerCaller: ArrayList<JavaMethod> = ArrayList<JavaMethod>(),
  val inputName: String? = null,
  val overrideArg: ArrayList<Variable> = ArrayList<Variable>(),
  val arrayAttribute: Array.ArrayAttribute? = null,
  val classAttribute: JavaClass? = null,
) : JavaMethod(
  methodClass = methodClass,
  globalContext = globalContext,
  type = type,
  flagAttribute = flagAttribute,
  outerCaller = outerCaller,
) {
  private fun createReturnValue(): ScalarExpression? {
    if (type == JavaType.OBJECT) {
      return ScalarExpression(
        parentStatement = FictiveStatement(
          methodContext,
          parent = null,
          globalContext = globalContext,
        ),
        context = methodContext,
        depth = 0,
        globalContext = globalContext,
        type = JavaType.OBJECT,
        classAttribute = classAttribute,
      )
    }

    if (type == JavaType.ARRAY) {
      return ScalarExpression(
        parentStatement = FictiveStatement(
          methodContext,
          parent = null,
          globalContext = globalContext,
        ),
        context = methodContext,
        depth = 0,
        globalContext = globalContext,
        type = JavaType.ARRAY,
        arrayAttribute = arrayAttribute,
      )
    }
    return null
  }

  private fun generateStatementCount(): Int {
    if (globalContext.randomGenerator.nextInt(101) < Configuration.P_INLINE_METHOD) {
      return globalContext.randomGenerator.nextInt(1)
    } else {
      return Configuration.MAX_STATEMENTS / 2
    }
  }

  override val name = if (inputName != null
  ) {
    inputName
  } else {
    methodClass.classContext.genUniqueName(
      "meth",
      type,
    )
  }

  override val argument = arrayListOf<Variable>()

  init {
    if (classAttribute != null) {
      globalContext.addMethodCaller(this, classAttribute.constructorMethod)
    }
    methodClass.methodList.add(this)
    globalContext.methodCaller[this] = arrayListOf<JavaMethod>(this)
    if (flagCheck(MethodAttribute.OVERRIDE)) {
      overrideArg.forEach {
        argument.add(it)
      }
      Variable(
        context = methodClass.classContext,
        flagAttribute = NameAttribute.PUBLIC.value or NameAttribute.STATIC.value
          or NameAttribute.CHECKSUM.value or NameAttribute.CLASS.value,
        type = JavaType.LONG,
        name = name + "_override_check_sum",
        value = "0",
        globalContext = globalContext,
      )
    } else {
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
    outerCaller.forEach { it ->
      globalContext.addMethodCaller(it, this)
    }
  }

  override var numStatementsGenerate = generateStatementCount()

  override val resultFieldName = methodClass.className + "." + name + "_check_sum"

  val rootStatement = RootStatement(numStatementsGenerate, methodContext, globalContext)

  val returnValue = createReturnValue()

  override fun generateEnding(): String {
    if (type == JavaType.VOID) {
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

    if (type == JavaType.ARRAY || type == JavaType.OBJECT) {
      return returnResult + globalContext.printLine(
        "return " + returnValue!!.generate() + ";",
      )
    }

    if (type == JavaType.BOOLEAN) {
      return returnResult + globalContext.printLine(
        "return meth_res % 2 > 0;",
      )
    }

    return returnResult + globalContext.printLine(
      "return (" + type!!.stringValue + ")meth_res;",
    )
  }

  override fun generate(): String {
    var returnResult = ""
    val staticCheck = if (flagCheck(MethodAttribute.STATIC)) "static " else ""
    if (flagCheck(MethodAttribute.OVERRIDE)) {
      returnResult += globalContext.printLine("@Override")
    }
    if (type == JavaType.OBJECT) {
      require(classAttribute != null)
      returnResult += globalContext.printLine(
        "public " + staticCheck +
          classAttribute.className + " " + name + " " +
          "(" + generateArguments(),
      )
    } else if (type == JavaType.ARRAY) {
      require(arrayAttribute != null)
      returnResult += globalContext.printLine(
        "public " + staticCheck +
          Util.generateArrayIdentifier(arrayAttribute) + " " + name + " " +
          "(" + generateArguments(),
      )
    } else {
      returnResult += globalContext.printLine(
        "public " + staticCheck +
          type!!.stringValue + " " + name + " " +
          "(" + generateArguments(),
      )
    }

    globalContext.updateIndentation(1)
    val ending = generateEnding()
    val statements = rootStatement.generate()
    returnResult += methodContext.generateDeclaration()
    returnResult += statements
    returnResult += ending
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}
