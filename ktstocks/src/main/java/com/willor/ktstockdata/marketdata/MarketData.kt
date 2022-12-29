package com.willor.ktstockdata.marketdata

import com.willor.ktstockdata.common.*
import com.willor.ktstockdata.marketdata.dataobjects.*
import com.willor.ktstockdata.marketdata.dataobjects.responses.RawUnusualOptionsActivityPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import kotlin.coroutines.coroutineContext


class MarketData : IMarketData {


    private val tag: String = MarketData::class.java.simpleName


    // Urls --------------------------------------------------------------------------------------


    private val getSnrLevelsUrl = { tick: String ->
        "https://www.barchart.com/stocks/quotes/${tick.uppercase()}/overview"
    }
    private val getYfQuoteUrl = { tick: String ->
        val ticker = tick.uppercase()
        "https://finance.yahoo.com/quote/$ticker?p=$ticker&.tsrc=fin-srch"
    }
    private val getOptionStatsUrl = { ticker: String ->
        "https://www.barchart.com/stocks/quotes/${ticker.uppercase()}/overview"
    }
    private val getStocksCompetitorsUrl = { ticker: String ->
        "https://www.marketwatch.com/investing/stock/$ticker?mod=search_symbol"
    }
    private val getUnusualOptionsActivityUrl = { pageNum: Int ->
        "https://app.fdscanner.com/api2/unusualvolume?p=0&page_size=50&page=$pageNum"
    }
    private val futuresDataUrl = "https://finance.yahoo.com/commodities"
    private val majorIndicesDataUrl = "https://finance.yahoo.com/world-indices"
    private val analystsPriceTargetChangesUrl = "https://www.marketbeat.com/ratings/"


    // Helper Methods -----------------------------------------------------------------------------


    /**
     * Scrapes the Overall Option Stats from Barchart.com... Includes Quote data as well.
     */
    private fun scrapeBarchartQuoteTable(page: String): List<List<String>>? {

        try {
            // NOTE -- Even though this url is for a Stock, it seems to work for ETFs as well
            // further testing is required
            val doc: Document = Jsoup.parse(page)

            val tableOfOptionStats = doc.select(
                "div.block-content"
            )

            val dataRows = tableOfOptionStats.select("li")

            val uglyData = mutableListOf<List<String>>()
            for (row in dataRows) {
                val labelAndValues = row.select("span")

                val dataChunk = mutableListOf<String>()
                if (labelAndValues.size != 13 && labelAndValues.size != 0) {
                    labelAndValues.map {
                        if (it.text().isNotEmpty()) {
                            dataChunk.add(it.text())
                        }
                    }
                    uglyData.add(dataChunk.toList())
                }
            }
            return uglyData
        } catch (e: Exception) {
            Log.w(
                tag, "scrapeBarchartQuoteTable() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Scrapes the finance.yahoo.com Quote Page. Can be used for either Stock or Etf quotes.
     */
    private fun scrapeYfQuote(page: String): Map<String, Map<String, String>?>? {
        try {
            // Responsible for scraping the top row data. Returns Map<String, String>
            val scrapeTopRow = { doc: Document ->
                val results = mutableMapOf<String, String>()

                val topRowElements = doc.select("div#quote-header-info")

                // Set up place holders + pull easy data
                val compName = topRowElements.select("h1").text()
                var ppLastPrice = "0.0"
                var ppChangeDol = "0.0"
                var ppChangePct = "(0.0%)"

                // Pull the data
                val rawDataElements = topRowElements.select("fin-streamer")

                // Build data
                val data = mutableListOf<String>()
                for (d in rawDataElements) {
                    data.add(d.text())
                }

                // Regular market price info is always there
                val regLastPrice = data[0]
                val regChangeDol = data[1]
                val regChangePct = data[2]

                // After market is only there after / before market open/close
                if (data.size >= 8) {
                    ppLastPrice = data[6]
                    ppChangeDol = data[7]
                    ppChangePct = data[8]
                }

                // Build result dict
                results["compName"] = compName; results["regLastPrice"] = regLastPrice
                results["regChangeDol"] = regChangeDol; results["regChangePct"] = regChangePct
                results["ppLastPrice"] = ppLastPrice; results["ppChangeDol"] = ppChangeDol
                results["ppChangePct"] = ppChangePct

                results.toMap()
            }

            // Responsible for scraping the body data. Returns Map<String, String>
            val scrapeBody = { doc: Document ->
                val results = mutableMapOf<String, String>()

                val table = doc.select("div#quote-summary")
                val rawDataRows = table.select("tr")

                // Loop through the table rows and build dict
                for (r in rawDataRows) {
                    val data = r.select("td")
                    if (data.size == 2) {
                        results[data[0].text()] = data[1].text()
                    }
                }

                results
            }

            val doc = Jsoup.parse(page)

            // Base map
            val payload = mutableMapOf<String, Map<String, String>?>()
            payload["topRow"] = scrapeTopRow(doc)
            payload["body"] = scrapeBody(doc)

            return payload.toMap()

        } catch (e: Exception) {
            Log.w(
                tag, "scrapeYfQuoteTable() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Parses the barchart.com quote page for Support and Resistance data.
     */
    private fun parseSupportAndResistanceBarchartPage(page: String, ticker: String): SnRLevels? {
        try {
            // Get Document
            val tick = ticker.uppercase()
            val doc = Jsoup.parse(page)

            // Pull data rows
            val table = doc.select("div.bc-side-widget")
            val rows = table.select("td")

            // Build list of all data
            val allData = mutableListOf<String>()
            for (r in rows) {
                allData.add(r.text())
            }

            // Snr data starts here -- Note there is other valuable data on this page for future ref...
            val indexStart = allData.indexOf("3rd Resistance Point")

            val cleanValues = mutableListOf<String>()
            for (n in indexStart..allData.lastIndex) {
                cleanValues.add(allData[n].replace(" ", ""))
            }

            return SnRLevels(
                ticker = tick,
                r3 = parseDouble(cleanValues[1]),
                r2 = parseDouble(cleanValues[3]),
                r1 = parseDouble(cleanValues[5]),
                approxPrice = parseDouble(cleanValues[7]),
                s1 = parseDouble(cleanValues[9]),
                s2 = parseDouble(cleanValues[11]),
                s3 = parseDouble(cleanValues[13]),
                fiftyTwoWeekHigh = parseDouble(cleanValues[15]),
                fibonacci62Pct = parseDouble(cleanValues[17]),
                fibonacci50Pct = parseDouble(cleanValues[19]),
                fibonacci38Pct = parseDouble(cleanValues[21]),
                fiftyTwoWeekLow = parseDouble(cleanValues[25])
            )
        } catch (e: Exception) {
            Log.w(
                tag, "parseSnrBarchartPage() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Parses the finance.yahoo.com futures page.
     */
    private fun parseFuturesData(page: String): MajorFuturesData? {
        try {

            val document: Document = Jsoup.parse(page)

            val table = document.select("section#yfin-list")
            val rows = table.select("tr")

            // Data starts at index 1
            // Symbol, Name, LastPrice, MarketTime, ChangeDollar, ChangePercent, Volume(IntAbbre), AvgVolu(IntAbbre)
            val dataList = mutableListOf<List<String>>()
            for (n in 1..rows.lastIndex) {
                val cleanInnerData = mutableListOf<String>()
                val innerdata = rows[n].select("td")
                for (i in 0..innerdata.lastIndex) {
                    cleanInnerData.add(innerdata[i].text().replace(" ", ""))
                }
                dataList.add(cleanInnerData)
            }

            return MajorFuturesData(
                data = dataList.mapNotNull {
                    Future.createFromList(it)
                }
            )

        } catch (e: Exception) {
            Log.w(
                tag, "parseFuturesData() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }

    }


    /**
     * Parses the finance.yahoo.com Major Indices Page
     */
    private fun parseMajorIndicesData(page: String): MajorIndicesData? {

        try {
            val document = Jsoup.parse(page)

            val table = document.select("section#yfin-list")
            val rows = table.select("tr")

            // Data starts at index 1
            // Symbol, Name, LastPrice, MarketTime, ChangeDollar, ChangePercent, Volume(IntAbbre), AvgVolu(IntAbbre)
            val dataList = mutableListOf<List<String>>()
            for (n in 1..rows.lastIndex) {
                val cleanInnerData = mutableListOf<String>()
                val innerdata = rows[n].select("td")
                for (i in 0..innerdata.lastIndex) {
                    cleanInnerData.add(innerdata[i].text())
                }
                dataList.add(cleanInnerData)
            }

            return MajorIndicesData(
                data = dataList.mapNotNull {
                    Index.createFromList(it)
                }
            )


        } catch (e: Exception) {
            Log.w(
                tag, "parseMajorIndicesData() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Uses the return data of 'scrapeYfQuote()' to produce a StockQuote.
     */
    private fun parseStockQuoteData(
        scrapeData: Map<String, Map<String, String>?>?, ticker: String
    ): StockQuote? {
        try {

            val rawScrapeData = scrapeData ?: return null

            val topRowData = rawScrapeData["topRow"]!!
            val bodyData = rawScrapeData["body"]!!

            // Verify that quote IS NOT an EtfQuote
            if (bodyData.keys.contains("Inception Date")) {
                return null
            }

            return StockQuote(
                ticker = ticker,
                changeDollarRegMarket = parseDouble(topRowData["regChangeDol"]!!),
                changePctRegMarket = parseDouble(
                    topRowData["regChangePct"]!!
                        .substringAfter("(")
                        .substringBefore("%)")
                ),
                lastPriceRegMarket = parseDouble(topRowData["regLastPrice"]!!),
                prepostPrice = parseDouble(topRowData["ppLastPrice"]!!),
                prepostChangeDollar = parseDouble(topRowData["ppChangeDol"]!!),
                prepostChangePct = parseDouble(
                    topRowData["ppChangePct"]!!
                        .substringAfter("(")
                        .substringBefore("%")
                ),
                prevClose = parseDouble(bodyData["Previous Close"]!!),
                openPrice = parseDouble(bodyData["Open"]!!),
                bidPrice = parseDouble(
                    bodyData["Bid"]!!.substringBefore(" x")
                ),
                bidSize = parseInt(
                    bodyData["Bid"]!!.substringAfter("x ")
                ),
                askPrice = parseDouble(
                    bodyData["Ask"]!!.substringBefore(" x")
                ),
                askSize = parseInt(
                    bodyData["Ask"]!!.substringAfter("x ")
                ),
                daysRangeLow = parseDouble(
                    bodyData["Day's Range"]!!
                        .substringBefore(" -")
                ),
                daysRangeHigh = parseDouble(
                    bodyData["Day's Range"]!!
                        .substringAfter("- ")
                ),
                fiftyTwoWeekRangeLow = parseDouble(
                    bodyData["52 Week Range"]!!.substringBefore(" -")
                ),
                fiftyTwoWeekRangeHigh = parseDouble(
                    bodyData["52 Week Range"]!!.substringAfter("- ")
                ),
                volume = parseInt(bodyData["Volume"]!!),
                avgVolume = parseInt(bodyData["Avg. Volume"]!!),
                marketCap = parseLongFromBigAbbreviatedNumbers(bodyData["Market Cap"]!!),
                betaFiveYearMonthly = parseDouble(bodyData["Beta (5Y Monthly)"]!!),
                peRatioTTM = parseDouble(bodyData["PE Ratio (TTM)"]!!),
                epsTTM = parseDouble(bodyData["EPS (TTM)"]!!),
                nextEarningsDate = bodyData["Earnings Date"]!!
                    .substringBefore(" -"),
                forwardDivYieldValue = parseDouble(
                    bodyData["Forward Dividend & Yield"]!!
                        .substringBefore(" (")
                ),
                forwardDivYieldPercentage = parseDouble(
                    bodyData["Forward Dividend & Yield"]!!
                        .substringAfter("(")
                        .substringBefore("%")
                ),
                exDividendDate = bodyData["Ex-Dividend Date"]!!,
                oneYearTargetEstimate = parseDouble(bodyData["1y Target Est"]!!),
                marketCapAbbreviatedString = bodyData["Market Cap"]!!,
                name = topRowData["compName"]!!.substringBefore(" (")
            )

        } catch (e: Exception) {
            Log.w(
                tag, "parseStockQuoteData() failed to parse $ticker quote page.\n" +
                        "Exception: $e\n" +
                        "Stacktrace: ${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Uses the return data of 'scrapeYfQuote()' to produce a EtfQuote.
     */
    private fun parseEtfQuoteData(
        scrapeData: Map<String, Map<String, String>?>?, ticker: String
    ): EtfQuote? {
        try {

            val rawScrapeData = scrapeData ?: return null
            val topRowData = rawScrapeData["topRow"]!!
            val bodyData = rawScrapeData["body"]!!

            // Verify that quote IS an EtfQuote
            if (!bodyData.keys.contains("Inception Date")) {
                return null
            }

            return EtfQuote(
                ticker = ticker,
                changeDollarRegMarket = parseDouble(topRowData["regChangeDol"]!!),
                changePctRegMarket = parseDouble(
                    topRowData["regChangePct"]!!
                        .substringAfter("(")
                        .substringBefore("%)")
                ),
                lastPriceRegMarket = parseDouble(topRowData["regLastPrice"]!!),
                prepostPrice = parseDouble(topRowData["ppLastPrice"]!!),
                prepostChangeDollar = parseDouble(topRowData["ppChangeDol"]!!),
                prepostChangePct = parseDouble(
                    topRowData["ppChangePct"]!!
                        .substringAfter("(")
                        .substringBefore("%")
                ),
                prevClose = parseDouble(bodyData["Previous Close"]!!),
                openPrice = parseDouble(bodyData["Open"]!!),
                bidPrice = parseDouble(
                    bodyData["Bid"]!!.substringBefore(" x")
                ),
                bidSize = parseInt(
                    bodyData["Bid"]!!.substringAfter("x ")
                ),
                askPrice = parseDouble(
                    bodyData["Ask"]!!.substringBefore(" x")
                ),
                askSize = parseInt(
                    bodyData["Ask"]!!.substringAfter("x ")
                ),
                daysRangeLow = parseDouble(
                    bodyData["Day's Range"]!!
                        .substringBefore(" -")
                ),
                daysRangeHigh = parseDouble(
                    bodyData["Day's Range"]!!
                        .substringAfter("- ")
                ),
                fiftyTwoWeekRangeLow = parseDouble(
                    bodyData["52 Week Range"]!!.substringBefore(" -")
                ),
                fiftyTwoWeekRangeHigh = parseDouble(
                    bodyData["52 Week Range"]!!.substringAfter("- ")
                ),
                volume = parseInt(bodyData["Volume"]!!),
                avgVolume = parseInt(bodyData["Avg. Volume"]!!),
                netAssets = parseLongFromBigAbbreviatedNumbers(bodyData["Net Assets"]!!),
                nav = parseDouble(bodyData["NAV"]!!),
                peRatioTTM = parseDouble(bodyData["PE Ratio (TTM)"]!!),
                yieldPercentage = parseDouble(
                    bodyData["Yield"]!!.substringBefore("%")
                ),
                yearToDateTotalReturn = parseDouble(
                    bodyData["YTD Daily Total Return"]!!.substringBefore("%")
                ),
                betaFiveYearMonthly = parseDouble(bodyData["Beta (5Y Monthly)"]!!),
                expenseRatioNetPercentage = parseDouble(
                    bodyData["Expense Ratio (net)"]!!.substringBefore("%")
                ),
                inceptionDate = bodyData["Inception Date"]!!,
                netAssetsAbbreviatedString = bodyData["Net Assets"]!!,
                name = topRowData["compName"]!!.substringBefore(" (")
            )
        } catch (e: Exception) {
            Log.w(
                tag, "parseEtfQuoteData() failed to parse $ticker quote page. \n" +
                        "Exception: $e\n" +
                        "Stacktrace: ${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Uses the return data of 'scrapeBarchartQuoteData()' to produce an OptionStats object.
     */
    private fun parseOptionStats(
        scrapeData: List<List<String>>?,
        ticker: String
    ): OptionStats? {

        try {

            val stringDateToJavaData = { s: String ->
                SimpleDateFormat("MM/dd/yy").parse(s)
            }

            val tick = ticker.uppercase()

            val scrapedStats = scrapeData ?: return null

            // Find starting index... It's different for Stock / ETF...Build list of target data
            var foundStart = false
            val dataActual = mutableListOf<List<String>>()
            for (n in 0..scrapedStats.lastIndex) {
                if (foundStart) {
                    dataActual.add(scrapedStats[n])
                    continue
                }
                if (scrapedStats[n].contains("Implied Volatility")) {
                    foundStart = true
                    dataActual.add(scrapedStats[n])
                }
            }

            return OptionStats(
                tick,
                parseDouble(dataActual[0][1].substringBefore("%")),
                parseDouble(
                    dataActual[0][1].substringAfter(
                        "("
                    ).substringBefore("%")
                ),
                parseDouble(
                    dataActual[1][1].substringBefore("%")
                ),
                parseDouble(dataActual[2][1].substringBefore("%")),
                parseDouble(dataActual[3][1].substringBefore("%")),
                parseDouble(dataActual[4][1].substringBefore("%")),
                stringDateToJavaData(dataActual[4][1].substringAfter("on "))!!,
                parseDouble(dataActual[5][1].substringBefore("%")),
                stringDateToJavaData(dataActual[5][1].substringAfter("on "))!!,
                parseDouble(dataActual[6][1]),
                parseInt(dataActual[7][1]),
                parseInt(dataActual[8][1]),
                parseDouble(dataActual[9][1]),
                parseInt(dataActual[10][1]),
                parseInt(dataActual[11][1])
            )
        } catch (e: Exception) {
            Log.w(
                tag, "parseOptionsStats() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Parses a stock's Competitors data from marketwatch.com
     */
    private fun parseStocksCompetitors(page: String): StockCompetitorsList? {

        try {
            // Used to extract company ticker from data
            val extractSymbolFromLink = { link: String ->

                var result: String? = null

                val sym = link.substringAfter("/investing/stock/")
                    .substringBefore("?mod")

                if (!sym.contains("=")) {
                    result = sym
                }
                result
            }

            val doc = Jsoup.parse(page)

            val rowData = mutableListOf<List<String>>()
            doc.select("table").forEach { table ->
                // Loop through table rows until start point is found, then extract data
                val rows = table.select("tr")
                var start = false
                for (r in rows) {
                    if (!start) {
                        if (r.text().contains("Name Chg % Market Cap")) {
                            start = true
                        }
                    } else {
                        val data = r.select("td")
                        val cleanData = mutableListOf<String>()
                        cleanData.add(
                            extractSymbolFromLink(
                                data[0].select("a").attr("href")
                            )?.uppercase() ?: ""
                        )
                        for (d in data) {
                            cleanData.add(d.text())
                        }
                        rowData.add(cleanData)
                    }
                }
            }

            val competitorsList = mutableListOf<StockCompetitor>()
            for (row in rowData) {
                if (row[0] != "") {
                    competitorsList.add(
                        StockCompetitor(
                            ticker = row[0],
                            companyName = row[1],
                            pctChange = parseDouble(row[2].substringBefore("%")),
                            marketCap = parseLongFromBigAbbreviatedNumbers(
                                row[3].substringAfter("$")
                            ),
                            marketCapAbbreviatedString = row[3]
                        )
                    )
                }
            }

            if (competitorsList.isEmpty()) {
                return null
            }

            return StockCompetitorsList(
                competitorsList = competitorsList
            )
        } catch (e: Exception) {
            Log.w(
                tag, "parseStocksCompetitors() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    private fun parseAnalystsPriceTargetRatings(page: String): AnalystPriceTargetRatings? {
        try {
            val doc = Jsoup.parse(page)
            val rows = doc.select("tr")

            val payload = mutableListOf<AnalystRating>()
            for (r in rows) {
                val data = r.select("td")
                if (data.size != 8) {
                    continue
                }
                val symbol = data[0].select("div.ticker-area").text()
                val assetName = data[0].select("div.title-area").text()
                val analystAction = data[1].text()
                val brokerageName = data[2].text().substringBefore(" Subscribe to MarketBeat")
                val analystAtBrokerage = if (data[3].text().isNullOrEmpty()) "Not Specified" else data[3].text()
                val curPriceAndChangePct = data[4].text()
                val oldTargetNewTarget = data[5].text()
                val rating = data[6].text().substringAfter(" ➝ ").ifBlank { "None" }

                val oldPriceTarget = parseDouble(
                    oldTargetNewTarget.substringBefore(" ➝").replace("$", "")
                )
                val newPriceTarget = parseDouble(
                    oldTargetNewTarget.substringAfter(" ➝").replace("$", "")
                )

                payload.add(
                    AnalystRating(
                        ticker = symbol,
                        assetName = assetName,
                        action = analystAction.replace(" by", ""),
                        brokerageName = brokerageName,
                        analystName = analystAtBrokerage,
                        curPrice = parseDouble(
                            curPriceAndChangePct.substringBefore(" ").replace("$", "")
                        ),
                        pctChange = parseDouble(
                            curPriceAndChangePct.substringAfter(" ").replace("%", "")
                        ),
                        oldPriceTarget = oldPriceTarget,
                        newPriceTarget = newPriceTarget,
                        sizeOfChange = newPriceTarget - oldPriceTarget,
                        rating = rating
                    )
                )
            }
            return AnalystPriceTargetRatings(payload)

        } catch (e: Exception) {
            Log.w(
                tag, "parseAnalystsPriceTargetRatings() triggered an exception:" +
                        " $e\n${e.stackTraceToString()}"
            )
            return null
        }
    }


    /**
     * Builds a request to retrieve data from
     */
    private fun buildUoaRequest(
        pageNumber: Int,
        sortAsc: Boolean = false,
        sortBy: UoaSortByOptions = UoaSortByOptions.VolOiRatio
    ): Request {

        val url = getUnusualOptionsActivityUrl(pageNumber) + "&sort_asc=$sortAsc&sort_by=${sortBy.key}"

        return Request.Builder()
            // WARNING -- Don't ever increase the page size above 90.
            // That is for "Premium" users of app.fdscanner.com
//            .url(
//                getUnusualOptionsActivityUrl(pageNumber)
//            )
            .url(url)
            .get()
            .addHeader("Accept", "*/*")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader("Connection", "keep-alive")
            .addHeader(
                "Cookie",
                "popupShown=Fri%20Sep%2002%202022%2012:18:43%20GMT-0400%20(Eastern%20Daylight%20Time)"
            )
            .addHeader("Referer", "https://app.fdscanner.com/unusualvolume")
            .addHeader("Sec-Fetch-Dest", "empty")
            .addHeader("Sec-Fetch-Mode", "cors")
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36"
            )
            .addHeader(
                "sec-ch-ua",
                "\"Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104\""
            )
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("sec-ch-ua-platform", "\"macOS\"")
            .build()
    }


    // Synchronous Methods ------------------------------------------------------------------------


    /**
     * Retrieves Support and Resistance Levels for ticker.
     *
     *   - Approximate Last Price
     *   - S1, S2, S3, R1, R2, R3
     *   - 52wk High, Fib 61.8%, Fib 50%, Fib 38.2%, 52wk Low
     */
    override fun getSnrLevels(ticker: String): SnRLevels? {

        val url = getSnrLevelsUrl(ticker)
        val page = NetworkClient.getWebpage(url) ?: return null
        return parseSupportAndResistanceBarchartPage(page, ticker)
    }


    /**
     * - Retrieves data for...
     *   - All major Index Futures
     *   - US Treasury Bond, 10yr Note, 5yr Note, 2yr Note
     *   - Gold, Silver
     *   - Oil, Gas
     */
    override fun getFuturesData(): MajorFuturesData? {
        val url = futuresDataUrl
        val page = NetworkClient.getWebpage(url) ?: return null
        return parseFuturesData(page)
    }


    /**
     * Returns the data for major world Indices.
     *
     *  - SP500
     *  - Dow
     *  - Nasdaq
     *  - Russel 2000
     *  - VIX
     *  - FTSE (Europe)
     *  - HengSeng (China)
     */
    override fun getMajorIndicesData(): MajorIndicesData? {
        val page = NetworkClient.getWebpage(majorIndicesDataUrl) ?: return null
        return parseMajorIndicesData(page)
    }


    /**
     * Retrieves a StockQuote for stock symbol. Only usable with Stock Symbols. ETF Symbols
     * are incompatible, use getEtfQuote() instead.
     */
    override fun getStockQuote(ticker: String): StockQuote? {
        val page = NetworkClient.getWebpage(
            getYfQuoteUrl(ticker)
        ) ?: return null
        val scrapeData = scrapeYfQuote(page) ?: return null
        return parseStockQuoteData(scrapeData, ticker)
    }


    /**
     * Retrieves a EtfQuote for etf symbol. Only usable with ETF Symbols. Stock Symbols
     * are incompatible, use getStockQuote() instead.
     */
    override fun getEtfQuote(ticker: String): EtfQuote? {
        val page = NetworkClient.getWebpage(
            getYfQuoteUrl(ticker)
        ) ?: return null
        val scrapeData = scrapeYfQuote(page) ?: return null
        return parseEtfQuoteData(scrapeData, ticker)
    }


    /**
     * Retrieves OptionStats for given ticker.
     */
    override fun getOptionStats(ticker: String): OptionStats? {
        val page = NetworkClient.getWebpage(
            getOptionStatsUrl(ticker)
        ) ?: return null
        val scrapedData = scrapeBarchartQuoteTable(page) ?: return null
        return parseOptionStats(scrapedData, ticker)
    }


    /**
     * Retrieves a StockCompetitorsList for given ticker.
     */
    override fun getStocksCompetitors(ticker: String): StockCompetitorsList? {
        val page = NetworkClient.getWebpage(
            getStocksCompetitorsUrl(ticker)
        ) ?: return null
        return parseStocksCompetitors(page)
    }

        /**
     * Retrieves unusual stock option activity, defined by high volume to open interest ratio.
     * Option volume that exceeds open interest signals new positions by institutional traders
     */
    override fun getUnusualOptionsActivity(pageNumber: Int, sortAsc: Boolean, sortBy: UoaSortByOptions): UnusualOptionsActivityPage? {
        return try {

            val response = NetworkClient.getClient()
                .newCall(buildUoaRequest(pageNumber, sortAsc, sortBy)).execute()

            if (response.isSuccessful) {
                val rawRespObj = gson.fromJson(
                    response.body!!.string(),
                    RawUnusualOptionsActivityPage::class.java
                )
                rawRespObj.toUnusualOptionsActivityPage()
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.w("EXCEPTION", ex.stackTraceToString())
            null
        }    }


    /**
     * Retrieves the Latest Price Change ratings by Top Analysts
     * */

    override fun getLatestAnalystPriceTargetRatings(): AnalystPriceTargetRatings? {

        val page = NetworkClient.getWebpage(analystsPriceTargetChangesUrl) ?: return null
        return parseAnalystsPriceTargetRatings(page)
    }


    // Async Methods -----------------------------------------------------------------------------

    /**
     * Retrieves Support and Resistance Levels for ticker asynchronously.
     *
     *  - Approximate Last Price
     *  - S1, S2, S3, R1, R2, R3
     *  - 52wk High, Fib 61.8%, Fib 50%, Fib 38.2%, 52wk Low
     */
    override suspend fun getSnrLevelsAsync(ticker: String): SnRLevels? {
        val url = getSnrLevelsUrl(ticker)
        val page = NetworkClient.getWebpageAsync(url) ?: return null
        return parseSupportAndResistanceBarchartPage(page, ticker)
    }


    /**
     * Asynchronously retrieves data for...
     *  - All major Index Futures
     *  - US Treasury Bond, 10yr Note, 5yr Note, 2yr Note
     *  - Gold, Silver
     *  - Oil, Gas
     */
    override suspend fun getFuturesDataAsync(): MajorFuturesData? {
        val url = futuresDataUrl
        val page = NetworkClient.getWebpageAsync(url) ?: return null
        return parseFuturesData(page)
    }


    /**
     * Asynchronously returns the data for major world Indices.
     *
     *  - SP500
     *  - Dow
     *  - Nasdaq
     *  - Russel 2000
     *  - VIX
     *  - FTSE (Europe)
     *  - HengSeng (China)
     */
    override suspend fun getMajorIndicesDataAsync(): MajorIndicesData? {
        val page = NetworkClient.getWebpageAsync(majorIndicesDataUrl) ?: return null
        return parseMajorIndicesData(page)
    }


    /**
     * Asynchronously retrieves a EtfQuote for etf symbol. Only usable with ETF Symbols. Stock Symbols
     * are incompatible, use getStockQuote() instead.
     */
    override suspend fun getStockQuoteAsync(ticker: String): StockQuote? {
        val page = NetworkClient.getWebpageAsync(
            getYfQuoteUrl(ticker)
        ) ?: return null
        val scrapeData = scrapeYfQuote(page) ?: return null
        return parseStockQuoteData(scrapeData, ticker)
    }


    /**
     * Asynchronously retrieves a EtfQuote for etf symbol. Only usable with ETF Symbols. Stock Symbols
     * are incompatible, use getStockQuote() instead.
     */
    override suspend fun getEtfQuoteAsync(ticker: String): EtfQuote? {
        val page = NetworkClient.getWebpageAsync(
            getYfQuoteUrl(ticker)
        ) ?: return null
        val scrapeData = scrapeYfQuote(page) ?: return null
        return parseEtfQuoteData(scrapeData, ticker)
    }


    /**
     * Asynchronously retrieves OptionStats for given ticker.
     */
    override suspend fun getOptionStatsAsync(ticker: String): OptionStats? {
        val page = NetworkClient.getWebpage(
            getOptionStatsUrl(ticker)
        ) ?: return null
        val scrapedData = scrapeBarchartQuoteTable(page) ?: return null
        return parseOptionStats(scrapedData, ticker)
    }


    /**
     * Asynchronously retrieves a StockCompetitorsList for given ticker.
     */
    override suspend fun getStocksCompetitorsAsync(ticker: String): StockCompetitorsList? {
        val page = NetworkClient.getWebpageAsync(
            getStocksCompetitorsUrl(ticker)
        ) ?: return null
        return parseStocksCompetitors(page)
    }


    /**
     * Asynchronously retrieves unusual stock option activity, defined by high volume to open interest ratio.
     * Option volume that exceeds open interest signals new positions by institutional traders
     */
    override suspend fun getUnusualOptionsActivityAsync(pageNumber: Int, sortAsc: Boolean, sortBy: UoaSortByOptions): UnusualOptionsActivityPage? {
        try {

            val deferredResp = CoroutineScope(coroutineContext)
                .async(Dispatchers.Unconfined) {
                    NetworkClient.getClient()
                        .newCall(buildUoaRequest(pageNumber, sortAsc, sortBy)).execute()
                }

            val response = deferredResp.await()

            return if (response.isSuccessful) {
                val rawRespObj = gson
                    .fromJson(response.body!!.string(), RawUnusualOptionsActivityPage::class.java)

                rawRespObj.toUnusualOptionsActivityPage()
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.w("EXCEPTION", ex.stackTraceToString())
            return null
        }
    }


    /**
     * Asynchronously retrieves the Latest Price Change ratings by Top Analysts
     */
    override suspend fun getLatestAnalystPriceTargetRatingsAsync(): AnalystPriceTargetRatings? {
        val page = NetworkClient.getWebpageAsync(analystsPriceTargetChangesUrl) ?: return null
        return parseAnalystsPriceTargetRatings(page)
    }




}
