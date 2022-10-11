package com.willor.ktstockdata.marketdata.dataobjects.responses


import com.google.gson.annotations.SerializedName

data class RawUnusualOptionsActivityPage(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("data")
    val data: List<RawUnusualOption>,
    @SerializedName("last_updated")
    val lastUpdatedEpochSeconds: Long,
    @SerializedName("pages")
    val pagesAvailable: Int
)