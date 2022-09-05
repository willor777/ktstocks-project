package com.willor.ktstockdata.marketdata.dataobjects

data class SnRLevels(
    val ticker: String,
    val r3: Double,
    val r2: Double,
    val r1: Double,
    val approxPrice: Double,
    val s1: Double,
    val s2: Double,
    val s3: Double,
    val fiftyTwoWeekHigh: Double,
    val fibonacci62Pct: Double,
    val fibonacci50Pct: Double,
    val fibonacci38Pct: Double,
    val fiftyTwoWeekLow: Double
)
