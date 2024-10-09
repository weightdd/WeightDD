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

import com.beust.jcommander.Parameter
import org.perses.util.cmd.AbstractCommandLineFlagGroup
import org.perses.util.cmd.AbstractCommandOptions
import java.nio.file.Files
import java.nio.file.Path

class CommandOptions : AbstractCommandOptions() {

  val compulsory = registerFlags(CompulsoryFlagGroup())

  class CompulsoryFlagGroup : AbstractCommandLineFlagGroup("Bazel Reducer Control") {

    @JvmField
    @Parameter(
      required = true,
      description = "The files and directories to apply build optimization",
      order = FlagOrder.COMPULSORY + 100,
    )
    var locations = ArrayList<Path>()

    @JvmField
    @Parameter(
      names = ["--test-script"],
      required = true,
      description = "The files and directories to apply build optimization",
      order = FlagOrder.COMPULSORY + 200,
    )
    var testScript: Path? = null

    override fun validate() {
      require(locations.isNotEmpty())
      require(testScript != null && Files.isRegularFile(testScript))
    }
  }
}

private object FlagOrder {
  const val COMPULSORY = 0
}
