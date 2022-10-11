package com.willor.ktstockdata.marketdata.dataobjects.responses


import com.google.gson.annotations.SerializedName

data class RawUnusualOption(
    @SerializedName("a")
    val ask: Double,
    @SerializedName("b")
    val bid: Double,
    @SerializedName("expiry")
    val contractExpiry: String,
    @SerializedName("iv")
    val impVol: Double,
    @SerializedName("oi")
    val openInt: Int,
    @SerializedName("op")
    val lastPrice: Double,
    @SerializedName("po")
    val otmPercentage: Double,
    @SerializedName("s")
    val strikePrice: Double,
    @SerializedName("t")
    val contractType: String,
    @SerializedName("tk")
    val ticker: String,
    @SerializedName("v")
    val volume: Int,
    @SerializedName("vol/oi")
    val volOiRatio: Double
)