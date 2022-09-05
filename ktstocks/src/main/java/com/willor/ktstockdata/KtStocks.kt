package com.willor.ktstockdata

import com.willor.ktstockdata.marketdata.MarketData

class KtStocks {
}


fun main() {
    println(MarketData().getETFQuote("SPY"))
}