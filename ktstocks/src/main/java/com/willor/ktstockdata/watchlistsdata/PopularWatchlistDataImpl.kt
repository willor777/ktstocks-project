package com.willor.ktstockdata.watchlistsdata

import com.willor.ktstockdata.common.*
import com.willor.ktstockdata.watchlistsdata.dataobjects.Ticker
import com.willor.ktstockdata.watchlistsdata.dataobjects.Watchlist
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.coroutines.coroutineContext

class PopularWatchlistDataImpl : PopularWatchlistData {

    private val tag: String = PopularWatchlistDataImpl::class.java.simpleName


    private fun scrapeYfWatchList(page: String): List<List<String>>? {

        try {
            val doc: Document? = Jsoup.parse(page)

            val rows = doc?.select("tr") ?: return null

            val uglyDataCollection = mutableListOf<List<String>>()
            for (r in rows) {
                val dataPoints = r.select("td")
                if (!dataPoints.isNullOrEmpty()) {
                    val listOfDataPoints = mutableListOf<String>()
                    dataPoints.map {
                        listOfDataPoints.add(
                            it.text()
                        )
                    }
                    uglyDataCollection.add(listOfDataPoints)
                }
            }

            return uglyDataCollection
        } catch (e: Exception) {
            Log.d(
                tag, "scrapeYfWatchlist encountered an Exception: $e\n" +
                        e.stackTraceToString()
            )
            return null
        }
    }


    private fun buildWatchlist(page: String, wlName: String): Watchlist? {
        val createTicker = { rowData: List<String> ->
            try {

                var returnValue: Ticker? = null
                when (rowData.size) {
                    10 -> {
                        if (!rowData[0].contains("-")) {
                            returnValue = Ticker(
                                ticker = rowData[0].replace(" ", ""),
                                companyName = rowData[1],
                                lastPrice = parseDouble(rowData[2]),
                                changeDollar = parseDouble(rowData[3]),
                                changePercent = parseDouble(rowData[4].substringBefore("%")),
                                volume = parseLongFromBigAbbreviatedNumbers(rowData[5]).toInt(),
                                volumeThirtyDayAvg = parseLongFromBigAbbreviatedNumbers(rowData[6]).toInt(),
                                marketCap = parseLongFromBigAbbreviatedNumbers(rowData[7])
                            )
                        }
                    }

                    9 -> {
                        if (!rowData[0].contains("-")) {
                            returnValue = Ticker(
                                ticker = rowData[0].replace(" ", ""),
                                companyName = rowData[1],
                                lastPrice = parseDouble(rowData[2]),
                                changeDollar = parseDouble(rowData[3]),
                                changePercent = parseDouble(rowData[4].substringBefore("%")),
                                volume = parseLongFromBigAbbreviatedNumbers(rowData[6]).toInt(),
                                volumeThirtyDayAvg = parseLongFromBigAbbreviatedNumbers(rowData[7]).toInt(),
                                marketCap = parseLongFromBigAbbreviatedNumbers(rowData[8])
                            )
                        }
                    }
                    else -> {
                        returnValue = null
                    }
                }
                returnValue
            } catch (e: Exception) {
                println(e.printStackTrace())
                null
            }
        }

        // Scrape the page
        val rawScrapeData = scrapeYfWatchList(page) ?: return null

        // Build Ticker Objects
        val tickers = mutableListOf<Ticker>()
        for (n in 0..rawScrapeData.lastIndex) {
            val listOfValues = rawScrapeData[n]

            if (listOfValues.size == 9 || listOfValues.size == 10) {
                val t = (createTicker(rawScrapeData[n]))
                if (t != null) {
                    tickers.add(t)
                }
            }
        }

        return Watchlist(wlName, tickers)
    }


    override fun getPopularWatchlist(wl: PopularWatchlistOptions): Watchlist? {
        val page = NetworkClient.getWebpage(wl.url) ?: return null
        return buildWatchlist(page, wl.name)
    }


    override suspend fun getPopularWatchlistAsync(wl: PopularWatchlistOptions): Watchlist? {
        val page = NetworkClient.getWebpageAsync(wl.url) ?: return null
        return buildWatchlist(page, wl.name)
    }


    override fun getPopularWatchlistsByKeywords(vararg keywords: String): List<Watchlist>? {

        // Search PopularWatchlistOptions for matching Watchlists
        val matchingWlOptions = mutableListOf<PopularWatchlistOptions>()
        for (w in PopularWatchlistOptions.values()) {
            for (k in keywords) {
                if (w.name.contains(k.uppercase())) {
                    matchingWlOptions.add(w)
                    break
                }
            }
        }

        // Retrieve Watchlists
        val watchlists = mutableListOf<Watchlist>()
        for (wlOption in matchingWlOptions) {
            val wl = getPopularWatchlist(wlOption)
            if (wl != null) {
                watchlists.add(wl)
            }
        }

        return watchlists.ifEmpty { null }
    }


    override suspend fun getPopularWatchlistsByKeywordsAsync(vararg keywords: String): List<Watchlist>? {

        // Search PopularWatchlistOptions for matching Watchlists
        val matchingWlOptions = mutableListOf<PopularWatchlistOptions>()
        for (w in PopularWatchlistOptions.values()) {
            for (k in keywords) {
                if (w.name.contains(k.uppercase())) {
                    matchingWlOptions.add(w)
                    break
                }
            }
        }

        // Retrieve Watchlists asynchronously
        val asyncReqs = mutableListOf<Deferred<Watchlist?>>()
        for (wlOption in matchingWlOptions) {

            asyncReqs.add(
                CoroutineScope(coroutineContext).async(Dispatchers.Unconfined) {
                    getPopularWatchlistAsync(wlOption)
                }
            )
        }
        val nullableWatchlists = asyncReqs.awaitAll()

        // Filter nulls
        val validWatchlists = mutableListOf<Watchlist>()
        nullableWatchlists.forEach {
            if (it != null) {
                validWatchlists.add(it)
            }
        }

        // Check size and return
        return validWatchlists.ifEmpty {
            null
        }
    }


    override fun getAllPopularWatchlistOptions(): List<PopularWatchlistOptions> {
        return PopularWatchlistOptions.values().asList()
    }
}

