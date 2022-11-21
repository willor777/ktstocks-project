package com.willor.ktstockdata.marketdata.dataobjects

import com.willor.ktstockdata.marketdata.dataobjects.responses.RawUnusualOptionsActivityPage

data class UnusualOptionsActivityPage(
    val currentPage: Int,
    val data: List<UnusualOption>,
    val lastUpdatedEpochSeconds: Long,
    val pagesAvailable: Int
)


fun RawUnusualOptionsActivityPage.toUnusualOptionsActivityPage(): UnusualOptionsActivityPage {
    val newData = data.map {
        it.toUnusualOption()
    }

    return UnusualOptionsActivityPage(
        currentPage = this.currentPage,
        data = newData,
        lastUpdatedEpochSeconds = this.lastUpdatedEpochSeconds,
        pagesAvailable = this.pagesAvailable
    )
}