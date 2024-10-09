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
import org.pluverse.jvm.fuzz.util.ExceptionType
import org.pluverse.jvm.fuzz.util.JavaType

/** Scalar expression: Wrapper for variables during generation */
class ScalarExpression(
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
  var objectName: String? = null
  val scalarVariable = generateScalar()

  private fun generateScalar(): Variable {
    val returnScalar: Variable
    if (type == JavaType.OBJECT) {
      if (parentStatement.findParentTryStatement(ExceptionType.NULL_POINTER_EXCEPTION)) {
        returnScalar = context.getObject(
          Configuration.P_VAR_REUSE,
          flagCheck(ExpressionAttribute.DESTINATION),
          false,
          classAttribute!!,
        )
      } else {
        returnScalar = context.getObject(
          Configuration.P_VAR_REUSE,
          flagCheck(ExpressionAttribute.DESTINATION),
          true,
          classAttribute!!,
        )
      }
    } else if (type == JavaType.ARRAY) {
      returnScalar = context.getArray(
        Configuration.P_VAR_REUSE,
        arrayAttribute!!.arrayType,
        arrayAttribute.dimension,
        arrayAttribute.size,
        flagCheck(ExpressionAttribute.NOT_NULL),
      )
    } else {
      returnScalar = context.getVariable(
        Configuration.P_VAR_REUSE,
        type,
        flagCheck(ExpressionAttribute.DESTINATION),
        flagCheck(ExpressionAttribute.NOT_NULL),
      )
    }

    // If using non-static field of another class, invoc
    if (returnScalar.context.contextClass != context.contextClass &&
      !returnScalar.flagCheck(NameAttribute.STATIC) &&
      context.contextClass.extendClass != returnScalar.context.contextClass
    ) {
      val invocObject: JavaObject
      if (parentStatement.findParentTryStatement(ExceptionType.NULL_POINTER_EXCEPTION)) {
        invocObject = context.getObject(
          reuseProb = Configuration.P_VAR_REUSE,
          destination = false,
          notNull = false,
          classAttribute = returnScalar.context.contextClass,
        )
      } else {
        invocObject = context.getObject(
          reuseProb = Configuration.P_VAR_REUSE,
          destination = false,
          notNull = true,
          classAttribute = returnScalar.context.contextClass,
        )
      }
      objectName = invocObject.generateName()
    }
    return returnScalar
  }

  fun generateArrayAttribute(): Array.ArrayAttribute? {
    if (scalarVariable is Array) {
      return scalarVariable.getArrayAttribute()
    }
    return null
  }

  override fun generate(): String {
    var returnValue = ""
    if (flagCheck(ExpressionAttribute.CAST)) {
      returnValue += "(" + type.stringValue + ")"
    }
    if (objectName != null) {
      return returnValue + objectName + "." + scalarVariable.generateName()
    }
    return returnValue + scalarVariable.generateName()
  }
}
