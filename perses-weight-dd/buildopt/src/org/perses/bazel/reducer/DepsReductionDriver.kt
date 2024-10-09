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
package org.perses.bazel.reducer

import com.google.common.flogger.FluentLogger
import org.perses.bazel.reducer.io.BazelOutputManagerFactory
import org.perses.bazel.reducer.io.BazelReductionIOManager
import org.perses.program.LanguageKind
import org.perses.program.ScriptFile
import org.perses.reduction.AbstractReductionDriver
import org.perses.util.shell.Shells

class DepsReductionDriver private constructor(
  val setup: Setup,
) : AbstractReductionDriver<BuildFileSet, LanguageKind, BazelReductionIOManager>(
  BazelReductionIOManager(
    workingDir = setup.workingDir,
    reductionInputs = setup.reductionInputs,
    outputManagerFactory = BazelOutputManagerFactory(setup.reductionInputs),
    outputDirectory = null,
  ),
  SINGLE_THREAD_ONLY,
  scriptExecutionTimeoutInSeconds = 300L,
  keepWaitingAfterScriptTimeout = true,
) {

  val originalBuildFiles = BuildFileSet.create(ioManager.getConcreteReductionInputs())

  override fun reduce() {
  }

  companion object {

    const val SINGLE_THREAD_ONLY = 1

    @JvmStatic
    fun create(
      options: CommandOptions,
    ): DepsReductionDriver {
      val currentWorkingDir = Shells.CURRENT_DIR
      val setup = Setup(
        currentWorkingDir,
        ScriptFile(options.compulsory.testScript!!),
        options,
      )
      return DepsReductionDriver(setup)
    }

    val logger = FluentLogger.forEnclosingClass()
  }
}
