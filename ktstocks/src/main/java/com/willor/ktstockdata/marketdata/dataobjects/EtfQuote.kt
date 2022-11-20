package com.willor.ktstockdata.marketdata.dataobjects

import java.util.*

data class EtfQuote(
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
    val netAssets: Long,
    val nav: Double,
    val peRatioTTM: Double,
    val yieldPercentage: Double,
    val yearToDateTotalReturn: Double,
    val betaFiveYearMonthly: Double,
    val expenseRatioNetPercentage: Double,
    val inceptionDate: Date? = null,
    val quoteTimeStamp: Long = System.currentTimeMillis(),
    val prepostPrice: Double,
    val prepostChangeDollar: Double,
    val prepostChangePct: Double,
    val netAssetsAbbreviatedString: String,
    val name: String
)

