package org.perses.delta.xfs

import org.perses.delta.Configuration
import org.perses.delta.PropertyTestResultWithPayload
import org.perses.util.Util
import org.perses.util.toImmutableList
import java.util.ArrayDeque
import com.google.common.collect.ImmutableList
import org.perses.delta.AbstractDeltaDebugger
import org.perses.delta.WeightedDeltaDebugger.IWeightProvider
import org.perses.delta.WeightedDeltaDebugger.Companion.getWeight
import org.perses.delta.WeightedDeltaDebugger.Companion.computeSum

class WeightedDfsDeltaDebugger<T, PropertyPayload> (
  arguments: Arguments<T, PropertyPayload>,
  private val weightProvider: IWeightProvider<T>,
) : DfsDeltaDebugger<T, PropertyPayload>(arguments) {

  override fun createElementWrapperFor(index: Int, element: T): ElementWrapper<T> {
    val weight = weightProvider.weight(element)
    return ElementWrapper(index, element, weight)
  }

  private fun getWeightList(list: ImmutableList<ElementWrapper<T>>): List<Int> {
    val weightList = mutableListOf<Int>()
    for (i in 0 until list.size) {
      weightList.add(getWeight(list, i))
    }
    return weightList
  }

  override fun reduceNonEmptyInput() {
    val initialPartition = Partition(best)
    val startPartitions = initialPartition.weightSplit(getWeightList(best))
    val workList = ArrayDeque<Partition<ElementWrapper<T>>>()
    addToWorklist(workList, startPartitions)
    while (workList.isNotEmpty()) {
      val partition = pollFromWorklist(workList)
      val deletedInThisIteration = partition.asSequence().toImmutableList()
      val testResult = testProperty(
        Configuration(
          currentBest = null,
          candidate_ = null,
          deleted_ = deletedInThisIteration,
        ),
      )
      if (testResult !is PropertyTestResultWithPayload || testResult.result.isNotInteresting) {
        addToWorklist(workList, partition.weightSplit(getWeightList(deletedInThisIteration)))
        continue
      } else {
        updateBest(Util.computeDifference(best, deletedInThisIteration), testResult.payload)
      }
    }
  }

}
