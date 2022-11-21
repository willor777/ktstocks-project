package com.willor.ktstockdata.marketdata.dataobjects

import com.willor.ktstockdata.marketdata.dataobjects.responses.RawUnusualOption

data class UnusualOption(
    val ask: Double,
    val bid: Double,
    val contractExpiry: String,
    val impVol: Double,
    val openInt: Int,
    val lastPrice: Double,
    val otmPercentage: Double,
    val strikePrice: Double,
    val contractType: String,
    val ticker: String,
    val volume: Int,
    val volOiRatio: Double
)

fun RawUnusualOption.toUnusualOption(): UnusualOption {
    return UnusualOption(
        ask, bid, contractExpiry, impVol,
        openInt, lastPrice, otmPercentage,
        strikePrice, contractType, ticker,
        volume, volOiRatio
    )
}


