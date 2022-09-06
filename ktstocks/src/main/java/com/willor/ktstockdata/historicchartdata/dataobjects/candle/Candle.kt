package com.willor.ktstockdata.historicchartdata.dataobjects.candle

import java.util.*

data class Candle(
    val datetime: Date,
    val timestamp: Int,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Int
)