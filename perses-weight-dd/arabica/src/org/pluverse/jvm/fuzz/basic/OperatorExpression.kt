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
import org.pluverse.jvm.fuzz.util.ExceptionType
import org.pluverse.jvm.fuzz.util.FuzzerConstants
import org.pluverse.jvm.fuzz.util.JavaType
import org.pluverse.jvm.fuzz.util.OperatorCategory
import org.pluverse.jvm.fuzz.util.OperatorKind
import org.pluverse.jvm.fuzz.util.OperatorType

class OperatorExpression(
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
  val notNullFlag = if (flagCheck(ExpressionAttribute.NOT_NULL)) {
    ExpressionAttribute.NOT_NULL.value
  } else {
    0
  }

  private fun generateOperandOne(): JavaExpression {
    if (expressionOperator.operandOneType.contains(JavaType.VARIABLE)) {
      return ScalarExpression(
        parentStatement,
        context,
        depth + 1,
        globalContext,
        type,
        notNullFlag or ExpressionAttribute.DESTINATION.value,
        arrayAttribute,
        classAttribute,
      )
    }
    return JavaExpression.generateExpression(
      parentStatement,
      context,
      depth + 1,
      globalContext,
      Configuration.TYPES.getRandom(
        ImmutableSet.copyOf(expressionOperator.operandOneType),
        null,
        globalContext.randomGenerator,
      ),
      notNullFlag,
      arrayAttribute,
      classAttribute,
    )
  }

  private fun generateOperandTwo(): JavaExpression? {
    if (expressionOperator.kind == OperatorKind.INFIX) {
      return JavaExpression.generateExpression(
        parentStatement,
        context,
        depth + 1,
        globalContext,
        Configuration.TYPES.getRandom(
          ImmutableSet.copyOf(
            expressionOperator.operandTwoType!!,
          ),
          null,
          globalContext.randomGenerator,
        ),
        notNullFlag,
        arrayAttribute,
        classAttribute,
      )
    }
    return null
  }

  private fun generateCategory(): OperatorCategory {
    // Refactor this, just experiment at moment
    val returnCategory = Configuration.OPERATION_CATEGORY.getRandom(
      FuzzerConstants.OPERATOR_TYPES[type]!![OperatorType.OPERATOR],
      null,
      globalContext.randomGenerator,
    )
    if (returnCategory == OperatorCategory.INTEGRAL_ASSN ||
      returnCategory == OperatorCategory.ARITH_ASSN
    ) {
      generateCategory()
    }
    return returnCategory
  }

  private fun generateResType(): JavaType {
    var currentResType = type
    if (FuzzerConstants.TYPE_ARITHMETIC.contains(type)) {
      if (operandTwo != null &&
        operandOne.type.weight < operandTwo!!.type.weight
      ) {
        currentResType = operandTwo!!.type
      } else {
        currentResType = operandOne.type
      }
    }
    if (!expressionOperator.resultType.contains(currentResType)) {
      currentResType = JavaType.LONG
    }
    if (FuzzerConstants.TYPE_ARITHMETIC.contains(type) &&
      currentResType.weight < JavaType.INT.weight
    ) {
      currentResType = JavaType.INT
    }
    return currentResType
  }

  val operatorCategory = generateCategory()

  var expressionOperator = Operator.getOperator(
    Configuration.OPERATORS[operatorCategory]!!.getRandom(
      null,
      null,
      globalContext.randomGenerator,
    ),
    null,
    operatorCategory,
    null,
  )

  var operandOne = generateOperandOne()

  var operandTwo = generateOperandTwo()

  /*
  @todo refactor: Considering getting constructors without using
  reflaction on respective classes
  Currently the arithmetic error check need to conducted again on new operands
   */
  constructor(
    parentStatement: JavaStatement,
    context: Context,
    depth: Int,
    globalContext: GlobalContext,
    type: JavaType,
    inputOperator: Operator,
    leftOperand: JavaExpression,
    rightOperand: JavaExpression,
  ) : this(
    parentStatement,
    context,
    depth,
    globalContext,
    type,
  ) {
    operandOne = leftOperand
    operandTwo = rightOperand
    expressionOperator = inputOperator
    avoidArithmeticError()
  }

  private fun avoidArithmeticError() {
    // If catching arithmetic error break early
    if (parentStatement.findParentTryStatement(ExceptionType.ARITHMETIC_EXCEPTION)) {
      return
    }

    if (FuzzerConstants.ARITHMETIC_ERROR.contains(expressionOperator.sign) &&
      !operandTwo!!.flagCheck(ExpressionAttribute.FINAL) &&
      operandTwo !is LiteralExpression
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

      if (operandTwo!!.resType.weight > JavaType.LONG.weight ||
        operandTwo!!.type.weight > JavaType.LONG.weight
      ) {
        operandTwo!!.addFlag(ExpressionAttribute.CAST)
        operandTwo!!.type = JavaType.LONG
      }

      operandTwo = OperatorExpression(
        parentStatement,
        context,
        depth,
        globalContext,
        JavaType.LONG,
        newOperator,
        operandTwo!!,
        secondOperand,
      )
      return
    }

    if (operandTwo is LiteralExpression &&
      FuzzerConstants.ARITHMETIC_ERROR.contains(expressionOperator.sign) &&
      (
        operandTwo!!.resType == JavaType.INT ||
          operandTwo!!.resType == JavaType.LONG ||
          operandTwo!!.resType == JavaType.SHORT
        )
    ) {
      val tempOperand = operandTwo as LiteralExpression
      if (tempOperand.faultyLiteral(type)) {
        if (tempOperand.type == JavaType.LONG) {
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

  override val resType = generateResType()

  override fun generate(): String {
    var returnResult = ""
    if ((
        flagCheck(ExpressionAttribute.CAST) ||
          resType.weight > type.weight
        ) &&
      !(parentStatement is VectorizationStatement)
    ) {
      returnResult += "(" + type.stringValue + ") "
    }
    if (expressionOperator.kind == OperatorKind.PREFIX) {
      returnResult += expressionOperator.sign
    }
    if (!(parentStatement is VectorizationStatement)) {
      returnResult += "("
    }
    returnResult += operandOne.generate()
    if (expressionOperator.kind == OperatorKind.POSTFIX) {
      returnResult += expressionOperator.sign
    } else if (expressionOperator.kind == OperatorKind.INFIX) {
      returnResult += " " + expressionOperator.sign + " "
      returnResult += operandTwo!!.generate()
    }
    if (!(parentStatement is VectorizationStatement)) {
      return returnResult + ")"
    }
    return returnResult
  }
}
