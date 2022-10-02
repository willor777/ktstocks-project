package com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp


import com.google.gson.annotations.SerializedName

internal data class HistoryResponse(
    @SerializedName("chart")
    val chart: Chart
)