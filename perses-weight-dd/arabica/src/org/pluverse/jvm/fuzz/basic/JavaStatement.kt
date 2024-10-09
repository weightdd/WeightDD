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
import org.pluverse.jvm.fuzz.util.ProbabilityTable

abstract class JavaStatement(
  val context: Context,
  val parent: JavaStatement?,
  val globalContext: GlobalContext,
) {
  val variableReuseProb = Configuration.P_VAR_REUSE
  val inMainTest = context.contextMethod is MainTest

  open val loopFlag = false

  fun statementsRemainder(): Int {
    if (context.contextMethod!!.numStatementsGenerate < 0) {
      return 0
    }
    return context.contextMethod!!.numStatementsGenerate
  }

  fun loopDepth(): Int {
    var currentDepth = 0
    var currentStatement = parent
    while (currentStatement != null) {
      if (currentStatement is LoopStatement) {
        currentDepth += 1
      }
      currentStatement = currentStatement.parent
    }
    return currentDepth
  }

  fun findParentTryStatement(exception: ExceptionType): Boolean {
    if (this is TryStatement && this.chosenException == exception) {
      return true
    } else if (parent == null) {
      return false
    }
    return parent.findParentTryStatement(exception)
  }

  /** Generic generate method used for all classes */
  abstract fun generate(): String

  companion object {
    private val STATEMENT_PROBABILITY = ProbabilityTable<StatementType>(
      arrayListOf(
        StatementType.ASSIGNMENT_STATEMENT to 5,
        StatementType.CONDITIONAL_STATEMENT to 1,
        StatementType.INVOCATION_STATEMENT to 1,
        StatementType.LOOP_STATEMENT to 1,
        StatementType.VECTORIZATION_STATEMENT to 1,
        StatementType.TRY_STATEMENT to 1,
        StatementType.SWITCH_STATEMENT to 1,
      ),
    )

    private fun findStatementDepth(statement: JavaStatement): Int {
      var counter = 0
      var currentStatement = statement
      while (currentStatement.parent != null) {
        currentStatement = currentStatement.parent as JavaStatement
        counter += 1
      }
      return counter
    }

    private fun containSwitchStatement(statement: JavaStatement): Boolean {
      var currentStatement = statement
      if (currentStatement is SwitchStatement) {
        return true
      }
      while (currentStatement.parent != null) {
        if (currentStatement is SwitchStatement) {
          return true
        }
        currentStatement = currentStatement.parent as JavaStatement
      }
      return false
    }

    /** Helper used to determine which type of statement to generate */
    // To do, remove reflection here and replace with lambda function
    private fun pickNestedStatement(
      currentStatement: JavaStatement,
      context: Context,
      globalContext: GlobalContext,
    ): JavaStatement {
      context.contextMethod!!.numStatementsGenerate -= 1
      val ignoreStatement = mutableSetOf<StatementType>()

      if (context.contextMethod!!.numStatementsGenerate < 1) {
        ignoreStatement.add(StatementType.CONDITIONAL_STATEMENT)
        ignoreStatement.add(StatementType.TRY_STATEMENT)
      }

      if (globalContext.getCallerHashDepth(context.contextMethod) >= Configuration.MAX_CALLER_CHAIN
      ) {
        ignoreStatement.add(StatementType.INVOCATION_STATEMENT)
      }

      if (context.contextMethod!!.numStatementsGenerate < 2 ||
        currentStatement.loopDepth() >= Configuration.MAX_LOOP_DEPTH
      ) {
        ignoreStatement.add(StatementType.LOOP_STATEMENT)
        ignoreStatement.add(StatementType.VECTORIZATION_STATEMENT)
      }

      if (containSwitchStatement(currentStatement)) {
        ignoreStatement.add(StatementType.SWITCH_STATEMENT)
      }

      val chosenClass = STATEMENT_PROBABILITY.getRandom(
        null,
        ignoreStatement,
        globalContext.randomGenerator,
      )

      val statementInstance = chosenClass.javaClass.getDeclaredConstructor(
        Context::class.java,
        JavaStatement::class.java,
        GlobalContext::class.java,
      ).newInstance(context, currentStatement, globalContext) as? JavaStatement
      check(statementInstance != null) { "Statement Class Not Found" }
      return statementInstance
    }

    /** Generates sequence of statements under this one */
    @JvmStatic
    fun generateStatementSequence(
      currentStatement: JavaStatement,
      context: Context,
      globalContext: GlobalContext,
      maxStatementNumber: Int,
    ): ArrayList<JavaStatement> {
      val returnResult = arrayListOf<JavaStatement>()
      if (maxStatementNumber == 0) {
        return returnResult
      }
      for (counter in 0..maxStatementNumber) {
        val newStatement = pickNestedStatement(currentStatement, context, globalContext)
        returnResult.add(newStatement)
        if (context.contextMethod!!.numStatementsGenerate <= 0) {
          break
        }
      }
      return returnResult
    }
  }
}
