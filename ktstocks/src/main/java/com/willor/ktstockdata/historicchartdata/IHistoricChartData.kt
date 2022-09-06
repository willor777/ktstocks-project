package com.willor.ktstockdata.historicchartdata

import com.willor.ktstockdata.historicchartdata.charts.advancedchart.AdvancedStockChart
import com.willor.ktstockdata.historicchartdata.charts.simplechart.SimpleStockChart

interface IHistoricChartData {

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