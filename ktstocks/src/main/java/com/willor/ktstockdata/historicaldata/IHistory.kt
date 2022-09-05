package com.willor.ktstockdata.historicaldata

import com.willor.ktstockdata.historicaldata.charts.advancedchart.AdvancedStockChart
import com.willor.ktstockdata.historicaldata.charts.simplechart.SimpleStockChart

interface IHistory {

    fun getHistoryAsSimpleStockChart(
        ticker: String,
        interval: String = "5m",
        periodRange: String = "5d",
        prepost: Boolean = true
    ): SimpleStockChart?


    fun getHistoryAsAdvancedStockChart(
        ticker: String,
        interval: String = "5m",
        periodRange: String = "5d",
        prepost: Boolean = true
    ): AdvancedStockChart?
}