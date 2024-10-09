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
package org.pluverse.chaoty.ast

import org.perses.util.ast.IPrecedence
import org.perses.util.ast.Indent
import org.pigen.python.ast.AbstractPythonAst
import org.pigen.python.ast.Precedence
import java.io.PrintStream

class TaichiProgram(var backend: EnumTaichiBackend) : AbstractPythonAst() {

  private val taichiModule = TaichiModule()

  override val precedence: IPrecedence
    get() = Precedence.UNIT

  override fun toSourceCode(stream: PrintStream, indent: Indent, multiLineMode: Boolean) {
    taichiModule.createImportStmtAst().toSourceCode(
      stream,
      indent,
      multiLineMode,
    )
    stream.print('\n')
    taichiModule.createInitStmtAst(backend).toSourceCode(
      stream,
      indent,
      multiLineMode,
    )
    stream.print('\n')
  }
}
