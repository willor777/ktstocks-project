package com.willor.ktyfinance.yfinance.data_objects.responses


import com.google.gson.annotations.SerializedName

internal data class HistoryResponse(
    @SerializedName("chart")
    val chart: Chart
)