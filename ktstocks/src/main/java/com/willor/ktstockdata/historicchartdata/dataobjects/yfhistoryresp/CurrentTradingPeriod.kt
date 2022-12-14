package com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp


import com.google.gson.annotations.SerializedName

internal data class CurrentTradingPeriod(
    @SerializedName("post")
    val post: Post,
    @SerializedName("pre")
    val pre: Pre,
    @SerializedName("regular")
    val regular: Regular
)