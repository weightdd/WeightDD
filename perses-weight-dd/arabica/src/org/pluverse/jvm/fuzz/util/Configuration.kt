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
package org.pluverse.jvm.fuzz.util

object Configuration {
  const val MODE = "default"
  const val MAIN_CLASS_NAME = "Test"
  const val PACKAGE_NAME = ""
  const val OUTER_CONTROL = true
  const val MAX_SIZE = 100
  const val MAX_NESTED_SIZE = 10000000
  const val MAX_NESTED_SIZE_NOT_MAIN_TEST = 20000
  const val MIN_SIZE_FRACTION = 0.5
  const val MAX_STATEMENTS = 10
  const val MAX_ARRAY_DIMENSION = 2
  const val WIDTH = 120
  const val MAX_SHIFT = 110
  const val P_BIG_ARRAY = 5
  const val MIN_BIG_ARRAY = 1200
  const val MAX_BIG_ARRAY = 10000
  const val ARRAY_DEFAULT_SIZE = 100
  const val MAX_METHOD = 5
  const val MAX_ARGUMENT = 5
  const val MAX_CLASSES = 5
  const val MAX_CALLER_CHAIN = 2
  const val P_NON_STATIC_METHOD = 100
  const val MAIN_TEST_CALL_NUMBER = 20
  const val TIME_SLEEP_COMPLETE_TIER_ONE = 5000
  const val MAIN_TEST_CALL_NUMBER_TIER_TWO = 50
  const val EXPRESSION_INVOCATION_DEPTH = 10
  const val P_EXTENDS_CLASS = 50
  const val P_METHOD_OVERRIDE = 100
  const val P_INLINE_METHOD = 30
  const val MAX_NUMBER = 0x10000
  const val MAX_EXPRESSION_DEPTH = 3
  const val P_NULL_LITERAL = 30
  const val MAX_IF_STATEMENT = 4
  const val MAX_ELSE_STATEMENT = 6
  const val MAX_TRY_STATEMENT = 6
  const val MAX_LOOP_STATEMENT = 5
  const val MAX_SWITCH_STATEMENT = 2
  const val MAX_LOOP_DEPTH = 3
  const val START_FRACTION = 16
  const val MIN_SMALL_METHOD_CALL = 100
  const val MAX_SMALL_METHOD_CALL = 10000
  const val P_UNKNOWN_LOOP_LIMIT = 0
  const val P_INEQUALITY_IN_LOOP_CONDITION = 0
  const val P_GUARANTEED_AIIOB = 20
  const val MAX_OBJECT_ARRAY_SIZE = 100
  const val ALLOW_OBJECT_ARGS = 0
  const val P_INVOC_EXPRESSION = 25
  const val P_INLINE_INVOC_EXPRESSION = 2
  const val P_VOLATILE = 15
  const val P_ELSE_IF = 10
  const val MAX_STATEMENT_DEPTH = 2
  const val P_TRIANG = 30
  val VAR_TYPES = ProbabilityTable<VariableType>(
    arrayListOf(
      VariableType.NON_STATIC to 5,
      VariableType.STATIC to 1,
      VariableType.LOCAL to 10,
      VariableType.STATIC_OTHER to 1,
      VariableType.NON_STATIC_OTHER to 10,
      VariableType.BLOCK to 3,
    ),
  )
  val METHOD_TYPES = ProbabilityTable<MethodType>(
    arrayListOf(
      MethodType.SAME_CLASS_STATIC to 5,
      MethodType.SAME_CLASS_NON_STATIC to 20,
      MethodType.STATIC_OTHER to 3,
      MethodType.NON_STATIC_OTHER to 3,
    ),
  )
  val TYPES = ProbabilityTable<JavaType>(
    arrayListOf(
      JavaType.ARRAY to 2,
      JavaType.OBJECT to 2,
      JavaType.BOOLEAN to 1,
      JavaType.STRING to 0,
      JavaType.BYTE to 1,
      JavaType.CHAR to 1,
      JavaType.SHORT to 1,
      JavaType.INT to 1,
      JavaType.LONG to 1,
      JavaType.FLOAT to 1,
      JavaType.DOUBLE to 1,
    ),
  )
  val EXPRESSION_KIND = ProbabilityTable<String>(
    arrayListOf(
      "literal" to 20, "scalar" to 8,
      "array" to 2, "field" to 0, "oper" to 10,
      "assign" to 1, "cond" to 0, "inlinvoc" to 0, "invoc" to 5, "libinvoc" to 2,
    ),
  )
  val OPERATION_CATEGORY = ProbabilityTable<OperatorCategory>(
    arrayListOf(
      OperatorCategory.RELATIONAL to 2, OperatorCategory.BOOLEAN to 1,
      OperatorCategory.INTEGRAL to 5, OperatorCategory.ARITHMETIC to 30,
      OperatorCategory.U_ARITHMETIC to 5, OperatorCategory.PRE_INDECREMENT to 2,
      OperatorCategory.POST_INDECREMENT to 2, OperatorCategory.BOOLEAN_ASSN to 1,
      OperatorCategory.INTEGRAL_ASSN to 1, OperatorCategory.ARITH_ASSN to 2,
      OperatorCategory.OBJECT_ASSN to 1, OperatorCategory.ARRAY_ASSN to 25,
    ),
  )
  val INDEX_KINDS = ProbabilityTable<String>(
    arrayListOf(
      "-1" to 12,
      "0" to 18,
      "+1" to 12,
      "any" to 1,
    ),
  )
  val OPERATORS = hashMapOf<OperatorCategory, ProbabilityTable<String>>(
    OperatorCategory.RELATIONAL to ProbabilityTable<String>(
      arrayListOf(
        "==" to 1,
        "!=" to 1,
        "<" to 1,
        "<=" to 1,
        ">" to 1,
        ">=" to 1,
      ),
    ),
    OperatorCategory.BOOLEAN to ProbabilityTable<String>(
      arrayListOf(
        "==" to 1,
        "!=" to 1,
        "&" to 1,
        "|" to 1,
        "^" to 1,
        "&&" to 1,
        "||" to 1,
        "!" to 1,
      ),
    ),
    OperatorCategory.INTEGRAL to ProbabilityTable<String>(
      arrayListOf(
        "&" to 1,
        "|" to 1,
        "^" to 1,
        "<<" to 1,
        ">>" to 1,
        ">>>" to 1,
        "~" to 1,
      ),
    ),
    OperatorCategory.ARITHMETIC to ProbabilityTable<String>(
      arrayListOf(
        "+" to 1,
        "-" to 1,
        "*"
          to 1,
        "/" to 1,
        "%" to 1,
      ),
    ),
    OperatorCategory.U_ARITHMETIC to ProbabilityTable<String>(arrayListOf("-" to 1)),
    OperatorCategory.PRE_INDECREMENT to ProbabilityTable<String>(arrayListOf("++" to 1, "--" to 1)),
    OperatorCategory.POST_INDECREMENT to ProbabilityTable<String>(
      arrayListOf("++" to 1, "--" to 1),
    ),
    OperatorCategory.BOOLEAN_ASSN to ProbabilityTable<String>(arrayListOf("=" to 1)),
    OperatorCategory.INTEGRAL_ASSN to ProbabilityTable<String>(
      arrayListOf(
        "=" to 1,
        "&=" to 1,
        "|=" to 1,
        "^=" to 1,
        "<<=" to 1,
        ">>=" to 1,
        ">>>=" to 1,
      ),
    ),
    OperatorCategory.ARITH_ASSN to ProbabilityTable<String>(
      arrayListOf(
        "=" to 1,
        "+=" to 1,
        "-=" to 1,
        "*=" to 1,
        "/=" to 1,
        "%=" to 1,
      ),
    ),
    OperatorCategory.OBJECT_ASSN to ProbabilityTable<String>(arrayListOf("=" to 1)),
    OperatorCategory.ARRAY_ASSN to ProbabilityTable<String>(arrayListOf("=" to 1)),
  )
  val P_EMPTY_SEQUENCE = 2
  val P_ELSE = 40
  val P_THROW_EXCEPTION = 20
  val P_FINALLY = 40
  val P_INDUCTION = 30
  val P_METHOD_REUSE = 100
  val P_RETURN = 10
  val P_VAR_REUSE = 100
  val P_CLASS_REUSE = 30
  val P_BIG_SWITCH = 1
  val P_PACKED_SWITCH = 60
  val P_SWITCH_EMPTY_CASE = 5
  val P_INDUCTION_VAR_REUSE = 30
  val P_GENERATE_OTHER_STATIC_METHOD = 30
  val P_GENERATE_OTHER_NON_STATIC_METHOD = 30
  val P_HIDE_PARENT = 30
  val P_ARRAY_INDEX = 20
  val FOR_STEP = ProbabilityTable<Int>(
    arrayListOf(
      -3 to 1,
      -2 to 1,
      -1 to 4,
      1 to 32,
      2 to 1,
      3 to 1,
    ),
  )
  val P_INDUCTION_TYPE = ProbabilityTable<JavaType>(
    arrayListOf(
      JavaType.INT to 20,
      JavaType.LONG to 5,
      JavaType.FLOAT to 1,
      JavaType.DOUBLE to 1,
    ),
  )
  val P_EXCEPTION_TYPE = ProbabilityTable<ExceptionType>(
    arrayListOf(
      ExceptionType.ARITHMETIC_EXCEPTION to 10,
      ExceptionType.ARRAY_INDEX_EXCEPTION to 10,
      ExceptionType.NULL_POINTER_EXCEPTION to 5,
    ),
  )
}
