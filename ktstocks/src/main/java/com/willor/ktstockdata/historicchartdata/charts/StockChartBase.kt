package com.willor.ktstockdata.historicchartdata.charts

import com.willor.ktstockdata.historicchartdata.dataobjects.candle.Candle
import java.util.*

abstract class StockChartBase(
    val ticker: String,
    val interval: String,
    val periodRange: String,
    val prepost: Boolean,
    val datetime: List<Date>,
    val timestamp: List<Int>,
    val open: List<Double>,
    val high: List<Double>,
    val low: List<Double>,
    val close: List<Double>,
    val volume: List<Int>
) {

    val lastIndex = open.lastIndex

    val size = open.size

    fun getOpenAtIndex(i: Int): Double {
        return open[findTrueIndex(i)]
    }

    fun getSublistOfOpen(startIndex: Int, endIndex: Int): List<Double> {
        return open.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getHighAtIndex(i: Int): Double {
        return high[findTrueIndex(i)]
    }

    fun getSublistOfHigh(startIndex: Int, endIndex: Int): List<Double> {
        return high.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getLowAtIndex(i: Int): Double {
        return low[findTrueIndex(i)]
    }

    fun getSublistOfLow(startIndex: Int, endIndex: Int): List<Double> {
        return low.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getCloseAtIndex(i: Int): Double {
        return close[findTrueIndex(i)]
    }

    fun getSublistOfClose(startIndex: Int, endIndex: Int): List<Double> {
        return close.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getVolumeAtIndex(i: Int): Int {
        return volume[findTrueIndex(i)]
    }

    fun getSublistOfVolume(startIndex: Int, endIndex: Int): List<Int> {
        return volume.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getDatetimeAtIndex(i: Int): Date {
        return datetime[findTrueIndex(i)]
    }

    fun getSublistOfDatetime(startIndex: Int, endIndex: Int): List<Date> {
        return datetime.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getTimestampAtIndex(i: Int): Int {
        return timestamp[findTrueIndex(i)]
    }

    fun getSublistOfTimestamp(startIndex: Int, endIndex: Int): List<Int> {
        return timestamp.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    /**
     * Returns [Candle] object for given index which includes the values of...
     *
     * - datetime
     * - timestamp
     * - open
     * - high
     * - low
     * - close
     * - volume
     */
    fun getCandleAtIndex(i: Int): Candle {
        // Find index once to save cpu cycles
        val trueIndex = findTrueIndex(i)

        return Candle(
            datetime = datetime[trueIndex],
            timestamp = timestamp[trueIndex],
            open = open[trueIndex],
            high = high[trueIndex],
            low = low[trueIndex],
            close = close[trueIndex],
            volume = volume[trueIndex]
        )
    }

    fun getSublistOfCandles(startIndex: Int, endIndex: Int): List<Candle>{
        val candles = mutableListOf<Candle>()
        for (n in findTrueIndex(startIndex)..findTrueIndex(endIndex)){
            candles.add(getCandleAtIndex(n))
        }

        return candles
    }

    /**
     * Used to determine index even if a negative value is provided.
     */
    protected fun findTrueIndex(i: Int): Int {
        return if (i < 0) {
            val lastIndex = open.lastIndex

            lastIndex + (i + 1)
        } else {
            i
        }
    }

}

