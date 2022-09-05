package com.willor.ktyfinance.yfinance.data_objects.responses


import com.google.gson.annotations.SerializedName

internal data class Meta(
    @SerializedName("chartPreviousClose")
    val chartPreviousClose: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("currentTradingPeriod")
    val currentTradingPeriod: CurrentTradingPeriod,
    @SerializedName("dataGranularity")
    val dataGranularity: String,
    @SerializedName("exchangeName")
    val exchangeName: String,
    @SerializedName("exchangeTimezoneName")
    val exchangeTimezoneName: String,
    @SerializedName("firstTradeDate")
    val firstTradeDate: Int,
    @SerializedName("gmtoffset")
    val gmtoffset: Int,
    @SerializedName("instrumentType")
    val instrumentType: String,
    @SerializedName("previousClose")
    val previousClose: Double,
    @SerializedName("priceHint")
    val priceHint: Int,
    @SerializedName("range")
    val range: String,
    @SerializedName("regularMarketPrice")
    val regularMarketPrice: Double,
    @SerializedName("regularMarketTime")
    val regularMarketTime: Int,
    @SerializedName("scale")
    val scale: Int,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("validRanges")
    val validRanges: List<String>
)