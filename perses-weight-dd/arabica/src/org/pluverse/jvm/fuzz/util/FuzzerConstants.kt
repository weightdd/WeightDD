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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet

object FuzzerConstants {
  const val TAB_NUMBER = 4
  const val SUPER = "FuzzerUtils"
  const val MAX_TRIPNM = "N"
  const val INIT = "init"
  const val CHECK_SUM = "checkSum"
  const val RESULT = "res"
  val LETTER: List<Char> = ImmutableList.of(
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
  )
  val DIGIT: List<Char> = ImmutableList.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
  val KEY_WORD: List<String> = ImmutableList.of(
    "init", "N", "checkSum", "res", "main",
    "abstract", "continue", "for", "new", "switch",
    "assert", "default", "if", "package", "synchronized",
    "boolean", "do", "goto", "private", "this",
    "break", "double", "implements", "protected", "throw",
    "byte", "else", "import", "public", "throws",
    "case", "enum", "instanceof", "return", "transient",
    "catch", "extends", "int", "short", "try",
    "char", "final", "interface", "static", "void",
    "class", "finally", "long", "strictfp", "volatile",
    "const", "float", "native", "super", "while",
    "Object", "new",
  )
  val TYPE_BOOLEAN: List<JavaType> = ImmutableList.of(JavaType.BOOLEAN)
  val TYPE_STRING: List<JavaType> = ImmutableList.of(JavaType.STRING)
  val TYPE_BOOLEAN_VAR: List<JavaType> = ImmutableList.of(JavaType.VARIABLE)
  val TYPE_INTEGRAL_VAR: List<JavaType> = ImmutableList.of(JavaType.VARIABLE)
  val TYPE_ARITHMETIC_VAR: List<JavaType> = ImmutableList.of(JavaType.VARIABLE)
  val TYPE_INTEGRAL: List<JavaType> = ImmutableList.of(
    JavaType.BYTE,
    JavaType.SHORT,
    JavaType.INT,
    JavaType.LONG,
  )
  val TYPE_ARITHMETIC: List<JavaType> = ImmutableList.of(
    JavaType.BYTE,
    JavaType.SHORT,
    JavaType.INT,
    JavaType.LONG,
    JavaType.CHAR,
    JavaType.FLOAT,
    JavaType.DOUBLE,
  )
  val TYPE_OBJECT: List<JavaType> = ImmutableList.of(JavaType.OBJECT)
  val TYPE_OBJECT_VAR: List<JavaType> = ImmutableList.of(JavaType.VARIABLE)
  val TYPE_ARRAY: List<JavaType> = ImmutableList.of(JavaType.ARRAY)
  val TYPE_ARRAY_VAR: List<JavaType> = ImmutableList.of(JavaType.VARIABLE)
  val ARITHMETIC_CATEGORY: ImmutableSet<OperatorCategory> = ImmutableSet.of(
    OperatorCategory.ARITHMETIC,
    OperatorCategory.U_ARITHMETIC,
    OperatorCategory.PRE_INDECREMENT,
    OperatorCategory.POST_INDECREMENT,
  )
  val INTEGRAL_CATEGORY: ImmutableSet<OperatorCategory> = ImmutableSet.of(
    OperatorCategory.INTEGRAL,
    OperatorCategory.ARITHMETIC,
    OperatorCategory.U_ARITHMETIC,
    OperatorCategory.PRE_INDECREMENT,
    OperatorCategory.POST_INDECREMENT,
  )
  val OBJECT_CATEGORY: ImmutableSet<OperatorCategory> = ImmutableSet.of(OperatorCategory.OBJECT)
  val ARRAY_CATEGORY: ImmutableSet<OperatorCategory> = ImmutableSet.of(OperatorCategory.ARRAY)
  val OPERATOR_TYPES = hashMapOf<JavaType, HashMap<OperatorType, ImmutableSet<OperatorCategory>>>(
    JavaType.BOOLEAN to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to ImmutableSet.of(
        OperatorCategory.RELATIONAL,
        OperatorCategory.BOOLEAN,
      ),
      OperatorType.ASSIGN to ImmutableSet.of(OperatorCategory.BOOLEAN_ASSN),
    ),
    JavaType.BYTE to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(
        OperatorCategory.INTEGRAL_ASSN,
        OperatorCategory.ARITH_ASSN,
      ),
    ),
    JavaType.CHAR to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(
        OperatorCategory.INTEGRAL_ASSN,
        OperatorCategory.ARITH_ASSN,
      ),
    ),
    JavaType.SHORT to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(
        OperatorCategory.INTEGRAL_ASSN,
        OperatorCategory.ARITH_ASSN,
      ),
    ),
    JavaType.INT to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(
        OperatorCategory.INTEGRAL_ASSN,
        OperatorCategory.ARITH_ASSN,
      ),
    ),
    JavaType.LONG to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(
        OperatorCategory.INTEGRAL_ASSN,
        OperatorCategory.ARITH_ASSN,
      ),
    ),
    JavaType.FLOAT to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(OperatorCategory.ARITH_ASSN),
    ),
    JavaType.DOUBLE to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to INTEGRAL_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(OperatorCategory.ARITH_ASSN),
    ),
    JavaType.OBJECT to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to OBJECT_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(OperatorCategory.OBJECT_ASSN),
    ),
    JavaType.ARRAY to hashMapOf<OperatorType, ImmutableSet<OperatorCategory>>(
      OperatorType.OPERATOR to ARRAY_CATEGORY,
      OperatorType.ASSIGN to ImmutableSet.of(OperatorCategory.ARRAY_ASSN),
    ),
  )
  val ARITHMETIC_ERROR = ImmutableSet.of("/", "%", "/=", "%=")
}
