package com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators

import com.tictactec.ta.lib.MInteger
import com.willor.ktstockdata.historicchartdata.dataobjects.candle.Candle

class AverageTrueRange(
    inputData: List<Candle>,
    window: Int = 14
) : IndicatorBase() {

    val lastIndex = inputData.lastIndex
    val size = inputData.size

    val values: List<Double>

    init {

        val size = calculateOutputArraySize(
            inputData.size, window
        )

        // ATR uses previous values too, so the size is minus 1
        val outputArr = DoubleArray(size - 1)

        val highList = mutableListOf<Double>()
        val lowList = mutableListOf<Double>()
        val closeList = mutableListOf<Double>()

        inputData.map {
            highList.add(it.high)
            lowList.add(it.low)
            closeList.add(it.close)
        }

        // startI, endI, inHigh, inLow, inClose, window, _, _, out
        talib.atr(
            0,
            inputData.lastIndex,
            highList.toDoubleArray(),
            lowList.toDoubleArray(),
            closeList.toDoubleArray(),
            window,
            MInteger(), MInteger(),
            outputArr
        )

        values = fillMissingValues(outputArr, highList)
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
