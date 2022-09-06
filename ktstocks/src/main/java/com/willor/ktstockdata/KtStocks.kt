package com.willor.ktstockdata

import com.willor.ktstockdata.historicaldata.History
import com.willor.ktstockdata.marketdata.MarketData
import com.willor.ktstockdata.watchlistsdata.Watchlists

class KtStocks {
    val history = History()
    val marketData = MarketData()
    val watchlists = Watchlists()
}


