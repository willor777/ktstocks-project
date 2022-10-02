package com.willor.ktstockdata.marketdata

import com.google.gson.Gson
import com.willor.ktstockdata.common.*
import com.willor.ktstockdata.marketdata.dataobjects.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext


// TODO Improve Logging Messages -> Include Input Ticker + Fun name + Exception type + Stacktrace
// TODO Finish writing docs
class MarketData : IMarketData {

    private val tag: String = MarketData::class.java.simpleName


    // Urls --------------------------------------------------------------------------------------


    private val getSnrLevelsUrl = {tick: String ->
        "https://www.barchart.com/stocks/quotes/${tick.uppercase()}/overview"
    }

    private val getYfQuoteUrl = {tick: String ->
        val ticker = tick.uppercase()
        "https://finance.yahoo.com/quote/$ticker?p=$ticker&.tsrc=fin-srch"
    }

    private val getOptionStatsUrl = {ticker: String ->
        "https://www.barchart.com/stocks/quotes/${ticker.uppercase()}/overview"
    }

    private val getStocksCompetitorsUrl = {ticker: String ->
        "https://www.marketwatch.com/investing/stock/$ticker?mod=search_symbol"
    }

    private val getUnusualOptionsActivityUrl = {pageNum: Int ->
        "https://app.fdscanner.com/api2/unusualvolume?p=0&page_size=50&page=$pageNum"
    }

    private val futuresDataUrl = "https://finance.yahoo.com/commodities"

    private val majorIndicesDataUrl = "https://finance.yahoo.com/world-indices"


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
            Log.d("EXCEPTION", e.stackTraceToString())
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
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }


    /**
     * Parses the barchart.com quote page for Support and Resistance data.
     */
    private fun parseSupportAndResistanceBarchartPage(page: String, ticker: String): SnRLevels?{
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
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }


    /**
     * Parses the finance.yahoo.com futures page.
     */
    private fun parseFuturesData(page: String): MajorFuturesData?{
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
                sp500Future = Future.createFromList(dataList[0]),
                dowFuture = Future.createFromList(dataList[1]),
                nasdaqFuture = Future.createFromList(dataList[2]),
                russel2000Future = Future.createFromList(dataList[3]),
                usTreasuryBondFuture = Future.createFromList(dataList[4]),
                usTenYearTreasuryNoteFuture = Future.createFromList(dataList[5]),
                usFiveYearTreasuryNoteFuture = Future.createFromList(dataList[6]),
                usTwoYearTreasureNoteFuture = Future.createFromList(dataList[7]),
                goldFuture = Future.createFromList(dataList[8]),
                goldMicroFuture = Future.createFromList(dataList[9]),
                silverFuture = Future.createFromList(dataList[10]),
                microSilverFuture = Future.createFromList(dataList[11]),
                crudeOilWTIFuture = Future.createFromList(dataList[15]),
                rbobGasolineFuture = Future.createFromList(dataList[18]),
                brentCrudeOilFuture = Future.createFromList(dataList[19])
            )
        } catch (e: Exception) {
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }

    }


    /**
     * Parses the finance.yahoo.com Major Indices Page
     */
    private fun parseMajorIndicesData(page: String): MajorIndicesData? {
        val stripDataGetIndex = { data: List<String> ->
            try {
                Index(
                    data[0].replace(" ", ""),
                    data[1],
                    parseDouble(data[2]),
                    parseDouble(data[3]),
                    parseDouble(data[4].substringBefore("%")),
                    parseLongFromBigAbbreviatedNumbers(data[5])
                )

            } catch (e: Exception) {
                Log.d("EXCEPTION", e.stackTraceToString() + "\n CAUSE...$data")
                null
            }
        }

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
                    cleanInnerData.add(innerdata[i].text().replace(" ", ""))
                }
                dataList.add(cleanInnerData)
            }

            val indexMap = mutableMapOf<String, Index?>()
            indexMap["sp"] = null; indexMap["dow"] = null; indexMap["nas"] = null
            indexMap["russ"] = null; indexMap["vix"] = null; indexMap["ftse"] = null
            indexMap["heng"] = null
            for (n in 0..dataList.lastIndex) {
                val data = dataList[n]

                when (data[0]) {
                    "^GSPC" -> {
                        indexMap["sp"] = stripDataGetIndex(data)
                    }
                    "^DJI" -> {
                        indexMap["dow"] = stripDataGetIndex(data)
                    }
                    "^IXIC" -> {
                        indexMap["nas"] = stripDataGetIndex(data)
                    }
                    "RUT" -> {
                        indexMap["russ"] = stripDataGetIndex(data)
                    }
                    "^VIX" -> {
                        indexMap["vix"] = stripDataGetIndex(data)
                    }
                    "^FTSE" -> {
                        indexMap["ftse"] = stripDataGetIndex(data)
                    }
                    "^HSI" -> {
                        indexMap["heng"] = stripDataGetIndex(data)
                    }
                }

            }

            return MajorIndicesData(
                sp500 = indexMap["sp"],
                dow = indexMap["dow"],
                nasdaq = indexMap["nas"],
                russel2000 = indexMap["russ"],
                vix = indexMap["vix"],
                ftse100 = indexMap["ftse"],
                hengSeng = indexMap["heng"]
            )

        } catch (e: Exception) {
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }


    /**
     * Uses the return data of 'scrapeYfQuote()' to produce a StockQuote.
     */
    private fun parseStockQuoteData(
        scrapeData: Map<String, Map<String, String>?>?, ticker: String
    ): StockQuote?{
        try{

            val parseDateYF = { s: String ->
                val result: Date?
                if (s.contains("N/A")) {
                    result = null
                } else {
                    var str = s
                    if (s.contains("-")) {
                        str = s.substringBefore("-")
                    }
                    result = SimpleDateFormat("MMM dd, yyyy").parse(str)
                }
                result
            }

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
                nextEarningsDate = parseDateYF(
                    bodyData["Earnings Date"]!!
                        .substringBefore(" -")
                ),
                forwardDivYieldValue = parseDouble(
                    bodyData["Forward Dividend & Yield"]!!
                        .substringBefore(" (")
                ),
                forwardDivYieldPercentage = parseDouble(
                    bodyData["Forward Dividend & Yield"]!!
                        .substringAfter("(")
                        .substringBefore("%")
                ),
                exDividendDate = parseDateYF(bodyData["Ex-Dividend Date"]!!),
                oneYearTargetEstimate = parseDouble(bodyData["1y Target Est"]!!),
                marketCapAbbreviatedString = bodyData["Market Cap"]!!
            )

        }catch (e: Exception){
            Log.d(tag, "Failed to parse $ticker quote page.\n" +
                    "Exception: ${e.toString()}\n" +
                    "Stacktrace: ${e.stackTraceToString()}")
            return null
        }
    }


    /**
     * Uses the return data of 'scrapeYfQuote()' to produce a EtfQuote.
     */
    private fun parseEtfQuoteData(
        scrapeData: Map<String, Map<String, String>?>?, ticker: String
    ): EtfQuote?{
        try{

            val dateFromString = { s: String ->
                val result: Date?
                if (s.contains("N/A")) {
                    result = null
                } else {
                    result = SimpleDateFormat("yyyy-MM-dd").parse(s)
                }
                result
            }
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
                inceptionDate = dateFromString(bodyData["Inception Date"]!!),
                netAssetsAbbreviatedString = bodyData["Net Assets"]!!
            )
        }catch (e: Exception){
            Log.d(tag, "Failed to parse $ticker quote page. \n" +
                    "Exception: ${e.toString()}\n" +
                    "Stacktrace: ${e.stackTraceToString()}")
            return null
        }
    }


    /**
     * Uses the return data of 'scrapeBarchartQuoteData()' to produce an OptionStats object.
     */
    private fun parseOptionStats(
        scrapeData: List<List<String>>?,
        ticker: String
    ): OptionStats?{

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
            Log.d(
                "EXCEPTION", "Ticker $ticker CAUSED AN EXCEPTION. FAILED TO OBTAIN " +
                        "OPTION STATS\n" + e.stackTraceToString()
            )
            return null
        }
    }


    /**
     * Parses a stock's Competitors data from marketwatch.com
     */
    private fun parseStocksCompetitors(page: String, ticker: String): StockCompetitorsList? {

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
            Log.d(
                "EXCEPTION", "Ticker $ticker CAUSED AN EXCEPTION. FAILED TO OBTAIN" +
                        " StockCompetitors List\n" + e.stackTraceToString()
            )
            return null
        }
    }


    /**
     * Builds a request to retrieve data from
     */
    private fun buildUoaRequest(pageNumber: Int): Request{

        return Request.Builder()
            // WARNING -- Don't ever increase the page size above 90.
            // That is for "Premium" users of app.fdscanner.com
            .url(
                getUnusualOptionsActivityUrl(pageNumber)
            )
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
     * are incompatable, use getEtfQuote() instead.
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
     * are incompatable, use getStockQuote() instead.
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
        return parseStocksCompetitors(page, ticker)
    }


    /**
     * Retrieves unusual stock option activity, defined by high volume to open interest ratio.
     * Option volume that exceeds open interest signals new positions by institutional traders
     */
    override fun getUnusualOptionsActivity(pageNumber: Int): UnusualOptionsActivityPage? {

        try {

            val response = NetworkClient.getClient()
                .newCall(buildUoaRequest(pageNumber)).execute()

            return if (response.isSuccessful) {
                Gson().fromJson(response.body!!.string(), UnusualOptionsActivityPage::class.java)
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.d("EXCEPTION", ex.stackTraceToString())
            return null
        }
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
     * are incompatable, use getStockQuote() instead.
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
     * are incompatable, use getStockQuote() instead.
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
        return parseStocksCompetitors(page, ticker)
    }


    /**
     * Asynchronously retrieves unusual stock option activity, defined by high volume to open interest ratio.
     * Option volume that exceeds open interest signals new positions by institutional traders
     */
    override suspend fun getUnusualOptionsActivityAsync(pageNumber: Int): UnusualOptionsActivityPage? {
        try {

            val deferredResp = CoroutineScope(coroutineContext)
                .async(Dispatchers.Unconfined){
                    NetworkClient.getClient()
                        .newCall(buildUoaRequest(pageNumber)).execute()
            }

            val response = deferredResp.await()

            return if (response.isSuccessful) {
                Gson().fromJson(response.body!!.string(), UnusualOptionsActivityPage::class.java)
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.d("EXCEPTION", ex.stackTraceToString())
            return null
        }
    }
}
