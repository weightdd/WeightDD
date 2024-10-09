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
import org.perses.program.ScriptFile
import org.perses.util.AbstractBashScriptGenerator
import org.perses.util.toImmutableList
import java.nio.file.Files
import java.nio.file.Path

class ReductionScriptTemplate(
  val optCriteriaScript: ScriptFile,
  val rootDirectory: Path,
  buildFiles: ImmutableList<Path>,
) : AbstractBashScriptGenerator() {

  private val absBuildFiles = buildFiles
    .asSequence()
    .map { it.toAbsolutePath() }
    .sorted()
    .toImmutableList()

  init {
    require(buildFiles.isNotEmpty())
    absBuildFiles.forEach {
      require(Files.isRegularFile(it))
      require(it.startsWith(rootDirectory))
    }
  }

  private val relativeBuildFiles = absBuildFiles
    .asSequence()
    .map { rootDirectory.relativize(it) }
    .toImmutableList()

  fun absoluteAndRelativePathPairSequence(): Sequence<Pair<Path, Path>> {
    assert(absBuildFiles.size == relativeBuildFiles.size)
    return absBuildFiles.asSequence().zip(relativeBuildFiles.asSequence())
  }

  override fun generateCode(lines: ArrayList<String>) {
    lines.add("# Enable tracing")
    lines.add("set -o pipefail")
    lines.add("set -o nounset")
    lines.add("set -o errexit")
    lines.add("set -o xtrace")

    lines.add("# Copy BUILD files.")
    relativeBuildFiles.forEach {
      lines.add("""cp "$it" "$rootDirectory/$it" """)
    }

    lines.add("# Call the criteria script in its own directory.")
    lines.add("readonly OLD_PWD=${'$'}(pwd)")
    val criteriaScriptParentAbs = optCriteriaScript.file.toAbsolutePath().parent
    lines.add("""cd $criteriaScriptParentAbs || exit 1""")
    lines.add("""./${optCriteriaScript.baseName} || exit 1 """)
    lines.add("""cd "${'$'}{OLD_PWD}" || exit 1 """)
  }
}
