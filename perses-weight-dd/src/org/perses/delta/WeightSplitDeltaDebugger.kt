package org.perses.delta

import com.google.common.collect.ImmutableList
import org.perses.util.Util.lazyAssert
import org.perses.util.toImmutableList

class WeightSplitDeltaDebugger<T, PropertyPayload>(
  args: Arguments<T, PropertyPayload>,
  private val weightProvider: IWeightProvider<T>,
) :  PristineDeltaDebugger<T, PropertyPayload>(args, false, false, weightProvider) {

  private val cache : AbstractConfigCache<T> = ConfigCache(false)

  override val print_detail = false
  override val print_stat = false

  override var num_of_tests = 0
  private var num_of_tests_for_one_minimal = 0

  // this is for weightDD to assure 1-minimal, save all single-element partitions for one-pass check after reduction
  private val singleElementPartitionList = mutableListOf<PartitionList.Partition<ElementWrapper<T>>>()

  override fun createElementWrapperFor(index: Int, element: T): ElementWrapper<T> {
    val weight = weightProvider.weight(element)
    return ElementWrapper(index, element, weight)
  }

  private fun splitEachPartition(list: PartitionList<ElementWrapper<T>>) : PartitionList<ElementWrapper<T>> {
    if(print_detail) println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
    val partitions = mutableListOf<PartitionList.Partition<ElementWrapper<T>>>()
    for (partition in list.partitions) {
      val size = partition.elements.size
      if(size <= 1) {
        singleElementPartitionList.add(partition) // add to singleElementPartitionList for 1-minimal check
        continue
      }
      if(size == 2) {
        val p1 = PartitionList.Partition(partition.elements.subList(0, 1))
        val p2 = PartitionList.Partition(partition.elements.subList(1, 2))
        partitions.add(p1)
        partitions.add(p2)
        if(print_detail) {
            println("Partition [${partition.elements.elementAt(0).index}, ], weight = ${getWeight(partition.elements.elementAt(0))}")
            println("Partition [${partition.elements.elementAt(1).index}, ], weight = ${getWeight(partition.elements.elementAt(1))}")
        }
        continue
      }
      // split the partition into two partitions by weight
      val weightOfPartition : Double = computeSum(partition.elements) / 2.0
      var i = 0
      var currentSum = getWeight(partition.elements, i)
      i++
      while(i < size) {
        val currentWeight = getWeight(partition.elements, i)
        if(currentSum + currentWeight > weightOfPartition) {
          if(weightOfPartition - currentSum >= currentSum + currentWeight - weightOfPartition) {
            currentSum += currentWeight
            i++
          }
          break
        }
        currentSum += currentWeight
        i++
      }
      val p1 = PartitionList.Partition(partition.elements.subList(0, i))
      val p2 = PartitionList.Partition(partition.elements.subList(i, size))
      if(print_detail) {
        println("Partition [${partition.elements.elementAt(0).index}, ${partition.elements.elementAt(i-1).index}], weight = $currentSum")
        println("Partition [${partition.elements.elementAt(i).index}, ${partition.elements.elementAt(size-1).index}], weight = ${computeSum(partition.elements) - currentSum}")
      }
      partitions.add(p1)
      partitions.add(p2)
    }
    return PartitionList(partitions.toImmutableList())
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
    val averageWeightOfBest = computeSum(best) / best.size.toDouble()


    val startTime = System.currentTimeMillis()
//    var prevPartitionList : PartitionList<ElementWrapper<T>>? = null
    var partitionList = PartitionList(ImmutableList.of(PartitionList.Partition(best)))

    while(best.size > 1 && best.size > partitionList.partitions.size) {

      partitionList = splitEachPartition(partitionList)

      if(print_detail) println("Size of partitionList: ${partitionList.partitions.size}")

      if(partitionList.partitions.size < 2) {
        lazyAssert { partitionList.partitions.size == 0}
        break
      }
      lazyAssert {
        partitionList.partitions.size != 1 || partitionList.partitions.single().elements == best
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
        if (print_detail) {
          println("********************Reducing to partitions*********************\n")
        }
        if (reducePartitions(partitionList)) {
          partitionList = PartitionList(ImmutableList.of(PartitionList.Partition(best)))
          continue
        }
      }

      if (arguments.partitionComplementControl.enableReducingComplements) {
        if (print_detail) {
          println("********************Reducing to complements*********************\n")
        }
        val partitionsLeft = reduceComplements0(partitionList)
        if(partitionsLeft.partitions.size < partitionList.partitions.size) {
          partitionList = partitionsLeft
          continue
        }
      }
      if(best.size <= partitionList.partitions.size) {
        break
      }
    }
    checkOneMinimal()
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
      println("Number of tests for one-minimal: $num_of_tests_for_one_minimal")
      println("Time: ${endTime - startTime} ms")
      println("***********************************************************************************************************************************************")
    }
  }

  private fun checkOneMinimal() {
    if(print_detail) {
      println("********************Checking one-minimal*********************\n")
      println("size of singleElementPartitionList: ${singleElementPartitionList.size} \n")
      println("size of best: ${best.size} \n")
    }
//    for(partition in singleElementPartitionList) {
//      lazyAssert { cache.contains(ConfigurationBasedOnElementSystemIdentity(partition.elements)) }
//    }
    var restart = true
    while (restart) {
      restart = false
      for (partition in singleElementPartitionList) {
        val complementElements = computeComplement(partition)
        val config = ConfigurationBasedOnElementSystemIdentity(complementElements)
        if(cache.contains(config)) {
          if(print_detail) {
            print("[Cache!] Complement contained [")
            for(element in complementElements) {
              print("${getWeight(element)}, ")
            }
            println("]\n")
          }
          continue
        }
        cache.add(config)
        ++num_of_tests
        ++num_of_tests_for_one_minimal
        val propertyTestResult = testProperty(
          Configuration(currentBest = best, candidate_ = complementElements, deleted_ = null),
        )
        if (propertyTestResult !is PropertyTestResultWithPayload<PropertyPayload>) {
          continue
        }
        if (propertyTestResult.result.isInteresting) {
          if(print_detail) {
            println("\n** Reduce to partition success! **")
            println("Partition left (new best): [${partition.elements.elementAt(0).index}, ${partition.elements.elementAt(partition.elements.size-1).index}], weight = ${computeSum(partition.elements)}\n")
          }
//          cache.deleteStaleConfigs(complementElements.size)
          updateBest(complementElements, propertyTestResult.payload)
          singleElementPartitionList.remove(partition)
          restart = true
          break
        }
      }
    }
  }


  override fun reducePartitions(partitionList: PartitionList<ElementWrapper<T>>): Boolean {
    log { "Reducing partitions: ${partitionList.partitions.size}" }
    for (partition in partitionList.partitions) {
      val elements = partition.elements
      lazyAssert { elements.isNotEmpty() }

      val config = ConfigurationBasedOnElementSystemIdentity(elements)
      if (cache.contains(config)) {
        if(print_detail) {
          print("[Cache!] Partition contained [")
          for(element in elements) {
            print("${getWeight(element)}, ")
          }
          println("]\n")
        }
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
          println("\n**Reduce to complement success! **")
          println("Partition deleted: [${partition.elements.elementAt(0).index}, ${partition.elements.elementAt(partition.elements.size-1).index}], weight = ${computeSum(partition.elements)}\n")
        }
        cache.deleteStaleConfigs(elements.size)
        updateBest(partition.elements, propertyTestResult.payload)
        return true
      }
    }
    return false
  }

  private fun computeComplement(partitionToExclude: PartitionList.Partition<ElementWrapper<T>>): ImmutableList<ElementWrapper<T>> {
    val result = ImmutableList.builder<ElementWrapper<T>>()
    for(element in best) {
      if(element in partitionToExclude.elements) {
        continue
      }
      result.add(element)
    }
    return result.build()
  }

  private fun reduceComplements0(originalPartitionList: PartitionList<ElementWrapper<T>>): PartitionList<ElementWrapper<T>> {
    log { "Reducing complements: ${originalPartitionList.partitions.size}" }
    var currentPartitionList = originalPartitionList
    var count = 0
    var restart = true
    while(restart) {
      if(print_detail && count > 0) {
        println("restart reduceComplements, size of partitionList: ${currentPartitionList.partitions.size} \n")
      }
      restart = false
      complementLoop@ for (partition in currentPartitionList.partitions) {
        val complementElements = computeComplement(partition)
//        val complement = currentPartitionList.computeComplementFor(partition)
        val config = ConfigurationBasedOnElementSystemIdentity(complementElements)
        if (cache.contains(config)) {
          if(print_detail) {
            print("[Cache!] Complement contained [")
            for(element in complementElements) {
              print("${getWeight(element)}, ")
            }
            println("]\n")
          }
          continue
        }
        cache.add(config)
        ++num_of_tests
        val propertyTestResult = testProperty(
          Configuration(currentBest = best, candidate_ = complementElements, deleted_ = null),
        )
        if (propertyTestResult !is PropertyTestResultWithPayload<PropertyPayload>) {
          continue
        }
        if (propertyTestResult.result.isInteresting) {
          if(print_detail) {
            println("\n** Reduce to complement success! **")
            println("Partition deleted: [${partition.elements.elementAt(0).index}, ${partition.elements.elementAt(partition.elements.size-1).index}], weight = ${computeSum(partition.elements)}\n")
          }
          count++
          cache.deleteStaleConfigs(complementElements.size)
          updateBest(complementElements, propertyTestResult.payload)
          // update current partition list
          val partitions = currentPartitionList.partitions.toMutableList()
          partitions.remove(partition)
          currentPartitionList = PartitionList(partitions.toImmutableList())
//          currentPartitionList = complement
          restart = true
          break
        }
      }
    }
    return currentPartitionList
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

}
