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
import org.pluverse.jvm.fuzz.util.OperatorCategory
import org.pluverse.jvm.fuzz.util.OperatorKind

class Operator(
  val sign: String,
  val category: OperatorCategory,
  val resultType: List<JavaType>,
  val kind: OperatorKind,
  val operandOneType: List<JavaType>,
  val operandTwoType: List<JavaType>?,
) {
  private fun verifyOperator(
    signInput: String,
    kindInput: OperatorKind?,
    categoryInput: OperatorCategory?,
    typeInput: JavaType?,
  ): Boolean {
    return (
      this.sign.equals(signInput) &&
        (kindInput == null || kind.equals(kindInput)) &&
        (categoryInput == null || category.equals(categoryInput)) &&
        (typeInput == null || operandOneType.contains(typeInput))
      )
  }

  companion object {
    @JvmStatic
    fun defineOperators(): ArrayList<Operator> {
      val operatorArray = arrayListOf<Operator>()

      Configuration.OPERATORS.forEach { (category, table) ->
        table.getKeys().map({
          val operation = it
          when (category) {
            OperatorCategory.RELATIONAL -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_BOOLEAN,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  FuzzerConstants.TYPE_ARITHMETIC,
                ),
              )
            }
            OperatorCategory.BOOLEAN -> {
              if (operation.equals("!")) {
                operatorArray.add(
                  Operator(
                    operation,
                    category,
                    FuzzerConstants.TYPE_BOOLEAN,
                    OperatorKind.PREFIX,
                    FuzzerConstants.TYPE_BOOLEAN,
                    null,
                  ),
                )
              } else {
                operatorArray.add(
                  Operator(
                    operation,
                    category,
                    FuzzerConstants.TYPE_BOOLEAN,
                    OperatorKind.INFIX,
                    FuzzerConstants.TYPE_BOOLEAN,
                    FuzzerConstants.TYPE_BOOLEAN,
                  ),
                )
              }
            }
            OperatorCategory.INTEGRAL -> {
              if (operation.equals("~")) {
                operatorArray.add(
                  Operator(
                    operation,
                    category,
                    FuzzerConstants.TYPE_INTEGRAL,
                    OperatorKind.PREFIX,
                    FuzzerConstants.TYPE_INTEGRAL,
                    null,
                  ),
                )
              } else {
                operatorArray.add(
                  Operator(
                    operation,
                    category,
                    FuzzerConstants.TYPE_INTEGRAL,
                    OperatorKind.INFIX,
                    FuzzerConstants.TYPE_INTEGRAL,
                    FuzzerConstants.TYPE_INTEGRAL,
                  ),
                )
              }
            }
            OperatorCategory.ARITHMETIC -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  FuzzerConstants.TYPE_ARITHMETIC,
                ),
              )
            }
            OperatorCategory.U_ARITHMETIC -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  OperatorKind.PREFIX,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  null,
                ),
              )
            }
            OperatorCategory.PRE_INDECREMENT -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  OperatorKind.PREFIX,
                  FuzzerConstants.TYPE_ARITHMETIC_VAR,
                  null,
                ),
              )
            }
            OperatorCategory.POST_INDECREMENT -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  OperatorKind.POSTFIX,
                  FuzzerConstants.TYPE_ARITHMETIC_VAR,
                  null,
                ),
              )
            }
            OperatorCategory.BOOLEAN_ASSN -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_BOOLEAN,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_BOOLEAN_VAR,
                  FuzzerConstants.TYPE_BOOLEAN,
                ),
              )
            }
            OperatorCategory.INTEGRAL_ASSN -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_INTEGRAL,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_INTEGRAL_VAR,
                  FuzzerConstants.TYPE_INTEGRAL,
                ),
              )
            }
            OperatorCategory.ARITH_ASSN -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_ARITHMETIC,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_ARITHMETIC_VAR,
                  FuzzerConstants.TYPE_ARITHMETIC,
                ),
              )
            }
            OperatorCategory.OBJECT_ASSN -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_OBJECT,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_OBJECT_VAR,
                  FuzzerConstants.TYPE_OBJECT,
                ),
              )
            }
            OperatorCategory.ARRAY_ASSN -> {
              operatorArray.add(
                Operator(
                  operation,
                  category,
                  FuzzerConstants.TYPE_ARRAY,
                  OperatorKind.INFIX,
                  FuzzerConstants.TYPE_ARRAY_VAR,
                  FuzzerConstants.TYPE_ARRAY,
                ),
              )
            }
            else -> {
              throw RuntimeException("Unknown operator for define category")
            }
          }
        })
      }
      return operatorArray
    }

    @JvmStatic
    val operatorList = defineOperators()

    @JvmStatic
    fun getOperator(
      sign: String,
      kind: OperatorKind?,
      category: OperatorCategory?,
      type: JavaType?,
    ): Operator {
      return operatorList.find { operation ->
        operation.verifyOperator(sign, kind, category, type)
      }!!
    }
  }
}
