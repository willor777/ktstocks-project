package com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp


import com.google.gson.annotations.SerializedName

internal data class Pre(
    @SerializedName("end")
    val end: Int,
    @SerializedName("gmtoffset")
    val gmtoffset: Int,
    @SerializedName("start")
    val start: Int,
    @SerializedName("timezone")
    val timezone: String
)