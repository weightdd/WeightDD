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

enum class ExpressionType(val javaClass: Class<*>) {
  INVOCATION_EXPRESSION(InvocationExpression::class.java),
  LITERAL_EXPRESSION(LiteralExpression::class.java),
  SCALAR_EXPRESSION(ScalarExpression::class.java),
  ASSIGNMENT_EXPRESSION(AssignmentExpression::class.java),
  OPERATOR_EXPRESSION(OperatorExpression::class.java),
  ARRAY_EXPRESSION(ArrayExpression::class.java),
}
