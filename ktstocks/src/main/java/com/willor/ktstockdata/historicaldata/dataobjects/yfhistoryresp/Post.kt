package com.willor.ktyfinance.yfinance.data_objects.responses


import com.google.gson.annotations.SerializedName

internal data class Post(
    @SerializedName("end")
    val end: Int,
    @SerializedName("gmtoffset")
    val gmtoffset: Int,
    @SerializedName("start")
    val start: Int,
    @SerializedName("timezone")
    val timezone: String
)