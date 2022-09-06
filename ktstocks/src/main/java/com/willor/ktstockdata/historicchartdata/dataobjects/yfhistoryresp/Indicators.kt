package com.willor.ktyfinance.yfinance.data_objects.responses


import com.google.gson.annotations.SerializedName

internal data class Indicators(
    @SerializedName("quote")
    val quote: List<Quote>
)