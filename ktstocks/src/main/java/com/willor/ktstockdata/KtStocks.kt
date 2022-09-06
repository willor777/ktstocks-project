package com.willor.ktstockdata

import com.willor.ktstockdata.historicchartdata.HistoricChartData
import com.willor.ktstockdata.historicchartdata.charts.advancedchart.AdvancedStockChart
import com.willor.ktstockdata.historicchartdata.charts.advancedchart.indicators.EMA
import com.willor.ktstockdata.historicchartdata.charts.simplechart.SimpleStockChart
import com.willor.ktstockdata.historicchartdata.dataobjects.candle.Candle
import com.willor.ktstockdata.marketdata.MarketData
import com.willor.ktstockdata.marketdata.dataobjects.EtfQuote
import com.willor.ktstockdata.marketdata.dataobjects.MajorFuturesData
import com.willor.ktstockdata.marketdata.dataobjects.StockQuote
import com.willor.ktstockdata.watchlistsdata.WatchlistData
import com.willor.ktstockdata.watchlistsdata.dataobjects.Watchlist

class KtStocks {
    val historicChartData = HistoricChartData()
    val marketData = MarketData()
    val watchlistData = WatchlistData()
}















