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
import org.perses.util.Util
import org.perses.util.toImmutableList
import kotlin.comparisons.compareBy
import kotlin.random.Random

class PristineProbabilisticDeltaDebugger<T, PropertyPayload>(
  arguments: AbstractDeltaDebugger.Arguments<T, PropertyPayload>,
  private val random: Random? = Random,
  private val terminationThreshold: Double = 1.0,
  private val initialProbability: Double = 0.1,
  private val weightProvider: WeightSplitDeltaDebugger.IWeightProvider<T>,
) : AbstractDeltaDebugger<T, PropertyPayload> (
  arguments,
) {

  val print_stat = false
  var num_of_tests = 0

  fun shouldTerminate(): Boolean {
    // checks if all probability models reach the threshold
    return best.all { getProbability(it) >= terminationThreshold }
  }

  // change the elementPayload in every element in deleted
  fun updateProbability(toBeDeleted: Iterable<ElementWrapper<T>>) {
    var product = 1.0

    for (element in toBeDeleted) {
      product *= (1.0 - getProbability(element))
    }

    for (element in toBeDeleted) {
      val newProbability = getProbability(element) / (1 - product)
      element.elementPayload = Triple(newProbability, getWeight(element), getCount(element) + 1)
    }
  }

  // sort copyBest according to elementPayload
  fun sortProbability(copyBest: MutableList<ElementWrapper<T>>) {
    if (random != null) {
      copyBest.shuffle(random)
    }
    copyBest.sortWith(compareBy { getProbability(it) })
  }

  // uses the sorted copyBest and generates new deleteForNextTest
  fun findNextTest(
    copyBest: Iterable<ElementWrapper<T>>,
  ): MutableList<ElementWrapper<T>> {
    var lastGain: Double = 0.0
    val toBeDeleted = mutableListOf<ElementWrapper<T>>()
    var product: Double = 1.0
    for (element in copyBest) {
      toBeDeleted.add(element)
      product *= (1.0 - getProbability(element))
      val currentGain: Double = toBeDeleted.size * product

      if (lastGain > currentGain) {
        toBeDeleted.removeAt(toBeDeleted.size - 1)
        break
      }
      lastGain = currentGain
    }
    toBeDeleted.sort()
    return toBeDeleted
  }

  private fun createWeightedElementWrapperFor(index: Int, element: T): ElementWrapper<T> {
    val weight = weightProvider.weight(element)
    val count = 0
    val payload = Triple(initialProbability, weight, count)
    return ElementWrapper(index, element, payload)
  }

  override fun createElementWrapperFor(index: Int, element: T): ElementWrapper<T> {
    return createWeightedElementWrapperFor(index, element)
  }

  override fun reduceNonEmptyInput() {

    if(print_stat) {
      print("weight_list(before reduction): [")
      for(element in best) {
        print("${getWeight(element)}, ")
      }
      println("]")
    }

    val bestLengthBeforeReduction = best.size
    val sumOfWeightBeforeReduction = computeSum(best)
    val averageWeightBeforeReduction = best.map {getWeight(it)}.average()

    val startTime = System.currentTimeMillis()

    while (!shouldTerminate()) {
      val copyBest = best.toMutableList()
      sortProbability(copyBest)
      val toBeDeleted = findNextTest(copyBest)
      val config = Configuration<T>(
        currentBest = best,
        candidate_ = null,
        deleted_ = toBeDeleted.toImmutableList(),
      )
      num_of_tests++
      val propertyTestResult = testProperty(config)
      if (propertyTestResult is PropertyTestResultWithPayload<PropertyPayload> &&
        propertyTestResult.result.isInteresting
      ) {
        val newBest = Util.computeDifference(best, toBeDeleted)
        updateBest(newBest, propertyTestResult.payload)
      } else {
        updateProbability(toBeDeleted)
      }
    }
    val endTime = System.currentTimeMillis()
    if(print_stat) {
      print("weight_list(after reduction): [")
      for(element in best) {
        print("${getWeight(element)}, ")
      }
      println("]\n")

      if(best.size < bestLengthBeforeReduction) {
        println("[Length of the list before reduction: $bestLengthBeforeReduction]")
        println("[Length of the list after reduction: ${best.size}]")
        println("Sum of weight before reduction: $sumOfWeightBeforeReduction")
        println("Sum of weight after reduction: ${computeSum(best)}")
        println("Token reduced: ${sumOfWeightBeforeReduction - computeSum(best)}")
        println("Average weight before reduction: $averageWeightBeforeReduction")
        println("Average weight after reduction: ${best.map { getWeight(it) }.average()}")
      }
      else {
        println("No node is deleted.")
      }

      println("Number of tests: $num_of_tests")
      println("Time: ${endTime - startTime} ms")
      println("***********************************************************************************************************************************************")
    }
  }

  companion object {
    // get the probability from the elementPayload, which is a tuple, get the first element as double
    @JvmStatic
    fun <T> getProbability(element: ElementWrapper<T>) = (element.elementPayload as Triple<*, *, *>).first as Double

    fun <T> getWeight(element: ElementWrapper<T>) = (element.elementPayload as Triple<*, *, *>).second as Int

    fun <T> getCount(element: ElementWrapper<T>) = (element.elementPayload as Triple<*, *, *>).third as Int

    fun <T> computeSum(list: ImmutableList<ElementWrapper<T>>): Int {
      return list.sumOf { getWeight(it) }
    }
  }
}
