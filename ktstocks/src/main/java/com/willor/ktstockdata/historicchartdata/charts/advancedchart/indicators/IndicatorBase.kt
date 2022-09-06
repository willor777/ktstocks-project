package com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators

import com.willor.ktstockdata.common.TaLibCore


/**
 * All sublist methods return startIndex up to and including endIndex
 */
abstract class IndicatorBase {

    internal val talib = TaLibCore.getTaLibCore()

    protected fun findTrueIndex(i: Int, lastIndexOfValues: Int): Int{
        return if (i < 0) {
            lastIndexOfValues + (i + 1)
        } else {
            i
        }
    }

    protected fun fillMissingValues(outputData: DoubleArray, inputData: Collection<Double>
    ): List<Double>{

        val missingValues = inputData.size - outputData.size

        val finalData = mutableListOf<Double>()
        for (n in 1..missingValues){
            finalData.add(0.0)
        }

        finalData.addAll(outputData.toList())
        return finalData
    }


    protected fun calculateOutputArraySize(inputDataSize: Int, window: Int): Int{
        return inputDataSize - window + 1
    }




}