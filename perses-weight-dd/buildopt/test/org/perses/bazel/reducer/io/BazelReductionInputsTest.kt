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

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.bazel.reducer.LanguageStarlark
import org.perses.program.ScriptFile
import org.perses.program.SourceFile
import org.perses.util.Util
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

@RunWith(JUnit4::class)
class BazelReductionInputsTest {

  val tempDir = Files.createTempDirectory(this::class.java.canonicalName)
  val script = ScriptFile(
    Files.createFile(tempDir.resolve("r.sh")).apply {
      this.writeText("#!/usr/bin/env bash")
      Util.setExecutable(this)
    },
  )

  val root = Files.createDirectories(tempDir.resolve("root"))
  val relDir123 = "1/2/3"
  val dir123 = Files.createDirectories(root.resolve(relDir123))
  val relDir456 = "4/5/6"
  val dir456 = Files.createDirectories(root.resolve(relDir456))
  val relDir156 = "1/5/6"
  val dir156 = Files.createDirectories(root.resolve(relDir156))

  val file123 = Files.createFile(dir123.resolve("123.txt"))
  val file456 = Files.createFile(dir456.resolve("456.txt"))
  val file156 = Files.createFile(dir156.resolve("156.txt"))

  val sourceFile123 = SourceFile(file123, LanguageStarlark)
  val sourceFile456 = SourceFile(file456, LanguageStarlark)
  val sourceFile156 = SourceFile(file156, LanguageStarlark)

  val inputs = BazelReductionInputs(
    testScript = script,
    rootDirectory = root,
    buildFiles = ImmutableList.of(sourceFile123, sourceFile456, sourceFile156),
  )

  @After
  fun teardown() {
    tempDir.deleteRecursively()
  }

  @Test
  fun testAbsoluteRootDirectory() {
    assertThat(inputs.absoluteRootDirectory.toAbsolutePath()).isEqualTo(
      root.toAbsolutePath(),
    )
  }

  @Test
  fun testLanguage() {
    assertThat(inputs.mainDataKind).isEqualTo(LanguageStarlark)
  }

  @Test
  fun testComputeAbsolutePathListWrtNewDir() {
    val newFolder = tempDir.resolve("new_root")
    assertThat(
      inputs.computeAbsPathListWrt(newFolder),
    ).containsExactly(
      newFolder.resolve(relDir123).resolve(file123.fileName),
      newFolder.resolve(relDir456).resolve(file456.fileName),
      newFolder.resolve(relDir156).resolve(file156.fileName),
    ).inOrder()
  }

  @Test
  fun testComputeAbsPathWrt() {
    val newFolder = tempDir.resolve("new_root")
    assertThat(
      inputs.computeAbsPathWrt(sourceFile123, newFolder),
    ).isEqualTo(newFolder.resolve(relDir123).resolve(file123.fileName))
  }

  @Test
  fun testRelativePathSequence() {
    assertThat(inputs.relativePathSequence().toList())
      .containsExactly(
        Paths.get(relDir123).resolve(file123.fileName),
        Paths.get(relDir456).resolve(file456.fileName),
        Paths.get(relDir156).resolve(file156.fileName),
      ).inOrder()
  }
}
