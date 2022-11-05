package com.willor.ktstockdata.watchlistsdata.dataobjects

data class Ticker(
    val ticker: String,
    val companyName: String,
    val lastPrice: Double,
    val changeDollar: Double,
    val changePercent: Double,
    val volume: Int,
    val volumeThirtyDayAvg: Int,
    val marketCap: Long,
    val marketCapAbbreviatedString: String
)
