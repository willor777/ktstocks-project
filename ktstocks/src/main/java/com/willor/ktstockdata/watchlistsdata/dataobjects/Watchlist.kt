package com.willor.ktstockdata.watchlistsdata.dataobjects

data class Watchlist(
    val name: String,
    val tickers: List<Ticker>
)
