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
package org.pluverse.jvm.fuzz.util

import java.util.Random

class ProbabilityTable<T>(
  val input: ArrayList<Pair<T, Int>>,
) {

  private fun createLinkedHashMap(): LinkedHashMap<T, Int> {
    val hashTable = LinkedHashMap<T, Int>()
    for (pair in input) {
      hashTable.put(pair.first, pair.second)
    }
    return hashTable
  }

  private val hashTable = createLinkedHashMap()

  private val keyTable = hashTable.keys.toList()

  private fun generateIndices(index: Int, key: T): ArrayList<Int> {
    val list = ArrayList<Int>()
    for (i in 0..hashTable[key]!! - 1) {
      list.add(index)
    }
    return list
  }

  private val indexArray = ArrayList(
    keyTable.flatMapIndexed { index, key ->
      generateIndices(index, key)
    },
  )

  fun getRandom(
    subset: Set<T>? = null,
    exclude: Set<T>? = null,
    generator: Random = Random(),
  ): T {
    if (subset != null) {
      val subsetIndex = indexArray.filter { subset.contains(keyTable[it]) }
      return keyTable[subsetIndex[generator.nextInt(subsetIndex.size)]]
    }

    if (exclude != null) {
      val subsetIndex = indexArray.filter { !exclude.contains(keyTable[it]) }
      return keyTable[subsetIndex[generator.nextInt(subsetIndex.size)]]
    }
    return keyTable[indexArray[generator.nextInt(indexArray.size)]]
  }

  fun setValue(key: T, value: Int) {
    val index = indexArray.first { keyTable[it] == key }
    indexArray.removeAll(listOf(index))
    for (i in 0..value - 1) {
      indexArray.add(index)
    }
  }

  fun getKeys(): List<T> {
    return keyTable
  }
}
