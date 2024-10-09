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
package org.perses.bazel.reducer.io

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.bazel.reducer.BuildFileSet
import org.perses.bazel.reducer.BuildOptTestUtil
import org.perses.reduction.io.ReductionFolder
import org.perses.util.Util
import java.nio.file.Files
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readText

@RunWith(JUnit4::class)
class BazelOutputManagerFactoryTest {

  val tempDir = Files.createTempDirectory(this::class.java.canonicalName)
  val script = BuildOptTestUtil.createScript(tempDir.resolve("r.sh"))
  val root = tempDir.resolve("root").apply { Files.createDirectories(this) }
  val inputs = BuildOptTestUtil.DefaultInputs.create(script, root)
  val buildFileSet = BuildFileSet.create(inputs)

  val factory = BazelOutputManagerFactory(inputs)

  @After
  fun teardown() {
    tempDir.deleteRecursively()
  }

  @Test
  fun test() {
    val manager = factory.createManagerFor(buildFileSet)
    val testDir = Files.createDirectories(tempDir.resolve("test"))
    assertThat(Util.isEmptyDirectory(testDir)).isTrue()
    val reductionFolder = ReductionFolder(reductionInputs = inputs, testDir)
    manager.write(reductionFolder)
    reductionFolder.deleteAllOtherFiles()
    assertThat(
      reductionFolder.folder.resolve(BuildOptTestUtil.DefaultInputs.RELATIVE_PATH_1).readText(),
    ).isEqualTo(BuildOptTestUtil.DefaultInputs.CONTENT)
    assertThat(
      reductionFolder.folder.resolve(BuildOptTestUtil.DefaultInputs.RELATIVE_PATH_2).readText(),
    ).isEqualTo(BuildOptTestUtil.DefaultInputs.CONTENT)
    assertThat(Files.isRegularFile(reductionFolder.folder.resolve(script.baseName))).isTrue()
  }
}
