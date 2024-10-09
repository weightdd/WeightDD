package org.perses.reduction.partition

import com.google.common.collect.ImmutableList
import org.perses.delta.xfs.Partition
import org.perses.reduction.ReductionLevel
import org.perses.spartree.AbstractSparTreeNode

/**
 *
 */
class WeightedLevelPartitionPolicy : AbstractLevelPartitionPolicy() {

  override fun partition(
    region: ReductionLevel,
    maxSizeOfPartition: Int,
    numOfPartitions: Int,
  ): ImmutableList<Partition<AbstractSparTreeNode>> {
    check(maxSizeOfPartition > 0) {
      "max number of nodes in per partition should be positive:$maxSizeOfPartition"
    }
    val nodeCount = region.nodeCount
    if (nodeCount == 0) {
      return ImmutableList.of()
    }

    val weightList = getWeights(region)
    val average : Int = weightList.sum() / numOfPartitions
    val builder = ImmutableList.builder<Partition<AbstractSparTreeNode>>()
    var partition = Partition.Builder<AbstractSparTreeNode>(nodeCount)

    if(numOfPartitions >= nodeCount) {
      for (i in 0 until nodeCount) {
        partition = Partition.Builder(1)
        partition.addNode(region.getNode(i))
        builder.add(partition.build())
      }
      return builder.build()
    }

//    println("Size of region: $nodeCount")
//    println("Average weight: $average")
//    println("Number of partitions: $numOfPartitions")
    var startIndex = 0
    var numPartitions = 0
    while(startIndex < nodeCount) {
      var currentSum = weightList[startIndex]
      partition.addNode(region.getNode(startIndex))
      var currentIndex = startIndex + 1
      while(currentIndex < nodeCount) {
        val currentWeight = weightList[currentIndex]
        if(currentSum + currentWeight > average) {
          break
        }
        currentSum += currentWeight
        partition.addNode(region.getNode(currentIndex))
        currentIndex++
      }
      builder.add(partition.build())
//      println("partition weight: $currentSum")
      numPartitions++
      partition = Partition.Builder(nodeCount)
      startIndex = currentIndex
    }
//    println("Real number of partitions: $numPartitions")
    return builder.build()
  }

  fun getWeight(node: AbstractSparTreeNode): Int {
//    println("[Node weight: ${node.leafTokenCount}]")
    return node.leafTokenCount
  }

  fun getWeights(region: ReductionLevel): List<Int> {
    val weights = mutableListOf<Int>()
    for (i in 0 until region.nodeCount) {
      weights.add(getWeight(region.getNode(i)))
    }
    return weights
  }

}
