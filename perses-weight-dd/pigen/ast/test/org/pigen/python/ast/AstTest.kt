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
package org.pigen.python.ast

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AstTest {

  @Test
  fun testIsIdentifier() {
    assertThat(Identifier.isIdentifier("")).isFalse()
    assertThat(Identifier.isIdentifier(" ")).isFalse()
    assertThat(Identifier.isIdentifier("a")).isTrue()
  }

  @Test
  fun testNameExpr() {
    val ast = NameExprAst("a".toIdentifier())
    assertThat(ast.sourceCode).isEqualTo("a")
  }

  @Test
  fun testAttrExpr() {
    val ast = AttributeExprAst(
      value = NameExprAst("a".toIdentifier()),
      attribute = "b".toIdentifier(),
    )
    assertThat(ast.sourceCode).isEqualTo("a.b")
  }

  @Test
  fun testCallExpr() {
    CallExprAst(
      function = "a".toNameExprAst(),
      arguments = ArrayList(),
      keywords = ArrayList(),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a()")
    }

    CallExprAst(
      function = AttributeExprAst(value = "a".toNameExprAst(), attribute = "b".toIdentifier()),
      arguments = listOf("c".toNameExprAst()),
      keywords = listOf(),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a.b(c)")
    }

    CallExprAst(
      function = "a".toNameExprAst(),
      arguments = listOf(),
      keywords = listOf(KeywordArgument("key".toIdentifier(), "e".toNameExprAst())),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a(key=e)")
    }

    CallExprAst(
      function = "a".toNameExprAst(),
      arguments = listOf("b".toNameExprAst()),
      keywords = listOf(KeywordArgument("key".toIdentifier(), "e".toNameExprAst())),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a(b, key=e)")
    }
  }

  @Test
  fun testAssign() {
    AssignStmtAst(
      target = "a".toNameExprAst(),
      value = "b".toNameExprAst(),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a = b\n")
    }
    AssignStmtAst(
      target = "a".toNameExprAst(),
      value = 123.toConstantExprAst(),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a = 123\n")
    }
    AssignStmtAst(
      target = "a".toNameExprAst(),
      value = BinaryOperatorExprAst(
        left = "a".toNameExprAst(),
        operator = BinaryOperatorExprAst.AbstractBinaryOperator.Plus,
        right = 5.toConstantExprAst(),
      ),
    ).let {
      assertThat(it.sourceCode).isEqualTo("a = a + 5\n")
    }
  }

  @Test
  fun testTuple() {
    TupleExprAst(listOf("a".toNameExprAst(), "b".toNameExprAst())).let {
      assertThat(it.sourceCode).isEqualTo("(a, b)")
    }
  }

  @Test
  fun testIntConstant() {
    assertThat(1.toConstantExprAst().sourceCode).isEqualTo("1")
  }

  @Test
  fun testPixelsDefinition() {
    AssignStmtAst(
      target = "pixels".toNameExprAst(),
      value = CallExprAst(
        function = AttributeExprAst(
          value = "ti".toNameExprAst(),
          attribute = "field".toIdentifier(),
        ),
        arguments = listOf(),
        keywords = listOf(
          KeywordArgument("dtype".toIdentifier(), value = "float".toNameExprAst()),
          KeywordArgument(
            "shape".toIdentifier(),
            value = TupleExprAst(
              listOf(
                BinaryOperatorExprAst(
                  left = "n".toNameExprAst(),
                  BinaryOperatorExprAst.AbstractBinaryOperator.Multiply,
                  right = 2.toConstantExprAst(),
                ),
                "n".toNameExprAst(),
              ),
            ),
          ),
        ),
      ),
    ).let {
      assertThat(it.sourceCode).isEqualTo(
        "pixels = ti.field(dtype=float, shape=(n * 2, n))\n",
      )
    }
  }

  @Test
  fun testImport() {
    ImportStmtAst.createWithName(name = "taichi".toIdentifier(), alias = null).let {
      assertThat(it.sourceCode).isEqualTo(
        "import taichi\n",
      )
    }
    ImportStmtAst.createWithName(
      name = "taichi".toIdentifier(),
      alias = "ti".toIdentifier(),
    ).let {
      assertThat(it.sourceCode).isEqualTo(
        "import taichi as ti\n",
      )
    }
  }

  @Test
  fun testFunctionDef() {
    FunctionDefStmtAst(
      "f".toIdentifier(),
      decorators = listOf(
        AttributeExprAst(value = "ti".toNameExprAst(), attribute = "kernel".toIdentifier()),
      ),
      body = listOf(
        PassStmtAst(),
      ),
    ).let {
      assertThat(it.sourceCode.trim()).isEqualTo(
        """
         |@ti.kernel
         |def f():
         |    pass
        """.trimMargin(),
      )
    }
  }
}
