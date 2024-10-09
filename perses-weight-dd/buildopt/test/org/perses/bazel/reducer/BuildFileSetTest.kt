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

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.bazel.reducer.BuildOptTestUtil.DefaultInputs.CONTENT
import org.perses.bazel.reducer.BuildOptTestUtil.DefaultInputs.RELATIVE_PATH_1
import org.perses.bazel.reducer.BuildOptTestUtil.DefaultInputs.RELATIVE_PATH_2
import java.nio.file.Files
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readText

@RunWith(JUnit4::class)
class BuildFileSetTest {

  val tempDir = Files.createTempDirectory(this::class.java.canonicalName)

  val testScript = BuildOptTestUtil.createScript(tempDir.resolve("r.sh"))

  @After
  fun teardown() {
    tempDir.deleteRecursively()
  }

  @Test
  fun test() {
    val root = Files.createDirectories(tempDir.resolve("root"))

    val inputs = BuildOptTestUtil.DefaultInputs.create(
      testScript,
      root,
    )

    val buildFileSet = BuildFileSet.create(inputs)
    val testDir = Files.createDirectories(tempDir.resolve("test"))
    buildFileSet.writeToDirectory(testDir)
    assertThat(Files.isRegularFile(testDir.resolve(RELATIVE_PATH_1))).isTrue()
    assertThat(Files.isRegularFile(testDir.resolve(RELATIVE_PATH_2))).isTrue()
    assertThat(testDir.resolve(RELATIVE_PATH_1).readText()).isEqualTo(CONTENT)
    assertThat(testDir.resolve(RELATIVE_PATH_2).readText()).isEqualTo(CONTENT)
  }
}
