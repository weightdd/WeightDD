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

import org.perses.bazel.reducer.io.BazelReductionInputs
import org.perses.program.ScriptFile
import org.perses.program.SourceFile
import org.perses.util.Util
import org.perses.util.toImmutableList
import java.nio.file.Files
import java.nio.file.Path

object BuildOptTestUtil {

  fun createScript(path: Path): ScriptFile {
    return ScriptFile(
      Util.createDirsAndWriteText(path, "#!/usr/bin/env bash").apply {
        Util.setExecutable(this)
      },
    )
  }

  fun createInputs(
    testScript: ScriptFile,
    rootDir: Path,
    relativePath2Content: Map<String, String>,
  ): BazelReductionInputs {
    Files.createDirectories(rootDir)
    val buildFiles = relativePath2Content.entries
      .asSequence()
      .map {
        val relativePath = it.key
        val content = it.value

        Util.createDirsAndWriteText(rootDir.resolve(relativePath), content).let {
          SourceFile(it, LanguageStarlark)
        }
      }.toImmutableList()
    return BazelReductionInputs(
      testScript = testScript,
      rootDir,
      buildFiles,
    )
  }

  object DefaultInputs {
    const val RELATIVE_PATH_1 = "a/b/c/d/BUILD"
    const val RELATIVE_PATH_2 = "c/d/e/f/BUILD"
    const val CONTENT = "#empty"

    fun create(testScript: ScriptFile, rootDir: Path): BazelReductionInputs {
      return BuildOptTestUtil.createInputs(
        testScript,
        rootDir,
        mapOf(
          RELATIVE_PATH_1 to CONTENT,
          RELATIVE_PATH_2 to CONTENT,
        ),
      )
    }
  }
}
