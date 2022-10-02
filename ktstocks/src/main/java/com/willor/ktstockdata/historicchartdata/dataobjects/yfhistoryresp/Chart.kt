package com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp


import com.google.gson.annotations.SerializedName

internal data class Chart(
    @SerializedName("error")
    val error: Any,
    @SerializedName("result")
    var result: List<Result>
)