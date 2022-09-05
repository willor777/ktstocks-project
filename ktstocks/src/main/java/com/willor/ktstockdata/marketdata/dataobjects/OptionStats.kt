package com.willor.ktstockdata.marketdata.dataobjects

import java.util.*

data class OptionStats(
    val ticker: String,
    val impVol: Double,
    val impVolChangeToday: Double,
    val historicalVolatilityPercentage: Double,
    val ivPercentile: Double,
    val ivRank: Double,
    val ivHighLastYear: Double,
    val ivHighDate: Date,
    val ivLowLastYear: Double,
    val ivLowDate: Date,
    val putCallVolumeRatio: Double,
    val optionVolumeToday: Int,
    val optionVolumeAvgThirtyDay: Int,
    val putCallOpenInterestRatio: Double,
    val openInterestToday: Int,
    val openInterestThirtyDay: Int,
)

