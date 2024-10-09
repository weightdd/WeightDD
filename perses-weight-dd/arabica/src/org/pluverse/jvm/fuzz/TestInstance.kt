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

import com.google.common.collect.ImmutableList
import org.perses.util.AutoDeletableFolder
import org.perses.util.java.JarFile
import org.perses.util.java.JarPackager
import org.perses.util.java.JavacWrapper
import org.pluverse.jvm.fuzz.basic.GlobalContext
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Random
import kotlin.io.path.appendText
import kotlin.io.path.writeText

class TestInstance(
  val randomSeed: Long,
  val outputFolder: Path,
  val repetition: Int,
) {
  private val testFolder = AutoDeletableFolder.createTempDir(
    TEST_FOLDER_PREFIX + randomSeed.toString(),
  )

  private val jarFile = testFolder.file.resolve("$TEST_CLASS_NAME.jar")

  private fun generateTestFile(generatedProgram: String): Path {
    val testFile = testFolder.file.resolve(TEST_CLASS_NAME + ".java")
    testFile.writeText(
      generatedProgram,
    )
    return testFile
  }

  private fun createJarFile() {
    val packager = JarPackager(
      testFolder.file,
      packageName = PACKAGE_NAME,
      { file ->
        file.endsWith(".java") || file.endsWith(".class")
      },
      customizer = {},
    )
    packager.createJarFile(jarFile)
  }

  fun run() {
    var generatedProgram = ""
    try {
      generatedProgram = GlobalContext(randomSeed).generate()
      JavacWrapper(
        ImmutableList.of(
          generateTestFile(generatedProgram),
        ),
      ).use { it.compile() }

      createJarFile()

      JarFile(
        path = jarFile,
        mainClassFullName = PACKAGE_NAME + "." + TEST_CLASS_NAME,
      ).use {
        var previousOutput = ""
        for (repetition in 0 until repetition) {
          val mainClass = it.loadMainClass()
          if (repetition == 0) {
            mainClass.getMethod("loadClass").invoke(null)
          }
          val result = mainClass.getMethod(
            "invokeMethod",
            Array<String>::class.java,
          ).invoke(null, arrayOf<String>())
          if (result != previousOutput && previousOutput != "") {
            throw RuntimeException(
              "Output does not match\n output one \n" + result + "output two \n" + previousOutput,
            )
          }
          mainClass.getMethod("resetStatic").invoke(null)
          previousOutput = result as String
        }
        it.close()
      }
    } catch (e: Throwable) {
      Files.createDirectory(
        outputFolder.resolve(
          randomSeed.toString() + "Failure",
        ),
      ).apply {
        Files.createFile(
          this.resolve(
            "FuzzerTest" + randomSeed.toString() + "Program.java",
          ),
        ).apply {
          this.writeText(generatedProgram)
        }

        Files.createFile(
          this.resolve(
            "ErrorOutput" + randomSeed.toString() + ".out",
          ),
        ).apply {
          val stringWriter = StringWriter()
          val printWriter = PrintWriter(stringWriter)
          if (e is InvocationTargetException) {
            val targetException = e.targetException
            targetException.printStackTrace(printWriter)
          } else {
            e.printStackTrace(printWriter)
          }
          this.writeText("Failure has occured for seed number: " + randomSeed)
          this.appendText(Random(randomSeed).nextInt().toString())
          this.appendText(stringWriter.toString())
        }
      }
    }
    testFolder.close()
  }

  companion object {
    const val TEST_FOLDER_PREFIX = "JavaTest_"
    const val TEST_CLASS_NAME = "Test"
    const val PACKAGE_NAME = "org.pluverse.jvm.fuzz"
  }
}
