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

open class Variable(
  val context: Context,
  val flagAttribute: Int = 0,
  type: JavaType? = null,
  name: String? = null,
  val value: String? = null,
  val isArray: Boolean = false,
  val globalContext: GlobalContext,
) {
  private fun setFlag(flagAttribute: Int): Int {
    var flags = flagAttribute
    if (context.kind.value < flagAttribute &&
      ((flags and NameAttribute.CLASS.value) > 0)
    ) {
      flags = (flags or NameAttribute.PUBLIC.value)
    }
    if (globalContext.randomGenerator.nextInt(101) < Configuration.P_VOLATILE &&
      flagCheck(NameAttribute.CHECKSUM)
    ) {
      return (flags or NameAttribute.VOLATILE.value)
    }
    return flags
  }

  fun addFlag(flagInput: Int) {
    this.flags = this.flags or flagInput
  }

  fun flagCheck(flags: NameAttribute): Boolean {
    return this.flags and flags.value > 0
  }

  var flags = setFlag(flagAttribute)
  val type = if (type == null) {
    Configuration.TYPES.getRandom(
      ImmutableSet.of(
        JavaType.BOOLEAN,
        JavaType.STRING,
        JavaType.BYTE,
        JavaType.CHAR,
        JavaType.SHORT,
        JavaType.LONG,
        JavaType.FLOAT,
        JavaType.DOUBLE,
      ),
      null,
      globalContext.randomGenerator,
    )
  } else {
    type
  }

  private fun generateInitialValue(): String {
    if (type == JavaType.OBJECT || type == JavaType.ARRAY) {
      return ""
    }

    if (flagCheck(NameAttribute.NULL)) {
      return "0"
    }
    if (value != null) {
      return value
    }
    return Util.rightLiteral(type, globalContext.randomGenerator)
  }

  open val initialValue = generateInitialValue()

  open fun resetStaticValue(): String {
    return globalContext.printLine(generateDeclaration() + ";")
  }

  val name = context.registerVariable(this, flagCheck(NameAttribute.LOCAL), name, isArray)

  fun generateModifier(): String {
    var returnValue = ""
    if (flagCheck(NameAttribute.VOLATILE)) {
      returnValue = "volatile " + returnValue
    }
    if (flagCheck(NameAttribute.FINAL)) {
      returnValue = "final " + returnValue
    }
    if (flagCheck(NameAttribute.STATIC)) {
      returnValue = "static " + returnValue
    }
    if (flagCheck(NameAttribute.PUBLIC)) {
      returnValue = "public " + returnValue
      return returnValue
    }
    returnValue = "private" + returnValue
    return returnValue
  }

  open fun generateDeclaration(): String {
    return name + " = " + initialValue
  }

  open fun generateCheckSum(): String {
    val name = generateName()
    when (type) {
      JavaType.BOOLEAN -> {
        return "(" + name + " ? 1 : 0)"
      }
      JavaType.CHAR -> {
        return "(int)" + name
      }
      JavaType.FLOAT -> {
        return "Float.floatToIntBits(" + name + ")"
      }
      JavaType.DOUBLE -> {
        return "Double.doubleToLongBits(" + name + ")"
      }
      else -> {
        return name
      }
    }
  }

  fun generateName(): String {
    if (flagCheck(NameAttribute.STATIC)) {
      return context.contextClass.className + "." + name
    }
    return name
  }
}
