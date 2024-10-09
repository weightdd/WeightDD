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
import org.pluverse.jvm.fuzz.util.FuzzerConstants
import org.pluverse.jvm.fuzz.util.JavaType
import org.pluverse.jvm.fuzz.util.OperatorCategory
import org.pluverse.jvm.fuzz.util.OperatorKind
import org.pluverse.jvm.fuzz.util.OperatorType

class AssignmentExpression(
  parentStatement: JavaStatement,
  context: Context,
  depth: Int,
  globalContext: GlobalContext,
  type: JavaType = Util.generateType(globalContext),
  flag: Int = 0,
  arrayAttribute: Array.ArrayAttribute? = Util.generateArrayAttribute(globalContext, type),
  classAttribute: JavaClass? = Util.generateClassAttribute(globalContext, type, context),
) : JavaExpression(
  parentStatement = parentStatement,
  context = context,
  depth = depth,
  type = type,
  flag = flag,
  globalContext = globalContext,
  arrayAttribute = arrayAttribute,
  classAttribute = classAttribute,
) {
  private fun generateOperator(): Operator {
    val operationCategory = Configuration.OPERATION_CATEGORY.getRandom(
      FuzzerConstants.OPERATOR_TYPES[type]!![OperatorType.ASSIGN],
      null,
      globalContext.randomGenerator,
    )
    return Operator.getOperator(
      Configuration.OPERATORS[operationCategory]!!.getRandom(
        null,
        null,
        globalContext.randomGenerator,
      ),
      null,
      operationCategory,
      null,
    )
  }

  private fun generateAssignOperand(): JavaExpression {
    if (globalContext.randomGenerator.nextInt(101) <
      Configuration.P_ARRAY_INDEX && type != JavaType.ARRAY &&
      type != JavaType.OBJECT
    ) {
      return ArrayExpression(
        parentStatement,
        context,
        depth = 1,
        globalContext,
        type = type,
        flag = ExpressionAttribute.DESTINATION.value,
      )
    }
    return ScalarExpression(
      parentStatement = parentStatement,
      context = context,
      depth = depth + 1,
      globalContext = globalContext,
      type = type,
      flag = ExpressionAttribute.DESTINATION.value,
      arrayAttribute = arrayAttribute,
      classAttribute = classAttribute,
    )
  }

  val assignOperator = generateOperator()

  val assignOperand = generateAssignOperand()

  var valueOperand = JavaExpression.generateExpression(
    parentStatement,
    context,
    depth + 1,
    globalContext,
    assignOperator.operandTwoType!![
      globalContext.randomGenerator.nextInt(assignOperator.operandTwoType.size),
    ],
    flag,
    if (assignOperand is ScalarExpression) assignOperand.generateArrayAttribute() else null,
    assignOperand.classAttribute,
  )

  private fun avoidArithmeticError() {
    // If catching arithmetic error break early
    if (parentStatement.findParentTryStatement(ExceptionType.ARITHMETIC_EXCEPTION)) {
      return
    }

    if (FuzzerConstants.ARITHMETIC_ERROR.contains(assignOperator.sign) &&
      !valueOperand.flagCheck(ExpressionAttribute.FINAL) &&
      valueOperand !is LiteralExpression
    ) {
      val newOperator = Operator.getOperator(
        "|",
        OperatorKind.INFIX,
        OperatorCategory.INTEGRAL,
        JavaType.LONG,
      )
      val secondOperand = LiteralExpression(
        parentStatement = parentStatement,
        context = context,
        depth = depth,
        globalContext = globalContext,
        type = JavaType.LONG,
      )
      secondOperand.rightLiteral = "1"

      if (valueOperand.type.weight > JavaType.LONG.weight ||
        valueOperand.resType.weight > JavaType.LONG.weight
      ) {
        valueOperand.addFlag(ExpressionAttribute.CAST)
        valueOperand.type = JavaType.LONG
      }

      valueOperand = OperatorExpression(
        parentStatement,
        context,
        depth,
        globalContext,
        type,
        newOperator,
        valueOperand,
        secondOperand,
      )
      return
    }

    if (valueOperand is LiteralExpression &&
      FuzzerConstants.ARITHMETIC_ERROR.contains(assignOperator.sign)
    ) {
      val tempOperand = valueOperand as LiteralExpression
      if (tempOperand.faultyLiteral(type)) {
        if (tempOperand.type == JavaType.DOUBLE) {
          tempOperand.rightLiteral = "1.0"
        } else if (valueOperand.type == JavaType.FLOAT) {
          tempOperand.rightLiteral = "1F"
        } else if (valueOperand.type == JavaType.LONG) {
          tempOperand.rightLiteral = "1L"
        } else {
          tempOperand.rightLiteral = "1"
        }
      }
    }
  }

  init {
    avoidArithmeticError()
  }

  private fun generateAssignment(): String {
    var returnResult: String
    // Check if we need type casting
    // Last complicated expression is because char and short
    if ((
      (
        assignOperator.category == OperatorCategory.ARITH_ASSN ||
          assignOperator.category == OperatorCategory.INTEGRAL_ASSN
        ) &&
        (
          assignOperand.type.weight < valueOperand.type.weight || (
            assignOperand.type == JavaType.SHORT &&
              valueOperand.type == JavaType.CHAR
            ) ||
            (
              assignOperand.type == JavaType.CHAR
              )
          )
      )
    ) {
      returnResult = assignOperand.generate() + " " + assignOperator.sign + " " +
        "(" + assignOperand.resType.stringValue + ") " + valueOperand.generate()
    } else {
      returnResult = assignOperand.generate() + " " + assignOperator.sign +
        " " + valueOperand.generate()
    }

    // May need another layer of casting
    if (type.weight < resType.weight ||
      flagCheck(ExpressionAttribute.CAST)
    ) {
      return "(" + type.stringValue + ") " + "(" + returnResult + ")"
    }
    return returnResult
  }

  override fun generate(): String {
    if (depth != 0) {
      return "(" + generateAssignment() + ")"
    }
    return generateAssignment()
  }
}
