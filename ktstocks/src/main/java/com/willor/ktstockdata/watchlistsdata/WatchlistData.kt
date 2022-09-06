package com.willor.ktstockdata.watchlistsdata

import com.willor.ktstockdata.common.NetworkClient
import com.willor.ktstockdata.common.parseDouble
import com.willor.ktstockdata.common.parseLongFromBigAbbreviatedNumbers
import com.willor.ktstockdata.watchlistsdata.dataobjects.Ticker
import com.willor.ktstockdata.watchlistsdata.dataobjects.Watchlist
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class WatchlistData {

    // TODO Add a search for keyword function

    private fun scrapeYfWatchList(url: String): List<List<String>>? {
        try {
            val page = NetworkClient.getWebpage(url)
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
            return null
        }
    }

    private fun processYfWatchlistScrape(url: String): Watchlist? {
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
                        if (!rowData[0].contains("-")){
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
                }
                returnValue
            } catch (e: Exception) {
                println(e.printStackTrace())
                null
            }
        }

        val getWatchlistName = { s: String ->

            if (s.contains("/watchlists/")) {
                s.substringAfterLast("/watchlists/")
            } else {
                s.substringAfter(".com/")
            }
        }

        val l = scrapeYfWatchList(url) ?: return null

        val wlName = getWatchlistName(url)

        val tickers = mutableListOf<Ticker>()
        for (n in 0..l.lastIndex) {
            val listOfValues = l[n]

            if (listOfValues.size == 9 || listOfValues.size == 10) {
                val t = (createTicker(l[n]))
                if (t != null) {
                    tickers.add(t)
                }
            }

        }

        return Watchlist(wlName, tickers)
    }

    fun searchForByKeywords(vararg keywords: String): List<Watchlist>?{
        val matches = mutableListOf<WatchlistOptions>()

        for(w in WatchlistOptions.values()){
            for (k in keywords){
                if (w.name.contains(k.uppercase())){
                    matches.add(w)
                }
            }
        }

        val watchlistMatches = mutableListOf<Watchlist>()
        matches.forEach{
            getWatchlistByWatchlistOption(it)?.let{ wl ->
                watchlistMatches.add(wl)
            }
        }
        return if(watchlistMatches.isNotEmpty()){
            watchlistMatches.toList()
        } else{
            null
        }
    }

    fun getWatchlistByWatchlistOption(w: WatchlistOptions): Watchlist?{
        when (w) {
            WatchlistOptions.GAINERS -> {
                return getGainers()
            }
            WatchlistOptions.LOSERS -> {
                return getLosers()
            }
            WatchlistOptions.TECH_STOCKS_THAT_MOVE_THE_MARKET -> {
                return getTechStocksThatMoveTheMarket()
            }
            WatchlistOptions.MOST_ACTIVE -> {
                return getMostActive()
            }
            WatchlistOptions.MOST_ACTIVE_PENNY_STOCKS -> {
                return getMostActivePennyStocks()
            }
            WatchlistOptions.MOST_ACTIVE_SMALL_CAP_STOCKS-> {
                return getMostActiveSmallCapStocks()
            }
            WatchlistOptions.MOST_BOUGHT_BY_ACTIVE_HEDGE_FUNDS -> {
                return getMostBoughtByActiveHedgeFunds()
            }
            WatchlistOptions.MOST_BOUGHT_BY_HEDGE_FUNDS -> {
                return getMostBoughtByHedgeFunds()
            }
            WatchlistOptions.MOST_SOLD_BY_ACTIVE_HEDGE_FUNDS -> {
                return getMostSoldByActiveHedgeFunds()
            }
            WatchlistOptions.MOST_SOLD_BY_HEDGE_FUNDS-> {
                return getMostSoldByHedgeFunds()
            }
            WatchlistOptions.MOST_NEWLY_ADDED_TO_WATCHLISTS-> {
                return getMostNewlyAddedToWatchlists()
            }
            WatchlistOptions.MOST_WATCHED_BY_RETAIL_TRADERS-> {
                return getMostWatchedByRetailTraders()
            }
            WatchlistOptions.LARGEST_FIFTY_TWO_WEEK_GAINS-> {
                return getLargestFiftyTwoWeekGains()
            }
            WatchlistOptions.LARGEST_FIFTY_TWO_WEEK_LOSSES-> {
                return getLargestFiftyTwoWeekLosses()
            }
            WatchlistOptions.RECENT_FIFTY_TWO_WEEK_HIGHS-> {
                return getRecentFiftyTwoWeekHighs()
            }
            WatchlistOptions.RECENT_FIFTY_TWO_WEEK_LOWS-> {
                return getRecentFiftyTwoWeekLows()
            }
            WatchlistOptions.BIG_EARNINGS_MISSES-> {
                return getBigEarningsMisses()
            }
            WatchlistOptions.BIG_EARNINGS_BEATS-> {
                return getBigEarningsBeats()
            }
            WatchlistOptions.CHINA_TECH_AND_INTERNET_STOCKS-> {
                return getChinaTechAndInternet()
            }
            WatchlistOptions.E_COMMERCE_STOCKS-> {
                return getECommerceStocks()
            }
            WatchlistOptions.CANNABIS_STOCKS-> {
                return getCannabisStocks()
            }
            WatchlistOptions.SELF_DRIVING_CAR_STOCKS-> {
                return getSelfDrivingCarStocks()
            }
            WatchlistOptions.VIDEO_GAME_DEVELOPER_STOCKS-> {
                return getVideoGameStocks()
            }
            WatchlistOptions.BANKS_AND_FINANCIAL_SERVICES_STOCKS-> {
                return getBankAndFinancialServicesStocks()
            }
            WatchlistOptions.MEDICAL_DEVICE_AND_RESEARCH_STOCKS-> {
                return getSelfDrivingCarStocks()
            }
            WatchlistOptions.SMART_MONEY_STOCKS-> {
                return getSmartMoneyStocks()
            }
            WatchlistOptions.DIVIDEND_GROWTH_MARKET_LEADERS-> {
                return getDividendGrowthMarketLeaders()
            }
            WatchlistOptions.BERKSHIRE_HATHAWAY_PORTFOLIO-> {
                return getBerkshireHathawayPortfolio()
            }
            WatchlistOptions.BIOTECH_AND_DRUG_STOCKS-> {
                return getBioTechAndDrugStocks()
            }
        }
    }

    fun getGainers(): Watchlist? {
        return processYfWatchlistScrape("https://finance.yahoo.com/gainers") ?: return null
    }

    fun getLosers(): Watchlist? {
        return processYfWatchlistScrape("https://finance.yahoo.com/losers")
    }

    fun getTechStocksThatMoveTheMarket(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/" +
                    "watchlists/tech-stocks-that-move-the-market"
        )

    }

    fun getMostActive(): Watchlist? {
        return processYfWatchlistScrape("https://finance.yahoo.com/most-active")
    }

    fun getMostActivePennyStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                    "ists/most-active-penny-stocks"
        )
    }

    fun getMostActiveSmallCapStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo" +
                    "-finance/watchlists/most-active-small-cap-stocks"
        )
    }

    fun getMostBoughtByActiveHedgeFunds(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance" +
                    "/watchlists/most-bought-by-activist-hedge-funds"
        )
    }

    fun getMostBoughtByHedgeFunds(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo" +
                    "-finance/watchlists/most-bought-by-hedge-funds"
        )
    }

    fun getMostSoldByActiveHedgeFunds(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watch" +
                    "lists/most-sold-by-activist-hedge-funds"
        )
    }

    fun getMostSoldByHedgeFunds(): Watchlist?{
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchlists/m" +
                    "ost-sold-by-hedge-funds"
        )
    }

    fun getMostNewlyAddedToWatchlists(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/w" +
                    "atchlists/most-added"
        )
    }

    fun getMostWatchedByRetailTraders(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yaho" +
                    "o-finance/watchlists/most-watched"
        )
    }

    fun getLargestFiftyTwoWeekGains(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-fi" +
                    "nance/watchlists/fiftytwo-wk-gain"
        )
    }

    fun getLargestFiftyTwoWeekLosses(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                    "ists/fiftytwo-wk-loss"
        )
    }

    fun getRecentFiftyTwoWeekHighs(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                    "ists/fiftytwo-wk-high"
        )
    }

    fun getRecentFiftyTwoWeekLows(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo" +
                    "-finance/watchlists/fiftytwo-wk-low"
        )
    }

    fun getBigEarningsMisses(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/w" +
                    "atchlists/earnings-miss"
        )

    }

    fun getBigEarningsBeats(): Watchlist?{
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/" +
                    "watchlists/earnings-beat"
        )

    }

    fun getChinaTechAndInternet(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchlist" +
                    "s/china-tech-and-internet-stocks"
        )
    }

    fun getECommerceStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/" +
                    "watchlists/e-commerce-stocks"
        )
    }

    fun getCannabisStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchlist" +
                    "s/420_stocks"
        )
    }

    fun getSelfDrivingCarStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance" +
                    "/watchlists/the-autonomous-car"
        )
    }

    fun getVideoGameStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-f" +
                    "inance/watchlists/video-game-stocks"
        )
    }

    fun getBankAndFinancialServicesStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-fi" +
                    "nance/watchlists/bank-and-financial-services-stocks"
        )
    }

    fun getMedicalDeviceAndResearchStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo." +
                    "com/u/yahoo-finance/watchlists/medical-device-and-research-stocks"
        )
    }

    fun getSmartMoneyStocks(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                    "ists/smart-money-stocks"
        )
    }

    fun getDividendGrowthMarketLeaders(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/motley-fool/watchlists/div" +
                    "idend-growth-market-leaders"
        )
    }

    fun getBerkshireHathawayPortfolio(): Watchlist? {
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                    "ists/the-berkshire-hathaway-portfolio"
        )
    }

    fun getBioTechAndDrugStocks(): Watchlist?{
        return processYfWatchlistScrape(
            "https://finance.yahoo.com/u/yahoo-finance/watchlists/biotech-and-drug-stocks"
        )
    }



}
