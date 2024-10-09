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
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.util.Util
import java.nio.file.Files

@RunWith(JUnit4::class)
class SetupTest {

  val tempDir = Files.createTempDirectory(this::class.java.canonicalName)

  @Test
  fun testSingleFile() {
    val file = Util.createDirsAndWriteText(tempDir.resolve("a/b/c/BUILD"), "#empty")
    val result = Setup.globBuildFiles(listOf(file))
    assertThat(result).hasSize(1)
    assertThat(Files.isSameFile(result.first().file, file)).isTrue()
  }

  // This test should be removed when we support multiple-file globbing.
  @Test
  fun testMultipleFilesGlobbing() {
    val file1 = Util.createDirsAndWriteText(tempDir.resolve("a/BUILD"), "#empty")
    val file2 = Util.createDirsAndWriteText(tempDir.resolve("b/BUILD"), "#empty")
    val result = Setup.globBuildFiles(listOf(file1, file2)).map { it.file }
    assertThat(result).containsExactly(
      file1,
      file2,
    )
  }
}
