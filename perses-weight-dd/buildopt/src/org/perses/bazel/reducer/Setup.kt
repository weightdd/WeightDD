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
import com.google.common.flogger.FluentLogger
import org.perses.bazel.reducer.io.BazelReductionInputs
import org.perses.program.ScriptFile
import org.perses.program.SourceFile
import org.perses.util.AutoIncrementDirectory
import org.perses.util.TimeUtil
import org.perses.util.Util
import org.perses.util.ktInfo
import org.perses.util.toImmutableList
import java.nio.file.Files
import java.nio.file.Path

class Setup(
  currentWorkingDir: Path,
  criteriaScript: ScriptFile,
  cmdOptions: CommandOptions,
) {

  val tempDir = Util.getSystemTemporaryDirectory().resolve("org.perses.bazel.opt").apply {
    Files.createDirectories(this)
  }

  init {
    logger.ktInfo { "Creating setup for $criteriaScript in $tempDir" }
  }

  val workingDir = AutoIncrementDirectory
    .computeAndCreate(
      workingDir = Files.createTempDirectory(""),
      defaultDirName = DEFAULT_SETUP_DIR_NAME + "_" +
        TimeUtil.formatDateForFileName(System.currentTimeMillis()),
    ).also {
      check(Files.isDirectory(it))
      check(Util.isEmptyDirectory(it))
      logger.ktInfo { "The working directory of the setup is $it" }
    }

  val reductionScriptTemplate = ReductionScriptTemplate(
    optCriteriaScript = criteriaScript,
    rootDirectory = currentWorkingDir,
    buildFiles = globBuildFiles(
      cmdOptions.compulsory.locations,
    ).map { it.file }.toImmutableList(),
  )

  val testScript = reductionScriptTemplate.writeTo(workingDir.resolve("r.sh"))

  init {
    reductionScriptTemplate
      .absoluteAndRelativePathPairSequence()
      .forEach { (absolutePath, relativePath) ->
        assert(absolutePath.isAbsolute)
        assert(!relativePath.isAbsolute)
        assert(absolutePath.endsWith(relativePath))
        Files.copy(absolutePath, workingDir.resolve(relativePath))
      }
  }

  val reductionInputs = BazelReductionInputs(
    testScript = testScript,
    rootDirectory = tempDir,
    buildFiles = globBuildFiles(tempDir),
  )

  init {
    assert(
      reductionInputs.orig2relativePathPairs
        .asSequence().map { it.relativePath }.sorted().toList() ==
        reductionScriptTemplate.absoluteAndRelativePathPairSequence()
          .map { it.second }.sorted().toList(),
    ) {
    }
  }

  companion object {

    private const val DEFAULT_SETUP_DIR_NAME = "perses_build_optimizer"

    private val logger = FluentLogger.forEnclosingClass()

    @JvmStatic
    fun globBuildFiles(dirs: List<Path>): ImmutableList<SourceFile> {
      return dirs.asSequence()
        .flatMap {
          Util.globWithRegex(it, Regex("BUILD")).asSequence()
        }.distinct()
        .map { SourceFile(it, LanguageStarlark) }
        .toImmutableList()
    }

    @JvmStatic
    fun globBuildFiles(dir: Path): ImmutableList<SourceFile> {
      return globBuildFiles(listOf(dir))
    }
  }
}
