package com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp


import com.google.gson.annotations.SerializedName

internal data class TradingPeriods(
    @SerializedName("post")
    val post: List<List<Post>>,
)