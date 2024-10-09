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

import java.nio.file.Files
import java.util.Random

class JavaFuzzer(
  private val options: CommandOptions,
) {
  init {
    require(options.fuzzerFlags.outputFolder != null)
    if (!Files.exists(options.fuzzerFlags.outputFolder)) {
      Files.createDirectory(options.fuzzerFlags.outputFolder)
    }
  }

  fun run() {
    if (options.fuzzerFlags.seed != null) {
      TestInstance(options.fuzzerFlags.seed!!, options.fuzzerFlags.outputFolder!!, 1).run()
    } else {
      val currentGen = Random()
      for (iteration in 0 until options.fuzzerFlags.iteration) {
        val seed = currentGen.nextLong()
        TestInstance(seed, options.fuzzerFlags.outputFolder!!, options.fuzzerFlags.repetition).run()
      }
    }
  }
}
