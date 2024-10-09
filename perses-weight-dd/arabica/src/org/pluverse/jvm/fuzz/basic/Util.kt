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
import java.util.Random

object Util {
  @JvmStatic
  fun rightLiteralObject(
    globalContext: GlobalContext,
    context: Context,
    classAttribute: JavaClass,
    static: Boolean = true,
    notNull: Boolean = true,
  ): String {
    if (!notNull) {
      return "null"
    }
    /* This part is used in order to allow
    class members that are not static to call other
    non static fields for constructor invocation
    Consider refactoring later */
    var flagStore = 0
    if (context.kind == ContextAttribute.CLASS &&
      context.contextMethod != null && static
    ) {
      flagStore = context.contextMethod!!.flagAttribute
      context.contextMethod!!.flagAttribute = flagStore or MethodAttribute.STATIC.value
    }
    context.withinConstructorCall = true
    val argumentList = ArrayList<JavaExpression>()
    classAttribute.constructorMethod.argument.forEach { arg ->
      argumentList.add(
        JavaExpression.generateExpression(
          FictiveStatement(context, null, globalContext),
          context,
          1,
          classAttribute.globalContext,
          arg.type,
          ExpressionAttribute.CAST.value,
        ),
      )
    }
    if (context.kind == ContextAttribute.CLASS &&
      context.contextMethod != null && static
    ) {
      context.contextMethod!!.flagAttribute = flagStore
    }
    context.withinConstructorCall = false
    var returnResult = "new " + classAttribute.className + "("
    argumentList.forEachIndexed { index, arg ->
      if (index == argumentList.size - 1) {
        returnResult += arg.generate()
      } else {
        returnResult += arg.generate() + ", "
      }
    }
    return returnResult + ")"
  }

  @JvmStatic
  fun rightLiteralArray(type: JavaType, size: Int, dimension: Int, random: Random): String {
    return "FuzzerUtils." + type.stringValue + dimension.toString() +
      "array(" + size.toString() + ", (" + type.stringValue + ")" + rightLiteral(type, random) + ")"
  }

  @JvmStatic
  fun rightLiteral(type: JavaType, random: Random): String {
    when (type) {
      JavaType.INT -> {
        val value = random
          .nextInt(listOf(0xF, 0XFF, Configuration.MAX_NUMBER)[random.nextInt(3)])
        return (value * (2 * random.nextInt(2) - 1)).toString()
      }
      JavaType.LONG -> {
        val value = random.nextLong()
        return (value * (2 * random.nextInt(2) - 1)).toString() + "L"
      }
      JavaType.DOUBLE -> {
        val value = random.nextInt(listOf(3, 128)[random.nextInt(2)])
        return (value * (2 * random.nextInt(2) - 1)).toString() + "." +
          random.nextInt(1024 * 128).toString()
      }
      JavaType.FLOAT -> {
        val value = random.nextInt(listOf(3, 128)[random.nextInt(2)])
        return (value * (2 * random.nextInt(2) - 1)).toString() + "." +
          random.nextInt(1024).toString() + "F"
      }
      JavaType.SHORT -> {
        val value = random.nextInt(32768)
        return (value * (2 * random.nextInt(2) - 1)).toString()
      }
      JavaType.CHAR -> {
        return random.nextInt(65536).toString()
      }
      JavaType.BYTE -> {
        val value = random.nextInt(128)
        return (value * (2 * random.nextInt(2) - 1)).toString()
      }
      JavaType.BOOLEAN -> {
        return listOf("true", "false")[random.nextInt(2)]
      }
      JavaType.STRING -> {
        return listOf("one", "two", "three", "four")[random.nextInt(4)]
      }
      else -> throw RuntimeException("Unhandled type: $type.stringValue")
    }
  }

  @JvmStatic
  fun generateType(globalContext: GlobalContext): JavaType {
    if (globalContext.classCounter >= Configuration.MAX_CLASSES) {
      return Configuration.TYPES.getRandom(
        null,
        ImmutableSet.of(JavaType.OBJECT),
        globalContext.randomGenerator,
      )
    }
    return Configuration.TYPES.getRandom(
      null,
      null,
      globalContext.randomGenerator,
    )
  }

  @JvmStatic
  fun generateArrayAttribute(globalContext: GlobalContext, type: JavaType): Array.ArrayAttribute? {
    if (type != JavaType.ARRAY) {
      return null
    }
    return Array.ArrayAttribute(
      arrayType = Configuration.TYPES.getRandom(
        null,
        ImmutableSet.of(JavaType.OBJECT, JavaType.ARRAY),
        globalContext.randomGenerator,
      ),
      dimension = globalContext.randomGenerator.nextInt(Configuration.MAX_ARRAY_DIMENSION),
      size = globalContext.randomGenerator.nextInt(Configuration.ARRAY_DEFAULT_SIZE) + 1,
    )
  }

  @JvmStatic
  fun generateClassAttribute(
    globalContext: GlobalContext,
    type: JavaType,
    context: Context,
  ): JavaClass? {
    if (type != JavaType.OBJECT) {
      return null
    }
    return globalContext.getClass(
      context,
    )
  }

  @JvmStatic
  fun generateArraySize(globalContext: GlobalContext): Int {
    if (globalContext.randomGenerator.nextInt(101) < Configuration.P_BIG_ARRAY) {
      return globalContext.randomGenerator.nextInt(Configuration.MAX_BIG_ARRAY) +
        Configuration.MIN_BIG_ARRAY
    }
    return globalContext.randomGenerator.nextInt(Configuration.ARRAY_DEFAULT_SIZE) + 1
  }

  @JvmStatic
  fun generateArrayIdentifier(arrayAttribute: Array.ArrayAttribute): String {
    var returnResult = arrayAttribute.arrayType.stringValue
    for (i in 0..arrayAttribute.dimension - 1) {
      returnResult += "[]"
    }
    return returnResult
  }
}
