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

import org.pluverse.jvm.fuzz.util.ProbabilityTable

class LoopStatement(
  context: Context,
  parent: JavaStatement?,
  globalContext: GlobalContext,
) : JavaStatement(
  context = context,
  parent = parent,
  globalContext = globalContext,
) {
  enum class LoopStrategy(val javaClass: Class<*>) {
    BOOLEAN_FOR_LOOP(BooleanForLoop::class.java),
    SIMPLE_FOR_LOOP(SimpleForLoop::class.java),
    COMPLEX_FOR_LOOP(ComplexForLoop::class.java),
  }

  val loopStrategy = LoopStatement.getStrategy(this, context, globalContext)

  override fun generate(): String {
    return loopStrategy.generate()
  }

  companion object {
    val LOOP_PROBABILITY = ProbabilityTable<LoopStrategy>(
      arrayListOf(
        LoopStrategy.SIMPLE_FOR_LOOP to 1,
        LoopStrategy.COMPLEX_FOR_LOOP to 1,
        LoopStrategy.BOOLEAN_FOR_LOOP to 1,
      ),
    )

    @JvmStatic
    fun getStrategy(
      currentStatement: LoopStatement,
      context: Context,
      globalContext: GlobalContext,
    ): LoopImplementation {
      val chosenClass = LOOP_PROBABILITY.getRandom(
        null,
        null,
        globalContext.randomGenerator,
      )
      val implementationInstance = chosenClass.javaClass.getDeclaredConstructor(
        LoopStatement::class.java,
        Context::class.java,
        GlobalContext::class.java,
      ).newInstance(currentStatement, context, globalContext) as? LoopImplementation

      check(implementationInstance != null) { "Implementation Class Not Found" }

      return implementationInstance
    }
  }
}
