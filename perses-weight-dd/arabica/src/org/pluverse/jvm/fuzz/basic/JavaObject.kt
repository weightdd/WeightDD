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

import org.pluverse.jvm.fuzz.util.JavaType

class JavaObject(
  context: Context,
  flagAttribute: Int,
  name: String? = null,
  globalContext: GlobalContext,
  val classAttribute: JavaClass = globalContext.getClass(context),
) : Variable(
  context,
  flagAttribute,
  JavaType.OBJECT,
  name,
  globalContext = globalContext,
) {

  override val initialValue = Util.rightLiteralObject(
    globalContext,
    context,
    classAttribute,
    this.flagCheck(
      NameAttribute.STATIC,
    ),
    this.flagCheck(
      NameAttribute.NOTNULL,
    ),
  )

  // Register the object after literalValue
  // such that we won't get into cycle during right literal creation
  init {
    context.objectList.add(this)
  }

  override fun resetStaticValue(): String {
    return globalContext.printLine(name + " = " + initialValue + ";")
  }

  override fun generateDeclaration(): String {
    return classAttribute.className + " " + name + " = " + initialValue + ";"
  }

  override fun generateCheckSum(): String {
    if (flagCheck(NameAttribute.NULL)) {
      return "0"
    }
    return generateName() + ".checkSum()"
  }
}
