package com.willor.ktstockdata.marketdata.dataobjects

data class AnalystGrade(
    val date: Long,
    val ticker: String,
    val company: String,
    val ratingChange: Int,
    val analystFirm: String,
)