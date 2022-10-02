package com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators

import com.tictactec.ta.lib.MInteger

class MACD(
    inputData: List<Double>,
    fastWindow: Int = 12,
    slowWindow: Int = 26,
    signalWindow: Int = 9,
) : IndicatorBase() {

    val lastIndex = inputData.lastIndex
    val size = inputData.size

    val macdValues: List<Double>          // Fast
    val macdSignalValues: List<Double>        // Slow
    val macdHistValues: List<Double>          // Reversals


    init {

        // Macd is weird where first it will slice off the biggest of (fast, slow) + more for the
        // signal window
        val macdOutSize = calculateOutputArraySize(
            inputData.size,
            maxOf(fastWindow, slowWindow)
        ) - signalWindow + 1

        val macdArr = DoubleArray(macdOutSize)
        val macdSignalArr = DoubleArray(macdOutSize)
        val macdHistArr = DoubleArray(macdOutSize)


        // startI, endI, input, fast, slow, signal, _, _, macd, macdSignal, hist
        talib.macd(
            0,
            inputData.lastIndex,
            inputData.toDoubleArray(),
            fastWindow,
            slowWindow,
            signalWindow,
            MInteger(), MInteger(),
            macdArr,
            macdSignalArr,
            macdHistArr
        )

        macdValues = fillMissingValues(macdArr, inputData)
        macdSignalValues = fillMissingValues(macdSignalArr, inputData)
        macdHistValues = fillMissingValues(macdHistArr, inputData)
    }

    fun getMacdValueAtIndex(i: Int): Double {
        return macdValues[findTrueIndex(i, macdValues.lastIndex)]
    }

    fun getSublistOfMacdValues(startIndex: Int, endIndex: Int): List<Double> {

        return macdValues.subList(
            findTrueIndex(startIndex, macdValues.lastIndex),
            findTrueIndex(endIndex, macdValues.lastIndex) + 1
        )
    }

    fun getMacdSignalValueAtIndex(i: Int): Double {
        return macdSignalValues[findTrueIndex(i, macdSignalValues.lastIndex)]
    }

    fun getSublistOfMacdSignalValues(startIndex: Int, endIndex: Int): List<Double> {

        return macdSignalValues.subList(
            findTrueIndex(startIndex, macdSignalValues.lastIndex),
            findTrueIndex(endIndex, macdSignalValues.lastIndex) + 1
        )
    }

    fun getMacdHistValueAtIndex(i: Int): Double {
        return macdHistValues[findTrueIndex(i, macdHistValues.lastIndex)]
    }

    fun getSublistOfMacdHistValues(startIndex: Int, endIndex: Int): List<Double> {

        return macdHistValues.subList(
            findTrueIndex(startIndex, macdHistValues.lastIndex),
            findTrueIndex(endIndex, macdHistValues.lastIndex) + 1
        )
    }
}

