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

import org.pigen.python.ast.AttributeExprAst
import org.pigen.python.ast.CallExprAst
import org.pigen.python.ast.ExprStmtAst
import org.pigen.python.ast.Identifier
import org.pigen.python.ast.ImportStmtAst
import org.pigen.python.ast.KeywordArgument
import org.pigen.python.ast.toIdentifier
import org.pigen.python.ast.toNameExprAst

class TaichiModule(
  val alias: Identifier = "ti".toIdentifier(),
) {

  val moduleName: Identifier = "taichi".toIdentifier()
  val attrInit = AttributeExprAst(
    value = moduleName.toNameExprAst(),
    attribute = "init".toIdentifier(),
  )

  fun createImportStmtAst(): ImportStmtAst {
    return ImportStmtAst.createWithName(moduleName, alias)
  }

  fun createInitStmtAst(backend: EnumTaichiBackend): ExprStmtAst {
    return ExprStmtAst(
      CallExprAst(
        function = attrInit,
        arguments = listOf(),
        keywords = listOf(
          KeywordArgument(
            argumentName = "arch".toIdentifier(),
            value = backend.createAst(alias),
          ),
        ),
      ),
    )
  }
}
