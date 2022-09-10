package com.willor.ktstockdata.marketdata.dataobjects


import com.google.gson.annotations.SerializedName

data class UnusualOptionsActivityPage(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("data")
    val data: List<UnusualOption>,
    @SerializedName("last_updated")
    private val lastUpdatedEpochSeconds: Long,
    @SerializedName("pages")
    val pagesAvailable: Int
)