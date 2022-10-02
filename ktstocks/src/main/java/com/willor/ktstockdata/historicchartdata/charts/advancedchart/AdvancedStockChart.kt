package com.willor.ktstockdata.historicchartdata.charts.advancedchart

import com.willor.ktstockdata.common.Log
import com.willor.ktstockdata.common.d
import com.willor.ktstockdata.historicchartdata.charts.StockChartBase
import com.willor.ktstockdata.historicchartdata.charts.simplechart.SimpleStockChart
import java.util.*
import kotlin.math.abs


/**
 * Chart which provides some basic technical analysis helper methods.
 *
 *
 * - All methods which take an argument of 'index: Int' will accept a negative index. Example below.
 *      - -1 == lastIndex (the very last value)
 *      - -2 == lastIndex - 1 (the second to last value)
 */
class AdvancedStockChart(
    ticker: String,
    interval: String,
    periodRange: String,
    prepost: Boolean,
    datetime: List<Date>,
    timestamp: List<Int>,
    open: List<Double>,
    high: List<Double>,
    low: List<Double>,
    close: List<Double>,
    volume: List<Int>,
) : StockChartBase(
    ticker = ticker,
    interval = interval,
    periodRange = periodRange,
    prepost = prepost,
    datetime = datetime,
    timestamp = timestamp,
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume,
) {

    companion object {
        fun createFromSimpleStockChart(s: SimpleStockChart): AdvancedStockChart? {

            return try {
                AdvancedStockChart(
                    s.ticker, s.interval, s.periodRange, s.prepost, s.datetime, s.timestamp,
                    s.open, s.high, s.low, s.close, s.volume
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("DEBUG", e.stackTraceToString())
                null
            }
        }
    }


    fun getSliceChartAsAdvancedStockChart(startIndex: Int, endIndex: Int): AdvancedStockChart {
        return AdvancedStockChart(
            ticker,
            interval,
            periodRange + "--minus ${endIndex - startIndex} candles",
            prepost,
            getSublistOfDatetime(startIndex, endIndex),
            getSublistOfTimestamp(startIndex, endIndex),
            getSublistOfOpen(startIndex, endIndex),
            getSublistOfHigh(startIndex, endIndex),
            getSublistOfLow(startIndex, endIndex),
            getSublistOfClose(startIndex, endIndex),
            getSublistOfVolume(startIndex, endIndex)
        )
    }


    /**
     * Calculates the size of candle from High to Low for given index.
     */
    fun getCandleHighToLowMeasurement(index: Int): Double {
        val c = getCandleAtIndex(index)
        return c.high - c.low
    }


    /**
     * Calculates the size of candle's body for given index using abs(candle.open - candle.close)
     */
    fun getCandleBodyMeasurement(index: Int): Double {
        val c = getCandleAtIndex(index)
        return abs(c.open - c.close)
    }


    /**
     * Determines the "Color" of candle.
     *
     * - Returns...
     *      - "G" : open > close
     *      - "R" : close > open
     *      - "B" : close == open
     *
     */
    fun getCandleColorAsGorRorB(index: Int): String {

        val c = getCandleAtIndex(index)

        return if (c.close > c.open) {
            "G"
        } else if (c.close < c.open) {
            "R"
        } else {
            "B"
        }
    }


    /**
     * Calculates the Average Candle (High - Low) for the specified window ending
     * at the given index.
     */
    fun getAvgCandleHighToLow(index: Int, window: Int): Double {

        // Determine actual END index (even a negative value)
        val trueEndIndex = findTrueIndex(index)

        // Determine start index
        var startIndex = trueEndIndex - window
        var trueWindow = window
        if (startIndex < 0) {
            startIndex = 0
            trueWindow = trueEndIndex + 1
        }

        // Calculate avg for window
        var total = 0.0
        for (n in startIndex..trueEndIndex) {
            total += getCandleHighToLowMeasurement(n)
        }

        return total / trueWindow
    }


    /**
     * Calculates the Average Candle (abs(open - close)) for the specified window ending
     * at the given index
     */
    fun getAvgCandleBody(index: Int, window: Int): Double {
        // Find true end Index even with negative index value
        val trueEndIndex = findTrueIndex(index)

        // Determine start index / true window...aka check for < 0 start index
        var startIndex = trueEndIndex - window
        var trueWindow = window
        if (startIndex < 0) {
            startIndex = 0
            trueWindow = trueEndIndex + 1
        }

        // Calculate avg for window
        var total = 0.0
        for (n in startIndex..trueEndIndex) {
            total += getCandleBodyMeasurement(n)
        }
        return total / trueWindow
    }


    /**
     * Returns True if green candle closed within 10% of it's high
     */
    fun closedAtHigh(index: Int): Boolean {

        val c = getCandleAtIndex(index)

        // Make sure it's a green candle
        if (c.close < c.open) {
            return false
        }

        return getHeadSizeAsPercentageOfTotalSize(index) < .05
    }


    /**
     * Returns True if red candle closed within 10% of it's low
     */
    fun closedAtLow(index: Int): Boolean {

        val c = getCandleAtIndex(index)

        // Make sure candle is red
        if (c.close > c.open) {
            return false
        }

        return getTailSizeAsPercentageOfTotalSize(index) < .05
    }


    /**
     * Returns the Head size as a percentage of total size
     */
    fun getHeadSizeAsPercentageOfTotalSize(index: Int): Double {
        val c = getCandleAtIndex(index)

        return if (c.close > c.open) {

            (c.high - c.close) / (c.high - c.low)
        } else {
            (c.high - c.open) / (c.high - c.low)
        }

    }


    /**
     * Returns the Tail size as percentage of total size
     */
    fun getTailSizeAsPercentageOfTotalSize(index: Int): Double {
        val c = getCandleAtIndex(index)

        return if (c.close < c.open) {
            (c.close - c.low) / (c.high - c.low)
        } else {
            (c.open - c.low) / (c.high - c.low)
        }
    }


    /**
     * Returns the Body size as percentage of total size
     */
    fun getBodySizeAsPercentageOfTotalSize(index: Int): Double {
        val c = getCandleAtIndex(index)

        return abs(c.close - c.open) / (c.high - c.low)
    }


    /**
     * Returns the Percentage Difference of current body size vrs avg body size
     */
    fun getBodySizeVrsAvgBodyAsPercentage(index: Int, window: Int): Double {
        val targetCandle = getCandleBodyMeasurement(index)
        val avgCandle = getAvgCandleBody(index, window)

        return targetCandle / avgCandle
    }


    /**
     * Returns the Percentage Difference of current High to Low size vrs avg High to Low
     */
    fun getHighToLowSizeVrsAvgHighToLowAsPercentage(index: Int, window: Int): Double {
        val targetCandle = getCandleHighToLowMeasurement(index)
        val avgHighToLow = getAvgCandleHighToLow(index, window)

        return targetCandle / avgHighToLow
    }


    /**
     * Returns true if candle is Green
     */
    fun isGreenCandle(index: Int): Boolean {

        return when (getCandleColorAsGorRorB(index)) {
            "G" -> {
                true
            }
            else -> {
                false
            }
        }
    }


    /**
     * Returns true if candle is Red
     */
    fun isRedCandle(index: Int): Boolean {
        return when (getCandleColorAsGorRorB(index)) {
            "R" -> {
                true
            }
            else -> {
                false
            }
        }
    }


    /**
     * Checks if candle is above average body size
     */
    fun isAboveAvgBodySize(index: Int, windowForAvg: Int = 5): Boolean {
        val candle = getCandleAtIndex(index)

        return getCandleBodyMeasurement(index) > getAvgCandleBody(index, windowForAvg)
    }


    /**
     * Check if candle is above average high to low size
     */
    fun isAboveAvgHighToLowSize(index: Int, windowForAvg: Int = 5): Boolean {
        return getCandleHighToLowMeasurement(index) > getAvgCandleHighToLow(index, windowForAvg)
    }


    /**
     * Returns true if candle has is Green + no Tail + no Head + is Above Avg Body Size for span
     */
    fun isFullBody(index: Int): Boolean {
        return getHeadSizeAsPercentageOfTotalSize(index) < .05 &&
                getTailSizeAsPercentageOfTotalSize(index) < .05
    }


    /**
     * Returns true if candle is < 50% size of avg body, with both Head & Tail > 50% avg body
     */
    fun isPinBar(index: Int, windowForAvg: Int = 5): Boolean {
        val halfAvgBody = getAvgCandleBody(index, windowForAvg) * .5

        val head = getHeadSizeAsPercentageOfTotalSize(index)
        if (head < halfAvgBody) {
            return false
        }

        val tail = getTailSizeAsPercentageOfTotalSize(index)
        if (tail < halfAvgBody) {
            return false
        }

        if (getCandleBodyMeasurement(index) > halfAvgBody) {
            return false
        }

        return true
    }


    /**
     * Returns true if candle is Top Body Hammer. (Small Body + No Head + Big Tail)
     */
    fun isTopBodyHammer(index: Int): Boolean {

        // Closed at/near high (.05%)
        if (!closedAtHigh(index)) {
            return false
        }

        // If big tail, return true
        if (getTailSizeAsPercentageOfTotalSize(index) > .5) {
            return true
        }

        return false
    }


    /**
     * Returns true if candle is Bottom Body Hammer. (Small Body + No Tail + Big Head)
     */
    fun isBottomBodyHammer(index: Int): Boolean {

        // Closed at/near low (.05%)
        if (!closedAtLow(index)) {
            return false
        }

        // If big head, return true
        if (getHeadSizeAsPercentageOfTotalSize(index) > .5) {
            return true
        }

        return false
    }
}
