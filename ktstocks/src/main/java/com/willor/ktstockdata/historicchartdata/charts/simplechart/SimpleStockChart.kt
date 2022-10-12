package com.willor.ktstockdata.historicchartdata.charts.simplechart

import com.willor.ktstockdata.common.Log
import com.willor.ktstockdata.common.w
import com.willor.ktstockdata.historicchartdata.charts.StockChartBase
import com.willor.ktstockdata.historicchartdata.charts.advancedchart.AdvancedStockChart
import java.util.*


/**
 * Simple stock chart giving access to data as lists as well as basic meta data
 */
class SimpleStockChart(
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
    volume: List<Int>
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
    volume = volume
) {
    companion object {

        fun fromAdvancedStockChart(c: AdvancedStockChart): SimpleStockChart? {
            return try {
                SimpleStockChart(
                    ticker = c.ticker,
                    interval = c.interval,
                    periodRange = c.periodRange,
                    prepost = c.prepost,
                    datetime = c.datetime,
                    timestamp = c.timestamp,
                    open = c.open,
                    high = c.high,
                    low = c.low,
                    close = c.close,
                    volume = c.volume
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("DEBUG", e.stackTraceToString())
                null
            }
        }
    }
}
