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

class WeightedDeltaDebugger<T, Payload>(
  args: Arguments<T, Payload>,
  private val weightProvider: WeightSplitDeltaDebugger.IWeightProvider<T>,
) : PristineDeltaDebugger<T, Payload>(args, false, false, weightProvider) {

  val print = false

  override fun createElementWrapperFor(index: Int, element: T): ElementWrapper<T> {
    val weight = weightProvider.weight(element)
    return ElementWrapper(index, element, weight)
  }

  override fun partition(
    list: ImmutableList<ElementWrapper<T>>,
    numberOfPartitions: Int,
  ): PartitionList<ElementWrapper<T>> {

    require(numberOfPartitions > 0) { "The number of partitions has to be positive." }
    require(list.size > 0) { "The list cannot be empty." }

    return partition1(list, numberOfPartitions)

  }

  fun partition_even(list: ImmutableList<ElementWrapper<T>>, numberOfPartitions: Int) : PartitionList<ElementWrapper<T>> {
    val size = list.size
    if(print) println("list size: $size")

    val average = computeSum(list) / numberOfPartitions
    if(print) println("Average: $average")

    // make sure "one-minimal" of ddmin
//    if(list.size <= numberOfPartitions) {
//      val builder = PartitionList.Builder(list)
//      for (i in 0 until size) {
//        builder.createPartition(i, rightExclusive = i + 1)
//        if(print) println("partition: [$i ${i + 1}] weight: ${getWeight(list, i)}")
//      }
//      if(print) println("[Number of partitions: $numberOfPartitions]")
//      if(print) println("[Real Number of partitions: $size]")
//      if(print) println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
//      return builder.build()
//    }

    val builder = PartitionList.Builder(list)
    var i = 0
    var numPartitions = 0
    while (i < size) {
      var currentSum = getWeight(list, i)
      var j = i + 1
      while (j < size) {
        val currentWeight = getWeight(list, j)
        if (currentSum + currentWeight > average) {
          break
        }
        currentSum += currentWeight
        ++j
      }
      builder.createPartition(i, rightExclusive = j)
      if(print) println("partition: [$i $j] weight: $currentSum")
      numPartitions++
      i = j
    }
    if(print) println("[Number of partitions: $numberOfPartitions]")
    if(print) println("[Real Number of partitions: $numPartitions]")
    if(print) println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
    return builder.build()
  }

  fun partition_cn(list: ImmutableList<ElementWrapper<T>>, numberOfPartitions: Int,): PartitionList<ElementWrapper<T>> {
    val size = list.size
    if(print) println("list size: $size")

    val average = computeSum(list) / numberOfPartitions
    if(print) println("Average: $average")

    val builder = PartitionList.Builder(list)
    var startIndex = 0
    var numPartitions = 0
    while (startIndex < size) {
      var currentSum = getWeight(list, startIndex)
      var currentIndex = startIndex + 1
      while (currentIndex < size) {
        val currentWeight = getWeight(list, currentIndex)
        if (currentSum + currentWeight > average) {
          break
        }
        currentSum += currentWeight
        ++currentIndex
      }
      builder.createPartition(startIndex, rightExclusive = currentIndex)
      if(print) println("partition: [$startIndex $currentIndex] weight: $currentSum")
      numPartitions++
      startIndex = currentIndex
    }
    if(print) println("[Number of partitions: $numberOfPartitions]")
    if(print) println("[Real Number of partitions: $numPartitions]")
    return builder.build()
  }


  fun interface IWeightProvider<T> {
    fun weight(element: T): Int
  }

  companion object {
    fun <T> computeSum(list: ImmutableList<ElementWrapper<T>>): Int {
      return list.sumOf { getWeight(it) }
    }

    fun <T> getWeight(list: ImmutableList<ElementWrapper<T>>, index: Int): Int {
      return getWeight(list[index])
    }

    fun <T> getWeight(element: ElementWrapper<T>) = element.elementPayload as Int
  }

  // partition by dp, O(n^2 * k)
  fun partition_dp(list: ImmutableList<ElementWrapper<T>>, numberOfPartitions: Int,): PartitionList<ElementWrapper<T>> {
//    println("Partition by dp")
    val size = list.size
    val builder = PartitionList.Builder(list)
    if(numberOfPartitions >= size) {
      for (i in 0 until size) {
        builder.createPartition(i, rightExclusive = i + 1)
      }
      return builder.build()
    }

    val dp = Array(size + 1) { IntArray(numberOfPartitions + 1) {Int.MAX_VALUE} }
    val sum = Array(size + 1) { IntArray(numberOfPartitions + 1) }
    val sub = IntArray(size + 1)
    for(i in 0 until size) {
      sub[i+1] = sub[i] + getWeight(list, i)
    }
    dp[0][0] = 0
    for(i in 1..size) {
      for(j in 1..minOf(i, numberOfPartitions)) {
        for(k in 0 until i) {
          val cost = maxOf(dp[k][j-1], sub[i] - sub[k])
          if(cost < dp[i][j]) {
            dp[i][j] = cost
            sum[i][j] = k
          }
        }
      }
    }
    // backtrack to get the partition
    var i = size
    var j = numberOfPartitions
    val partition = Array(numberOfPartitions) { IntArray(2) }
    while(j > 0) {
      val k = sum[i][j]
      partition[j-1][0] = k
      partition[j-1][1] = i
      i = k
      j--
    }
    for(index in 0 until numberOfPartitions) {
      builder.createPartition(partition[index][0], rightExclusive = partition[index][1])
    }
    return builder.build()
  }

  // number of partitions == numberOfPartitions
  // partition weight *almost* equal to average
  fun partition0(list: ImmutableList<ElementWrapper<T>>, numberOfPartitions: Int): PartitionList<ElementWrapper<T>> {

    if(numberOfPartitions >= list.size) {
      val builder = PartitionList.Builder(list)
      for (i in 0 until list.size) {
        builder.createPartition(i, rightExclusive = i + 1)
      }
      return builder.build()
    }

    val size = list.size
    val average = computeSum(list) / numberOfPartitions
    if(print) println("Average: $average")
    val builder = PartitionList.Builder(list)
    var i = 0
    var numPartitions = 0;
    var numPartitionsLeft = numberOfPartitions
    while(i < size) {
      var currentSum = getWeight(list, i)
      var j = i + 1
      while(j < size) {
        if(size - j < numPartitionsLeft) { break }
        if(numPartitionsLeft <= 1) { j = size; break }
        val currentWeight = getWeight(list, j)
        if(currentSum + currentWeight >= average) {
          if(average - currentSum >= currentSum + currentWeight - average) { j++; }
          break
        }
        currentSum += currentWeight
        j++
      }
      builder.createPartition(i, rightExclusive = j)
      if(print) println("partition: [$i $j]")
      numPartitions++
      numPartitionsLeft--
      if(size - j < numPartitionsLeft) {
        for(k in j until size) {
          builder.createPartition(k, rightExclusive = k + 1)
          if(print) println("partition: [$k ${k + 1}]")
          numPartitions++
          numPartitionsLeft--
        }
        j = size
      }
      i = j
    }
    assert(numPartitionsLeft == 0)
    return builder.build()
  }

  // make sure the real number of partitions == numberOfPartitions
  fun partition1(list: ImmutableList<ElementWrapper<T>>, numberOfPartitions: Int,): PartitionList<ElementWrapper<T>> {

//    println("Partition by partition1")

    if(numberOfPartitions >= list.size) {
      val builder = PartitionList.Builder(list)
      for (i in 0 until list.size) {
        builder.createPartition(i, rightExclusive = i + 1)
      }
      return builder.build()
    }

    val size = list.size
    val average = computeSum(list) / numberOfPartitions
    if(print) println("Average: $average")
    val builder = PartitionList.Builder(list)
    var startIndex = 0
    var numPartitions = 0
    while (startIndex < size) {
      var currentSum = getWeight(list, startIndex)
      var currentIndex = startIndex + 1
      while (currentIndex < size) {
        if(size - currentIndex < numberOfPartitions - numPartitions) { break }
        val currentWeight = getWeight(list, currentIndex)
        if (currentSum + currentWeight > average) {
          break
        }
        currentSum += currentWeight
        ++currentIndex
      }
      builder.createPartition(startIndex, rightExclusive = currentIndex)
      numPartitions++

      if(numPartitions == numberOfPartitions - 1) {
        builder.createPartition(currentIndex, rightExclusive = size)
        numPartitions++
        currentIndex = size
      }

      if(size - currentIndex < numberOfPartitions - numPartitions) {
        for(k in currentIndex until size) {
          builder.createPartition(k, rightExclusive = k + 1)
          if(print) println("partition: [$k ${k + 1}] weight: ${getWeight(list, k)}")
          numPartitions++
        }
        currentIndex = size
      }

      if(print) println("partition: [$startIndex $currentIndex] weight: $currentSum")
      startIndex = currentIndex
    }
    if(print) println("[Number of partitions: $numberOfPartitions]")
    if(print) println("[Real Number of partitions: $numPartitions]")

    assert(numPartitions == numberOfPartitions)
    return builder.build()
  }
}
