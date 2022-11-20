package com.willor.ktstockdata.marketdata.dataobjects

import java.util.*

data class StockQuote(
    val ticker: String,
    val changeDollarRegMarket: Double,
    val changePctRegMarket: Double,
    val lastPriceRegMarket: Double,
    val prevClose: Double,
    val openPrice: Double,
    val bidPrice: Double,
    val bidSize: Int,
    val askPrice: Double,
    val askSize: Int,
    val daysRangeHigh: Double,
    val daysRangeLow: Double,
    val fiftyTwoWeekRangeHigh: Double,
    val fiftyTwoWeekRangeLow: Double,
    val volume: Int,
    val avgVolume: Int,
    val marketCap: Long,
    val betaFiveYearMonthly: Double,
    val peRatioTTM: Double,
    val epsTTM: Double,
    val nextEarningsDate: Date? = null,
    val forwardDivYieldValue: Double,
    val forwardDivYieldPercentage: Double,
    val exDividendDate: Date? = null,
    val oneYearTargetEstimate: Double,
    val quoteTimeStamp: Long = System.currentTimeMillis(),
    val prepostPrice: Double,
    val prepostChangeDollar: Double,
    val prepostChangePct: Double,
    val marketCapAbbreviatedString: String,
    val name: String
)
