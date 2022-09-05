package com.willor.ktstockdata.historicaldata.charts.advancedchart.indicators

import com.tictactec.ta.lib.MInteger

class RSI(
    val inputData: List<Double>,
    val window: Int = 14
) : IndicatorBase(){

    val size = inputData.size
    val lastIndex = inputData.lastIndex
    val values: List<Double>

    init {

        // RSI requires 1 more value than the window, so window + 1 in size calculation
        val size = calculateOutputArraySize(inputData.size, window + 1)
        val outArr = DoubleArray(size)

        // startI, endI, input, window, _, _, output
        talib.rsi(
            0,
            inputData.lastIndex,
            inputData.toDoubleArray(),
            window,
            MInteger(), MInteger(),
            outArr
        )

        values = fillMissingValues(outArr, inputData)

    }

    fun getValueAtIndex(i: Int): Double{
        return values[findTrueIndex(i, values.lastIndex)]
    }

    fun getSublistOfValues(startIndex: Int, endIndex: Int): List<Double>{

        return values.subList(findTrueIndex(startIndex, values.lastIndex),
            findTrueIndex(endIndex, values.lastIndex) + 1)
    }
}
