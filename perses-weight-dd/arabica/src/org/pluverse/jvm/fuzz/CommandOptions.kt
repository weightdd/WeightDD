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
package org.pluverse.jvm.fuzz

import com.beust.jcommander.Parameter
import org.perses.util.cmd.AbstractCommandLineFlagGroup
import org.perses.util.cmd.AbstractCommandOptions
import java.nio.file.Path

class CommandOptions : AbstractCommandOptions() {
  @JvmField
  val fuzzerFlags = registerFlags(FuzzerFlagGroup())
}

class FuzzerFlagGroup : AbstractCommandLineFlagGroup(groupName = "JVM Fuzz Control") {
  @Parameter(
    names = ["--output"],
    description = "The path to save outputted logs in crash/failure",
    required = true,
  )
  var outputFolder: Path? = null

  @Parameter(
    names = ["--iteration"],
    description = "The number of iterations to run the tool",
    required = false,
  )
  var iteration: Int = 1

  @Parameter(
    names = ["--repetition"],
    description = "The number of times the same jar is invoked",
    required = false,
  )
  var repetition: Int = 1

  @Parameter(
    names = ["--seed"],
    description = "The seed for fuzzer, tool is only run once",
    required = false,
  )
  var seed: Long? = null

  override fun validate() {
    check(iteration >= 0)
  }
}
