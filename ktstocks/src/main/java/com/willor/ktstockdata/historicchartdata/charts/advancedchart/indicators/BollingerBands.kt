package com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators

import com.tictactec.ta.lib.MAType
import com.tictactec.ta.lib.MInteger


/**
 * Calculates the Bollinger Bands Indicator for give `inputData`
 *
 * :param: inputData: List<Double> - Values to calculate Bollinger Bands For
 *
 * :param: window: Int - Size of window for the Middle Band
 *
 * :param: maType: String - Type of Moving Average to use for Middle band. Valid Options...
 * sma, ema, dema, tema, wma
 */
class BollingerBands(
    val inputData: List<Double>,
    val window: Int = 20,
    val maType: String = "sma",
    val upperStdMultiplier: Double = 2.0,
    val lowerStdMultiplier: Double = 2.0
): IndicatorBase(){

    val upperBandValues: List<Double>
    val middleBandValues: List<Double>
    val lowerBandValues: List<Double>

    val size = inputData.size
    val lastIndex = inputData.lastIndex

    init {

        val outputArrSize = inputData.size - window + 1
        val outUpperArr = DoubleArray(outputArrSize)
        val outMidArr = DoubleArray(outputArrSize)
        val outLowArr = DoubleArray(outputArrSize)

        // startI, endI, input, window, devUp, devDown, maType, _, _, up, mid, lower out
        talib.bbands(
            0,
            inputData.lastIndex,
            inputData.toDoubleArray(),
            window,
            upperStdMultiplier,
            lowerStdMultiplier,
            determineMAType(),
            MInteger(), MInteger(),
            outUpperArr,
            outMidArr,
            outLowArr
            )

        upperBandValues = fillMissingValues(outUpperArr, inputData)
        middleBandValues = fillMissingValues(outMidArr, inputData)
        lowerBandValues = fillMissingValues(outLowArr, inputData)
    }

    private fun determineMAType(): MAType{
        return when(maType.lowercase()){
            "ema" ->{
                MAType.Ema
            }

            "dema" ->{
                MAType.Dema
            }

            "tema" ->{
                MAType.Tema
            }

            "wma" ->{
                MAType.Wma
            }

            else ->{
                MAType.Sma
            }
        }
    }

    fun getUpperValueAtIndex(i: Int): Double{
        return upperBandValues[findTrueIndex(i, upperBandValues.lastIndex)]
    }

    fun getSublistOfUpperBandValues(startIndex: Int, endIndex: Int): List<Double>{

        return upperBandValues.subList(findTrueIndex(startIndex, upperBandValues.lastIndex),
            findTrueIndex(endIndex, upperBandValues.lastIndex) + 1)
    }

    fun getMiddleValueAtIndex(i: Int): Double{
        return middleBandValues[findTrueIndex(i, middleBandValues.lastIndex)]
    }

    fun getSublistOfMiddleBandValues(startIndex: Int, endIndex: Int): List<Double>{

        return middleBandValues.subList(findTrueIndex(startIndex, middleBandValues.lastIndex),
            findTrueIndex(endIndex, middleBandValues.lastIndex) + 1)
    }

    fun getLowerValueAtIndex(i: Int): Double{
        return lowerBandValues[findTrueIndex(i, lowerBandValues.lastIndex)]
    }

    fun getSublistOfLowerBandValues(startIndex: Int, endIndex: Int): List<Double>{

        return lowerBandValues.subList(findTrueIndex(startIndex, lowerBandValues.lastIndex),
            findTrueIndex(endIndex, lowerBandValues.lastIndex) + 1)
    }
}
