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
import org.perses.util.forEachElementAndGap
import java.io.PrintStream

class TupleExprAst(
  elements: Iterable<AbstractPythonExprAst>,
) : AbstractPythonExprAst() {

  init {
    require(elements.firstOrNull() != null)
  }

  private val _elements = ArrayList<AbstractPythonExprAst>().apply {
    addAll(elements)
  }

  val elements: Iterable<AbstractPythonExprAst>
    get() = _elements

  override val precedence: IPrecedence
    get() = Precedence.PARENTHESES

  override fun toSourceCode(stream: PrintStream, indent: Indent, multiLineMode: Boolean) {
    stream.print('(')
    elements.forEachElementAndGap(
      elementVisitor = { it.toSourceCode(stream, indent, multiLineMode) },
      gapVisitor = { stream.print(", ") },
    )
    stream.print(')')
  }
}
