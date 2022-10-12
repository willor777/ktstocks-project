package com.willor.ktstockdata.marketdata.dataobjects

import com.willor.ktstockdata.common.parseDouble
import com.willor.ktstockdata.common.parseLongFromBigAbbreviatedNumbers


data class Future(
    val ticker: String,
    val nameAndExpiration: String,
    val lastPrice: Double,
    val changeDollar: Double,
    val changePercent: Double,
    val volumeToday: Long,
    val volumeAvgThirtyDay: Long,
) {
    internal companion object {
        fun createFromList(l: List<String>): Future {

            return Future(
                l[0],
                l[1],
                parseDouble(l[2]),
                parseDouble(l[4]),
                parseDouble(l[5].substringBefore("%")),
                parseLongFromBigAbbreviatedNumbers(l[6]),
                parseLongFromBigAbbreviatedNumbers(l[7]),
            )
        }
    }
}
