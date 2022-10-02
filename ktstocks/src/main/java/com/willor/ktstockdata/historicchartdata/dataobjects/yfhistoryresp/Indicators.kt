package com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp


import com.google.gson.annotations.SerializedName

internal data class Indicators(
    @SerializedName("quote")
    val quote: List<Quote>
)