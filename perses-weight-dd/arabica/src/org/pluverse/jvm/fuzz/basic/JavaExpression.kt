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
import org.pluverse.jvm.fuzz.util.ProbabilityTable

abstract class JavaExpression(
  val parentStatement: JavaStatement,
  val context: Context,
  val depth: Int,
  val globalContext: GlobalContext,
  var flag: Int,
  var type: JavaType,
  val arrayAttribute: Array.ArrayAttribute?,
  val classAttribute: JavaClass?,
) {
  val operands = arrayListOf<JavaExpression>()

  open val resType = type

  fun addFlag(flagInput: ExpressionAttribute) {
    this.flag = this.flag or flagInput.value
  }

  fun flagCheck(flag: ExpressionAttribute): Boolean {
    return this.flag and flag.value > 0
  }

  abstract fun generate(): String

  companion object {
    @JvmStatic
    val EXPRESSION_PROBABILITY = ProbabilityTable<ExpressionType>(
      arrayListOf(
        ExpressionType.INVOCATION_EXPRESSION to 20,
        ExpressionType.LITERAL_EXPRESSION to 20,
        ExpressionType.SCALAR_EXPRESSION to 8,
        ExpressionType.ASSIGNMENT_EXPRESSION to 1,
        ExpressionType.OPERATOR_EXPRESSION to 10,
        ExpressionType.ARRAY_EXPRESSION to 4,
      ),
    )

    @JvmStatic
    fun generateExpression(
      parentStatement: JavaStatement,
      context: Context,
      depth: Int,
      globalContext: GlobalContext,
      type: JavaType? = null,
      flag: Int = 0,
      arrayAttribute: Array.ArrayAttribute? = null,
      classAttribute: JavaClass? = null,
      ignoreSet: Set<ExpressionType> = ImmutableSet.of<ExpressionType>(),
    ): JavaExpression {
      var ignoreExpression = mutableSetOf<ExpressionType>()

      if (type == JavaType.ARRAY || type == JavaType.OBJECT) {
        ignoreExpression.add(ExpressionType.OPERATOR_EXPRESSION)
        ignoreExpression.add(ExpressionType.ARRAY_EXPRESSION)
      }

      if (depth >= Configuration.MAX_CALLER_CHAIN ||
        parentStatement is FictiveStatement
      ) {
        ignoreExpression.add(ExpressionType.INVOCATION_EXPRESSION)
      }

      if (parentStatement is LoopStatement) {
        ignoreExpression.add(ExpressionType.INVOCATION_EXPRESSION)
      }

      val chosenClass = EXPRESSION_PROBABILITY.getRandom(
        null,
        ignoreExpression + ignoreSet,
        globalContext.randomGenerator,
      )

      val chosenConstructor = chosenClass.javaClass.getDeclaredConstructor(
        JavaStatement::class.java,
        Context::class.java,
        Int::class.java,
        GlobalContext::class.java,
        JavaType::class.java,
        Int::class.java,
        Array.ArrayAttribute::class.java,
        JavaClass::class.java,
      )
      val instance = chosenConstructor.newInstance(
        parentStatement,
        context,
        depth,
        globalContext,
        type,
        flag,
        arrayAttribute,
        classAttribute,
      ) as JavaExpression

      return instance
    }
  }
}
