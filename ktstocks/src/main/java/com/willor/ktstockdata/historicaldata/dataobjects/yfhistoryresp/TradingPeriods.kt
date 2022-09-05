package com.willor.ktyfinance.yfinance.data_objects.responses


import com.google.gson.annotations.SerializedName

internal data class TradingPeriods(
    @SerializedName("post")
    val post: List<List<Post>>,
)