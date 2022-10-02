package com.willor.ktstockdata.historicchartdata

import com.google.gson.Gson
import com.willor.ktstockdata.common.Log
import com.willor.ktstockdata.common.NetworkClient
import com.willor.ktstockdata.common.addParamsToUrl
import com.willor.ktstockdata.common.d
import com.willor.ktstockdata.historicchartdata.charts.StockChartBase
import com.willor.ktstockdata.historicchartdata.charts.advancedchart.AdvancedStockChart
import com.willor.ktstockdata.historicchartdata.charts.simplechart.SimpleStockChart
import com.willor.ktstockdata.historicchartdata.dataobjects.yfhistoryresp.HistoryResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Request
import okhttp3.Response
import java.util.*
import kotlin.coroutines.coroutineContext


/**
 * Retrieves Historical Stock Data from Yahoo Finance in the form of...
 *
 * SimpleStockChart - Contains data and useful getters
 *
 * AdvancedStockChart - Contains data and useful getters + methods for technical analysis
 */
class HistoricChartData : IHistoricChartData {

    private val baseUrl = "https://query2.finance.yahoo.com"

    override suspend fun getHistoryAsSimpleStockChartAsync(
        ticker: String,
        interval: String,
        periodRange: String,
        prepost: Boolean
    ): SimpleStockChart? {
        val tick = ticker.uppercase()
        val history = getHistoryAsync(
            tick,
            interval,
            periodRange,
            prepost
        ) ?: return null

        if (interval == "1m") {
            return formatOneMin(history, prepost, SimpleStockChart::class.java) as SimpleStockChart
        }

        return SimpleStockChart(
            ticker = tick,
            interval = history.chart.result[0].meta.dataGranularity,
            periodRange = history.chart.result[0].meta.range,
            prepost = prepost,
            datetime = convertTimestampsToDatetime(history.chart.result[0].timestamp),
            timestamp = history.chart.result[0].timestamp,
            open = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].open
            ),
            high = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].high
            ),
            low = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].low
            ),
            close = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].close
            ),
            volume = removeNullValuesFromIntList(
                history.chart.result[0].indicators.quote[0].volume
            )
        )
    }

    override suspend fun getHistoryAsAdvancedStockChartAsync(
        ticker: String,
        interval: String,
        periodRange: String,
        prepost: Boolean
    ): AdvancedStockChart? {
        val tick = ticker.uppercase()
        val history = getHistoryAsync(
            tick,
            interval,
            periodRange,
            prepost
        ) ?: return null

        if (interval == "1m") {
            return formatOneMin(
                history,
                prepost,
                AdvancedStockChart::class.java
            ) as AdvancedStockChart
        }

        return AdvancedStockChart(
            ticker = tick,
            interval = history.chart.result[0].meta.dataGranularity,
            periodRange = history.chart.result[0].meta.range,
            prepost = prepost,
            datetime = convertTimestampsToDatetime(history.chart.result[0].timestamp),
            timestamp = history.chart.result[0].timestamp,
            open = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].open
            ),
            high = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].high
            ),
            low = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].low
            ),
            close = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].close
            ),
            volume = removeNullValuesFromIntList(
                history.chart.result[0].indicators.quote[0].volume
            )
        )
    }

    /**
     * Retrieves the Historical Chart Data for the specified Ticker. Start time depends on period,
     * End time is always current
     *
     * ..................................................................................
     *
     * - Chart Behavior...
     *
     * - lastIndex : Candle that is currently being formed. (No Volume Information)
     *
     * - lastIndex - 1 : Candle which is the Last fully formed. (Complete Information)
     *
     * ..................................................................................
     *
     * :param: ticker - Ticker you want data for
     *
     * :param:
     * interval - Valid intervals: 1m,2m,5m,15m,60m,90m,1h,1d,5d,1wk,1mo,3mo.
     *
     * :param:
     * period - Valid period depends on interval 1d,5d,1mo,3mo,6mo,1y,2y,5y,10y,ytd,max.
     *
     * :param:
     * prepost - Include premarket data
     *
     * :return:
     * SimpleStockChart?
     */
    override fun getHistoryAsSimpleStockChart(
        ticker: String,
        interval: String,
        periodRange: String,
        prepost: Boolean
    ): SimpleStockChart? {
        val tick = ticker.uppercase()
        val history = getHistory(tick, interval, periodRange, prepost) ?: return null

        // Bug with 1m chart's where the second to last candle has null values
        if (interval == "1m") {
            return formatOneMin(history, prepost, SimpleStockChart::class.java) as SimpleStockChart
        }

        return SimpleStockChart(
            ticker = tick,
            interval = history.chart.result[0].meta.dataGranularity,
            periodRange = history.chart.result[0].meta.range,
            prepost = prepost,
            datetime = convertTimestampsToDatetime(history.chart.result[0].timestamp),
            timestamp = history.chart.result[0].timestamp,
            open = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].open
            ),
            high = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].high
            ),
            low = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].low
            ),
            close = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].close
            ),
            volume = removeNullValuesFromIntList(
                history.chart.result[0].indicators.quote[0].volume
            )
        )
    }


    /**
     * Retrieves the Historical Chart Data for the specified Ticker. Start time depends on period,
     * End time is always current.
     *
     * ..................................................................................
     *
     * - Chart Behavior...
     *
     * - lastIndex : Candle that is currently being formed. (No Volume Information)
     *
     * - lastIndex - 1 : Candle which is the Last fully formed. (Complete Information)
     *
     * ..................................................................................
     *
     * :param: ticker - Ticker you want data for
     *
     * :param:
     * interval - Valid intervals: 1m,2m,5m,15m,60m,90m,1h,1d,5d,1wk,1mo,3mo.
     *
     * :param:
     * period - Valid period depends on interval 1d,5d,1mo,3mo,6mo,1y,2y,5y,10y,ytd,max.
     *
     * :param:
     * prepost - Include premarket data
     *
     * :return:
     * AdvancedStockChart?
     */
    override fun getHistoryAsAdvancedStockChart(
        ticker: String,
        interval: String,
        periodRange: String,
        prepost: Boolean
    ): AdvancedStockChart? {
        val tick = ticker.uppercase()
        val history = getHistory(tick, interval, periodRange, prepost) ?: return null
        if (interval == "1m") {
            return formatOneMin(
                history,
                prepost,
                AdvancedStockChart::class.java
            ) as AdvancedStockChart
        }
        return AdvancedStockChart(
            ticker = tick,
            interval = history.chart.result[0].meta.dataGranularity,
            periodRange = history.chart.result[0].meta.range,
            prepost = prepost,
            datetime = convertTimestampsToDatetime(history.chart.result[0].timestamp),
            timestamp = history.chart.result[0].timestamp,
            open = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].open
            ),
            high = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].high
            ),
            low = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].low
            ),
            close = removeNullValuesFromDoublesList(
                history.chart.result[0].indicators.quote[0].close
            ),
            volume = removeNullValuesFromIntList(
                history.chart.result[0].indicators.quote[0].volume
            )
        )
    }


    /**
     * Retrieves the Historical Chart Data for the specified Ticker. Start time depends on period,
     * End time is always current

     * :param: ticker - Ticker you want data for
     *
     * :param:
     * interval - Valid intervals: 1m,2m,5m,15m,60m,90m,1h,1d,5d,1wk,1mo,3mo.
     *
     * :param:
     * period - Valid period depends on interval 1d,5d,1mo,3mo,6mo,1y,2y,5y,10y,ytd,max.
     *
     * :param:
     * prepost - Include premarket data
     *
     * :return:
     * HistoryResponse?
     */
    private fun getHistory(
        ticker: String,
        interval: String = "5m",
        periodRange: String = "5d",
        prepost: Boolean = true
    ): HistoryResponse? {
        try {
            // Period
            val params = HashMap<String, String>()
            params["range"] = periodRange

            // Interval
            params["interval"] = interval

            // prepost
            params["includePrePost"] = prepost.toString()

            // Events
            params["events"] = "div,splits"

            // Build url
            val urlString = "$baseUrl/v8/finance/chart/${ticker.uppercase()}"

            // Build Call / Execute / Check Success
            val call = NetworkClient.getClient().newCall(
                Request.Builder()
                    .url(addParamsToUrl(urlString, params))
                    .get()
                    .build()
            )

            val resp = call.execute()

            if (!resp.isSuccessful) {
                displayNonSuccessInfo(resp)
                return null
            }

            // Convert to HistoryResponse and return
            val rawJsonResponse = resp.body!!.string()
            val history = Gson().fromJson(rawJsonResponse, HistoryResponse::class.java)

            val verifyIntegrity = {
                val data = history.chart.result[0].indicators.quote[0]
                val nCandles = history.chart.result[0].timestamp.size

                // Verify that all data lists have equal size
                data.open.size == nCandles &&
                        data.high.size == nCandles &&
                        data.low.size == nCandles &&
                        data.close.size == nCandles &&
                        data.volume.size == nCandles
            }

            // Verify that all data OHLCV & Ts lists are equal size
            if (verifyIntegrity()) {
                return history
            }

            return null
        } catch (e: Exception) {
            println(e.stackTraceToString())
            Log.d("EXCEPTION", e.stackTraceToString())

            return null
        }
    }


    private suspend fun getHistoryAsync(
        ticker: String,
        interval: String = "5m",
        periodRange: String = "5d",
        prepost: Boolean = true
    ): HistoryResponse? {
        try {
            // Period
            val params = HashMap<String, String>()
            params["range"] = periodRange

            // Interval
            params["interval"] = interval

            // prepost
            params["includePrePost"] = prepost.toString()

            // Events
            params["events"] = "div,splits"

            // Build url
            val urlString = "$baseUrl/v8/finance/chart/${ticker.uppercase()}"

            // Build Call / Execute / Check Success
            val call = NetworkClient.getClient().newCall(
                Request.Builder()
                    .url(addParamsToUrl(urlString, params))
                    .get()
                    .build()
            )

            // Launch the Async Request
            val deferredReq: Deferred<Response> =
                CoroutineScope(coroutineContext).async(Dispatchers.Unconfined) {
                    call.execute()
                }

            // Await it
            val resp = deferredReq.await()

            if (!resp.isSuccessful) {
                displayNonSuccessInfo(resp)
                return null
            }

            // Convert to HistoryResponse and return
            val rawJsonResponse = resp.body!!.string()
            val history = Gson().fromJson(rawJsonResponse, HistoryResponse::class.java)

            val verifyIntegrity = {
                val data = history.chart.result[0].indicators.quote[0]
                val nCandles = history.chart.result[0].timestamp.size

                // Verify that all data lists have equal size
                data.open.size == nCandles &&
                        data.high.size == nCandles &&
                        data.low.size == nCandles &&
                        data.close.size == nCandles &&
                        data.volume.size == nCandles
            }

            // Verify that all data OHLCV & Ts lists are equal size
            if (verifyIntegrity()) {
                return history
            }

            return null
        } catch (e: Exception) {
            println(e.stackTraceToString())
            Log.d("EXCEPTION", e.stackTraceToString())

            return null
        }
    }


    /**
     * Used to fix a bug with 1m interval chart where the candle at (lastIndex - 1) was basically
     * empty. So now the 1m chart behaves like the others...
     *
     * lastIndex:     The current partially formed candle (Accurate Close, Zero Volume).
     *
     * lastIndex - 1: The last fully formed candle.
     */
    private fun <T : StockChartBase> formatOneMin(
        chart: HistoryResponse,
        prepost: Boolean,
        t: Class<T>
    ): StockChartBase? {

        val outerData = chart.chart.result[0]
        val priceData = outerData.indicators.quote[0]

        val ticker = outerData.meta.symbol
        val interval = outerData.meta.dataGranularity
        val periodRange = outerData.meta.range

        val dateTimes = mutableListOf<Date>()
        outerData.timestamp.map {
            dateTimes.add(Date(it.toLong() * 1000))
        }

        val ts = outerData.timestamp.toMutableList()
        ts.removeAt(ts.lastIndex - 1)

        val open = priceData.open.toMutableList()
        open.removeAt(open.lastIndex - 1)

        val high = priceData.high.toMutableList()
        high.removeAt(high.lastIndex - 1)

        val low = priceData.low.toMutableList()
        low.removeAt(low.lastIndex - 1)

        val close = priceData.close.toMutableList()
        close.removeAt(close.lastIndex - 1)

        val volume = priceData.volume.toMutableList()
        volume.removeAt(volume.lastIndex - 1)

        if (t == AdvancedStockChart::class.java) {
            return AdvancedStockChart(
                ticker, interval, periodRange, prepost, dateTimes, ts,
                removeNullValuesFromDoublesList(open),
                removeNullValuesFromDoublesList(high),
                removeNullValuesFromDoublesList(low),
                removeNullValuesFromDoublesList(close),
                removeNullValuesFromIntList(volume)
            )
        } else if (t == SimpleStockChart::class.java) {
            return SimpleStockChart(
                ticker, interval, periodRange, prepost, dateTimes, ts,
                removeNullValuesFromDoublesList(open),
                removeNullValuesFromDoublesList(high),
                removeNullValuesFromDoublesList(low),
                removeNullValuesFromDoublesList(close),
                removeNullValuesFromIntList(volume)
            )
        }
        return null
    }


    /**
     * Takes list of Epoch Seconds Timestamp <Int> and converts to Date objects
     */
    private fun convertTimestampsToDatetime(tsList: List<Int>): List<Date> {
        val dtList = mutableListOf<Date>()

        for (ts in tsList) {
            dtList.add(Date(ts.toLong() * 1000))
        }

        return dtList
    }


    /**
     * Displays the Error msg for a failed request.
     */
    private fun displayNonSuccessInfo(resp: Response) {
        val msg = """
            - NETWORK REQUEST FAILURE -
            *\-------------------------------------------------------------------------/*
            
            SUCCESS:     ${resp.isSuccessful}
            CODE:        ${resp.code}
            MESSAGE:     ${resp.message}
            BODY:        ${resp.body!!.string()}
            
            *\-------------------------------------------------------------------------/*
        """.trimIndent()

        Log.d("NETWORK", msg)
        println(msg)
    }


    /**
     * Replaces Null values with 0.0.
     */
    private fun removeNullValuesFromDoublesList(l: List<Double?>): List<Double> {

        val newList = mutableListOf<Double>()
        l.forEach {
            if (it == null) {
                newList.add(0.0)
            } else {
                newList.add(it)
            }
        }
        return newList
    }


    /**
     * Replaces Null values with 0.0
     */
    private fun removeNullValuesFromIntList(l: List<Int?>): List<Int> {
        val newList = mutableListOf<Int>()
        l.forEach {
            if (it == null) {
                newList.add(0)
            } else {
                newList.add(it)
            }
        }
        return newList
    }
}
