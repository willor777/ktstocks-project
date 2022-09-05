package com.willor.ktstockdata.marketdata.dataobjects

data class StockCompetitor(
    val ticker: String,
    val companyName: String,
    val pctChange: Double,
    val marketCap: Long,
    val marketCapAbbreviatedString: String
)