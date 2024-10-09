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

import com.google.common.collect.ImmutableList
import org.perses.bazel.reducer.io.BazelReductionInputs
import org.perses.util.Util
import org.perses.util.toImmutableList
import java.nio.file.Path
import kotlin.io.path.readText

class BuildFile internal constructor(
  val relativePath: Path,
  val content: String,
) {

  fun writeToDirectory(dir: Path) {
    Util.createDirsAndWriteText(dir.resolve(relativePath), content)
  }

  companion object {
    fun parse(file: Path, relativePath: Path) = BuildFile(
      relativePath = relativePath,
      content = file.readText(),
    )
  }
}

class BuildFileSet(val buildFiles: ImmutableList<BuildFile>) {

  init {
    require(
      buildFiles
        .asSequence()
        .map { it.relativePath.toAbsolutePath().toString() }
        .toSet()
        .size == buildFiles.size,
    ) {
      "Duplicate entries in $buildFiles"
    }
  }

  fun writeToDirectory(dir: Path) {
    buildFiles.forEach { buildFile ->
      buildFile.writeToDirectory(dir)
    }
  }

  companion object {

    fun create(inputs: BazelReductionInputs): BuildFileSet {
      return BuildFileSet(
        inputs.orig2relativePathPairs
          .asSequence()
          .map {
            val orig = it.origFile
            BuildFile.parse(orig.file, it.relativePath)
          }.toImmutableList(),
      )
    }
  }
}
