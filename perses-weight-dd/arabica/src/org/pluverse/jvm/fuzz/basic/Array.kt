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
import org.pluverse.jvm.fuzz.util.JavaType

class Array(
  context: Context,
  type: JavaType?,
  flagAttribute: Int,
  name: String? = null,
  dimension: Int,
  globalContext: GlobalContext,
  val size: Int,
) : Variable(
  context,
  flagAttribute,
  type,
  name,
  isArray = true,
  globalContext = globalContext,
) {
  fun generateDimension(dimension: Int): Int {
    if (dimension == 0 && size == 0) {
      val randomIndex = globalContext.randomGenerator.nextInt(4)
      return listOf(
        1,
        1,
        1,
        globalContext.randomGenerator.nextInt(Configuration.MAX_ARRAY_DIMENSION) + 1,
      )[randomIndex]
    }
    if (dimension == 0) {
      return 1
    }
    return dimension
  }

  val dimension = generateDimension(dimension)

  override fun generateDeclaration(): String {
    var declarationStatement = name
    for (i in 0..dimension - 1) {
      declarationStatement += "[]"
    }

    if (flagCheck(NameAttribute.NULL)) {
      return declarationStatement + "= null"
    }
    declarationStatement += " = new " + type.stringValue
    for (i in 0..dimension - 1) {
      declarationStatement = declarationStatement + "[" + size.toString() + "]"
    }
    return declarationStatement
  }

  fun generateInitialization(): String {
    var outputInitialization = ""
    if (flagCheck(NameAttribute.NULL)) {
      return outputInitialization
    }

    outputInitialization += FuzzerConstants.SUPER + "." +
      FuzzerConstants.INIT + "(" + generateName() + ", "

    if ((type == JavaType.SHORT) || (type == JavaType.CHAR) || (type == JavaType.BYTE)) {
      return outputInitialization + "(" + type.stringValue + ") " + Util.rightLiteral(
        type,
        globalContext.randomGenerator,
      ) + ")"
    }
    return outputInitialization + Util.rightLiteral(type, globalContext.randomGenerator) + ")"
  }

  fun getArrayAttribute(): ArrayAttribute {
    return ArrayAttribute(type, dimension, size)
  }

  override fun generateCheckSum(): String {
    if (flagCheck(NameAttribute.NULL)) {
      return "0"
    }
    val checkSum = FuzzerConstants.SUPER + "." + FuzzerConstants.CHECK_SUM + "(" + name + ")"
    if ((type == JavaType.FLOAT) || (type == JavaType.DOUBLE)) {
      return "Double.doubleToLongBits(" + checkSum + ")"
    }
    return checkSum
  }

  override val initialValue = generateInitialization()

  override fun resetStaticValue(): String {
    var returnResult = ""
    var declarationStatement = name
    if (flagCheck(NameAttribute.NULL)) {
      return globalContext.printLine(name + "= null")
    }
    declarationStatement += " = new " + type.stringValue
    for (i in 0..dimension - 1) {
      declarationStatement = declarationStatement + "[" + size.toString() + "]"
    }
    returnResult += globalContext.printLine(declarationStatement + ";")
    returnResult += globalContext.printLine(initialValue + ";")
    return returnResult
  }

  data class ArrayAttribute(
    val arrayType: JavaType,
    val dimension: Int = 0,
    val size: Int = 0,
  )
}
