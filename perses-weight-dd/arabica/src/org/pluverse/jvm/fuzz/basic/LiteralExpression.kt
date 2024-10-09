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

/** Literal expression: wrapper by rLiteral value during generation*/
class LiteralExpression(
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
  private fun generateLiteral(type: JavaType?): String {
    var generateType = type
    if (type == null) {
      generateType = Configuration.TYPES.getRandom(
        null,
        ImmutableSet.of(JavaType.OBJECT),
        globalContext.randomGenerator,
      )
    }

    if (generateType == JavaType.ARRAY) {
      require(arrayAttribute != null)
      return Util.rightLiteralArray(
        arrayAttribute.arrayType,
        arrayAttribute.size,
        arrayAttribute.dimension,
        globalContext.randomGenerator,
      )
    }
    if (generateType == JavaType.OBJECT) {
      require(classAttribute != null)
      return Util.rightLiteralObject(globalContext, context, classAttribute)
    }
    return Util.rightLiteral(generateType!!, globalContext.randomGenerator)
  }
  var rightLiteral = generateLiteral(type)

  constructor(
    parentStatement: JavaStatement,
    context: Context,
    depth: Int,
    globalContext: GlobalContext,
    type: JavaType,
    inputLiteral: String,
  ) : this(
    parentStatement,
    context,
    depth,
    globalContext,
    type,
  ) {
    rightLiteral = inputLiteral
  }

  fun faultyLiteral(inputType: JavaType): Boolean {
    var castValue: Int
    if (type == JavaType.DOUBLE) {
      castValue = rightLiteral.substring(
        0,
        rightLiteral.length - 1,
      ).toDouble().toInt()
    } else if (type == JavaType.FLOAT) {
      castValue = rightLiteral.toFloat().toInt()
    } else if (type == JavaType.LONG) {
      castValue = rightLiteral.substring(0, rightLiteral.length - 1).toLong().toInt()
    } else {
      castValue = rightLiteral.toInt()
    }

    when (inputType) {
      JavaType.BYTE -> {
        return castValue.toByte() == 0.toByte()
      }
      JavaType.CHAR -> {
        return castValue.toChar() == 0.toChar()
      }
      JavaType.SHORT -> {
        return castValue.toShort() == 0.toShort()
      }
      else -> {
        return castValue == 0
      }
    }
  }

  override val resType = if (type == JavaType.SHORT || type == JavaType.BYTE) JavaType.INT else type

  override fun generate(): String {
    if (flagCheck(ExpressionAttribute.CAST)) {
      return "(" + type.stringValue + ") " + rightLiteral
    }
    return rightLiteral
  }
}
