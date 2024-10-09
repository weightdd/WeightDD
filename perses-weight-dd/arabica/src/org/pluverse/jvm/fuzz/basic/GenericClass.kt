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

class GenericClass(
  val outerCaller: ArrayList<JavaMethod> = ArrayList<JavaMethod>(),
  globalContext: GlobalContext,
  extendClass: JavaClass? = null,
) : JavaClass(
  globalContext = globalContext,
  extendClass = extendClass,
) {
  override val className = classContext.genUniqueName("Cls")

  init {
    globalContext.registerClass()
    globalContext.generateQueue.add(this)
  }

  override val constructorMethod = Constructor(
    methodClass = this,
    outerCaller = outerCaller,
    globalContext = globalContext,
    flagAttribute = 0,
  )

  init {
    globalContext.classList.add(this)
    if (extendClass != null) {
      globalContext.addMethodCaller(
        constructorMethod,
        extendClass.constructorMethod,
      )
    }
  }

  override fun generate(): String {
    var returnResult = ""
    if (extendClass != null) {
      returnResult += globalContext.printLine(
        "class " + className + " extends " + extendClass.className + " {",
      ) + "\n"
    } else {
      returnResult += globalContext.printLine(
        "class " + className + " {",
      ) + "\n"
    }
    returnResult += globalContext.printLine("static int " + className + "loadVar = 0;")

    globalContext.updateIndentation(1)
    returnResult += classContext.generateDeclaration()

    methodList.forEach { method ->
      returnResult += method.generate() + "\n"
    }
    returnResult += generateClassCheckSum()
    returnResult += constructorMethod.generate()
    returnResult += resetMethod.generate()
    globalContext.updateIndentation(-1)
    returnResult += globalContext.printLine("}")
    return returnResult
  }
}

/* Fictive Statement Class Used to Supply a Statement Parameter for
Expression Generation, temporary solution, maybe better solution exists */
class FictiveStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {
  override fun generate(): String {
    return ""
  }
}
