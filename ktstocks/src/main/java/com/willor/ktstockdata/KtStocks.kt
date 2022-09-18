package com.willor.ktstockdata

import com.willor.ktstockdata.historicchartdata.HistoricChartData
import com.willor.ktstockdata.marketdata.MarketData
import com.willor.ktstockdata.watchlistsdata.WatchlistData

class KtStocks {
    val historicChartData = HistoricChartData()
    val marketData = MarketData()
    val watchlistData = WatchlistData()
}
