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
import org.perses.util.Util.lazyAssert

abstract class AbstractDefaultDeltaDebugger<T, PropertyPayload, ElementPayload>(
  arguments: Arguments<T, PropertyPayload>,
  enableCache: Boolean = false,
  enableCacheRefresh: Boolean = false,
  private val weightProvider: WeightSplitDeltaDebugger.IWeightProvider<T>,
) : AbstractDeltaDebugger<T, PropertyPayload> (arguments) {

  // TODO @zxt: change this back after finishing running benchmarks
  private val cache: AbstractConfigCache<T> = if (enableCache) {
    ConfigCache(enableCacheRefresh)
  } else {
    ConfigCache(false)
  }
//  private val cache: AbstractConfigCache<T> = ConfigCache(false)

  open val print_detail = false
  open val print_stat = true

  open var num_of_tests = 0

  override fun createElementWrapperFor(index: Int, element: T): ElementWrapper<T> {
    val weight = weightProvider.weight(element)
    return ElementWrapper(index, element, weight)
  }

  override fun reduceNonEmptyInput() {

    if(print_detail) {
      println("***********************************************************************************************************************************************")
      println("******************************************* [Start reduceNonEmptyInput, size of best: ${best.size}] *******************************************")
      println("***********************************************************************************************************************************************\n")
    }

    if(print_stat) {
      print("weight_list(before reduction): [")
      for(element in best) {
        print("${getWeight(element)}, ")
      }
      println("]\n")
    }
    val bestLengthBeforeReduction = best.size
    val sumOfWeightBeforeReduction = computeSum(best)
    val averageWeightOfBest = WeightSplitDeltaDebugger.computeSum(best) / best.size.toDouble()

    val startTime = System.currentTimeMillis()

    var numOfPartitions = 2
    var prevPartitionList: PartitionList<ElementWrapper<T>>? = null
    while (best.size > 1 && best.size >= numOfPartitions) {
      lazyAssert({ best.size >= numOfPartitions }) { "$best, $numOfPartitions" }
      check(numOfPartitions > 1) {
        "The number of partitions should be >1. #best=${best.size}"
      }
      log { "Number of partitions: $numOfPartitions" }
      val partitionList = partition(best, numOfPartitions)

      if(print_detail) {
        println("Number of partitions: $numOfPartitions")
      }

      lazyAssert {
        partitionList.partitions.size != 1 || partitionList.partitions.single().elements == best
      }

      if (partitionList.isEquivalentTo(prevPartitionList)) {
        break
      } else {
        prevPartitionList = partitionList
      }
      log {
        buildString {
          val partitions = partitionList.partitions
          val size = partitions.size
          appendLine("Partition size: $size")
          for (i in 0 until size) {
            val partition = partitions[i].elements.map { it.index }
            appendLine("--Partition $i: $partition")
          }
        }
      }
      if (arguments.partitionComplementControl.enableReducingPartitions) {
        if (reducePartitions(partitionList)) {
          numOfPartitions = 2
          continue
        }
      }
      if (arguments.partitionComplementControl.enableReducingComplements) {
        val count = reduceComplements(partitionList)
        if(count > 0) {
          numOfPartitions = (2 * (numOfPartitions - count)).coerceAtMost(best.size)
          continue
        }
      }
      if (best.size == numOfPartitions) {
        break
      } else {
        numOfPartitions = (2 * numOfPartitions).coerceAtMost(best.size)
      }
    }

    val endTime = System.currentTimeMillis()

    if(print_stat) {

      print("weight_list(after reduction): [")
      for(element in best) {
        print("${getWeight(element)}, ")
      }
      println("]\n")

      if ( best.size < bestLengthBeforeReduction ) {
        println("[Length of the list before reduction: $bestLengthBeforeReduction]")
        println("[Length of the list after reduction: ${best.size}]")
        println("Sum of weight before reduction: $sumOfWeightBeforeReduction")
        println("Sum of weight after reduction: ${computeSum(best)}")
        println("Token reduced: ${sumOfWeightBeforeReduction - computeSum(best)}")
        println("Average weight before reduction: $averageWeightOfBest")
        println("Average weight after reduction: ${computeSum(best) / best.size.toDouble()}")
      }
      else {
        println("No node is deleted.")
      }


      println("Number of tests: $num_of_tests")
      println("Time: ${endTime - startTime} ms")
      println("***********************************************************************************************************************************************")
    }

  }

  open fun reducePartitions(partitionList: PartitionList<ElementWrapper<T>>): Boolean {
    log { "Reducing partitions: ${partitionList.partitions.size}" }
    for (partition in partitionList.partitions) {
      val elements = partition.elements
      lazyAssert { elements.isNotEmpty() }
      val config = ConfigurationBasedOnElementSystemIdentity(elements)
      if (cache.contains(config)) {
        continue
      }
      cache.add(config)
      ++num_of_tests
      val propertyTestResult = testProperty(
        Configuration(currentBest = best, candidate_ = partition.elements, deleted_ = null),
      )

      // TODO: this needs test.
      if (propertyTestResult !is PropertyTestResultWithPayload<PropertyPayload>) {
        continue
      }
      if (propertyTestResult.result.isInteresting) {
        if(print_detail) {
          println("\n**Reduce to partitions success! **\n")
        }
        cache.deleteStaleConfigs(elements.size)
        updateBest(partition.elements, propertyTestResult.payload)
        return true
      }
    }
    return false
  }

//  open fun reduceComplements(originalPartitionList: PartitionList<ElementWrapper<T>>): Int {
//    log { "Reducing complements: ${originalPartitionList.partitions.size}" }
//    var currentPartitionList = originalPartitionList
//    var count = 0
//    complementLoop@ for (partition in currentPartitionList.partitions) {
//      val complement = currentPartitionList.computeComplementFor(partition)
//      val config = ConfigurationBasedOnElementSystemIdentity(complement.input)
//      if (cache.contains(config)) {
//        continue
//      }
//      cache.add(config)
//      ++num_of_tests
//      val propertyTestResult = testProperty(
//        Configuration(currentBest = best, candidate_ = complement.input, deleted_ = null),
//      )
//
//      if (propertyTestResult !is PropertyTestResultWithPayload<PropertyPayload>) {
//        continue
//      }
//      if (propertyTestResult.result.isInteresting) {
//        if(print_detail) {
//          println("\n**Reduce to complement success! **\n")
//        }
//        count++
//        cache.deleteStaleConfigs(complement.input.size)
//        updateBest(complement.input, propertyTestResult.payload)
//        currentPartitionList = complement
//        continue@complementLoop
//      }
//    }
//    return count
//  }

  open fun reduceComplements(originalPartitionList: PartitionList<ElementWrapper<T>>): Int {
    log { "Reducing complements: ${originalPartitionList.partitions.size}" }
    var currentPartitionList = originalPartitionList
    var count = 0

    var restart = true
    while(restart) {
      if(print_detail) {
        println("restart reduceComplements, size of partitionList: ${currentPartitionList.partitions.size} \n")
      }
      restart = false
      complementLoop@ for (partition in currentPartitionList.partitions) {
        val complement = currentPartitionList.computeComplementFor(partition)
        val config = ConfigurationBasedOnElementSystemIdentity(complement.input)
        if (cache.contains(config)) {
          continue
        }
        cache.add(config)
        ++num_of_tests
        val propertyTestResult = testProperty(
          Configuration(currentBest = best, candidate_ = complement.input, deleted_ = null),
        )

        if (propertyTestResult !is PropertyTestResultWithPayload<PropertyPayload>) {
          continue
        }
        if (propertyTestResult.result.isInteresting) {
          if(print_detail) {
            println("\n**Reduce to complement success! **\n")
          }
          count++
          cache.deleteStaleConfigs(complement.input.size)
          updateBest(complement.input, propertyTestResult.payload)
          currentPartitionList = complement
//          continue@complementLoop
          restart = true
          break
        }
      }
    }
    return count
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


  abstract fun partition(
    list: ImmutableList<ElementWrapper<T>>,
    numberOfPartitions: Int,
  ): PartitionList<ElementWrapper<T>>
}
