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

import org.perses.util.ast.IPrecedence
import org.perses.util.ast.Indent
import java.io.PrintStream

class FunctionDefStmtAst(
  val name: Identifier,
  decorators: Iterable<AbstractPythonExprAst>,
  body: Iterable<AbstractPythonStmtAst>,
) : AbstractPythonStmtAst() {

  private val _decorators = ArrayList<AbstractPythonExprAst>().apply {
    addAll(decorators)
  }

  val decorators: List<AbstractPythonExprAst>
    get() = _decorators

  private val _body = ArrayList<AbstractPythonStmtAst>().apply {
    addAll(body)
  }

  val body: List<AbstractPythonStmtAst>
    get() = _body

  override val precedence: IPrecedence
    get() = Precedence.UNIT

  override fun toSourceCode(stream: PrintStream, indent: Indent, multiLineMode: Boolean) {
    decorators.forEach {
      stream.print("@")
      it.toSourceCode(stream, indent, multiLineMode)
      stream.print('\n')
      indent.printIndent(stream)
    }
    stream.append("def ").append(name.value).append("(").append("):").append("\n")
    val newIndent = indent.increasedIndent
    newIndent.printIndent(stream)
    body.forEach {
      it.toSourceCode(stream, indent, multiLineMode)
    }
    stream.print('\n')
  }

  init {
    check(this.body.isNotEmpty())
  }
}
