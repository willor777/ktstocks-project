package com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators

import com.tictactec.ta.lib.MInteger

class WMA(
    val inputData: List<Double>,
    val window: Int
) : IndicatorBase() {

    val lastIndex = inputData.lastIndex
    val size = inputData.size

    val values: List<Double>

    init {

        val outSize = calculateOutputArraySize(inputData.size, window)
        val outArr = DoubleArray(outSize)

        // start, end, input, window, _, _, output
        talib.wma(
            0,
            lastIndex,
            inputData.toDoubleArray(),
            window,
            MInteger(), MInteger(),
            outArr
        )

        this.values = fillMissingValues(outArr, inputData)

        println(values)


        println(values.size)
        println(inputData.size)

    }


    fun getValueAtIndex(i: Int): Double {
        return values[findTrueIndex(i, values.lastIndex)]
    }

    fun getSublistOfValues(startIndex: Int, endIndex: Int): List<Double> {

        return values.subList(
            findTrueIndex(startIndex, values.lastIndex),
            findTrueIndex(endIndex, values.lastIndex) + 1
        )
    }

}
