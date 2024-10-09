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
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.util.Util
import org.perses.util.shell.Shells
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.appendText
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readText

@RunWith(JUnit4::class)
class ReductionScriptTemplateTest {

  val tempDir = Files.createTempDirectory(this::class.java.canonicalName)

  @After
  fun teardown() {
    tempDir.deleteRecursively()
  }

  @Test
  fun test() {
    val root = Files.createDirectories(tempDir.resolve("root"))

    val relPath1 = Paths.get("a/b/c/BUILD")
    val relPath2 = Paths.get("e/f/g/BUILD")

    val file1 = Util.createDirsAndWriteText(
      root.resolve(relPath1),
      "$relPath1",
    )

    val file2 = Util.createDirsAndWriteText(
      root.resolve(relPath2),
      "$relPath2",
    )
    val criteriaScript = BuildOptTestUtil.createScript(
      tempDir.resolve("criteria.sh"),
    ).apply {
      this.file.appendText(
        """
          |
          |
          |[ -e "${root.fileName}/$relPath1" ] && [ -e "${root.fileName}/$relPath2" ]
        """.trimMargin(),
      )
    }
    criteriaScript.let {
      val cmd = Shells.singleton.run(
        cmd = "${it.shebang} ${it.baseName}",
        workingDirectory = it.file.parent,
        captureOutput = true,
        environment = Shells.CURRENT_ENV,
      )
      assertThat(cmd.exitCode.intValue).isEqualTo(0)
      assertThat(file1.readText()).contains(relPath1.toString())
      assertThat(file2.readText()).contains(relPath2.toString())
    }

    val testDir = Files.createDirectories(tempDir.resolve("test"))

    val reductionScript = ReductionScriptTemplate(
      optCriteriaScript = criteriaScript,
      rootDirectory = root,
      buildFiles = ImmutableList.of(file1, file2),
    ).writeTo(testDir.resolve("r.sh"))

    val testFile1 = testDir.resolve(relPath1)
    Util.createDirsAndWriteText(testFile1, "test1")
    val testFile2 = testDir.resolve(relPath2)
    Util.createDirsAndWriteText(testFile2, "test2")

    val cmd = Shells.singleton.run(
      cmd = "${reductionScript.shebang} ${reductionScript.baseName}",
      workingDirectory = reductionScript.file.parent,
      captureOutput = true,
      environment = Shells.CURRENT_ENV,
    )
    assertThat(cmd.exitCode.intValue).isEqualTo(0)
    assertThat(file1.readText()).isEqualTo(testFile1.readText())
    assertThat(file2.readText()).isEqualTo(testFile2.readText())
  }
}
