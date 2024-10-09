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
package org.perses.delta

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.perses.delta.xfs.BfsDeltaDebugger
import org.perses.delta.xfs.DeltaDebugger
import org.perses.reduction.PropertyTestResult

@RunWith(JUnit4::class)
class DeltaDebuggerFactoryTest {

  val args = AbstractDeltaDebugger.Arguments(
    needToTestEmpty = true,
    input = ImmutableList.of("a"),
    propertyTester = IPropertyTester {
      PropertyTestResultWithPayload(PropertyTestResult.INTERESTING_RESULT, "payload")
    },
    onBestUpdateHandler = { _, _ -> },
    descriptionPrefix = "",
  )

  @Test
  fun testCreationWithEnum() {
    DeltaDebuggerFactory.create(EnumDeltaDebuggerType.BFS, args).let {
      assertThat(it).isInstanceOf(BfsDeltaDebugger::class.java)
    }
    DeltaDebuggerFactory.create(EnumDeltaDebuggerType.PERSES_VARIANT_OF_PRISTINE, args).let {
      assertThat(it).isInstanceOf(DeltaDebugger::class.java)
    }
  }

  @Test
  fun testCreationWithClassName() {
    DeltaDebuggerFactory.create(BfsDeltaDebugger::class.java.canonicalName, args).let {
      assertThat(it).isInstanceOf(BfsDeltaDebugger::class.java)
    }
  }
}
