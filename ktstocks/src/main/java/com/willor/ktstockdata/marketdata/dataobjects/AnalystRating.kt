package com.willor.ktstockdata.marketdata.dataobjects

data class AnalystRating(
    val ticker: String,
    val assetName: String,
    val action: String,
    val brokerageName: String,
    val analystName: String,
    val curPrice: Double,
    val pctChange: Double,
    val oldPriceTarget: Double,
    val newPriceTarget: Double,
    val sizeOfChange: Double,           // Distance from old target to new target
    // Come up with a system to quantify these ratings...
    // Outperform, Underperform, OverWeight, UnderWeight, Neutral, Equal Weight, Buy, Sell, Hold, Sector Perform, Market Perform
    val rating: String,
)
