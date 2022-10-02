package com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators

import com.tictactec.ta.lib.MInteger

class EMA(
    val inputData: List<Double>,
    val window: Int
) : IndicatorBase() {

    val values: List<Double>
    val lastIndex = inputData.lastIndex
    val size = inputData.size

    init {
        val size = calculateOutputArraySize(inputData.size, window)

        val outArr = DoubleArray(size)

        talib.ema(
            0, inputData.lastIndex,
            inputData.toDoubleArray(),
            window,
            MInteger(),
            MInteger(),
            outArr
        )

        values = fillMissingValues(outArr, inputData)
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
