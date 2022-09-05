package com.willor.ktstockdata.marketdata

import com.willor.ktstockdata.common.*
import com.willor.ktstockdata.common.NetworkClient
import com.willor.ktstockdata.common.parseDouble
import com.willor.ktstockdata.common.parseInt
import com.willor.ktstockdata.common.parseLongFromBigAbbreviatedNumbers
import com.willor.ktstockdata.marketdata.dataobjects.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class MarketData: IMarketData {


    /**
     * Scrapes the Overall Option Stats from Barchart.com... Includes Quote data as well.
     */
    private fun scrapeBarchartQuoteTable(ticker: String): List<List<String>>?{

        try{
            // NOTE -- Even though this url is for a Stock, it seems to work for ETFs as well
            // further testing is required
            val url = "https://www.barchart.com/stocks/quotes/$ticker/overview"
            val page = NetworkClient.getWebpage(url) ?: return null
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
        }catch(e: Exception){
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }


    private fun scrapeYfQuote(ticker: String): Map<String, Map<String, String>?>?{

        // Responsible for scraping the top row data. Returns Map<String, String>
        val scrapeTopRow = { doc: Document ->
            val results = mutableMapOf<String, String>()

            val topRowElements = doc.select("div#quote-header-info")

            // Set up place holders + pull easy data
            val compName = topRowElements.select("h1").text()
            var regLastPrice = ""
            var regChangeDol = ""
            var regChangePct = "(0.0%)"
            var ppLastPrice = "0.0"
            var ppChangeDol = "0.0"
            var ppChangePct = "(0.0%)"

            // Pull the data
            val rawDataElements = topRowElements.select("fin-streamer")

            // Build data
            val data = mutableListOf<String>()
            for (d in rawDataElements){
                data.add(d.text())
            }

            // Regular market price info is always there
            regLastPrice = data[0]
            regChangeDol = data[1]
            regChangePct = data[2]

            // After market is only there after / before market open/close
            if (data.size >= 8){
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
            for (r in rawDataRows){
                val data = r.select("td")
                if (data.size == 2){
                    results[data[0].text()] = data[1].text()
                }
            }

            results
        }


        val url = "https://finance.yahoo.com/quote/$ticker?p=$ticker&.tsrc=fin-srch"
        val page = NetworkClient.getWebpage(url) ?: return null
        val doc = Jsoup.parse(page)

        // Base map
        val payload = mutableMapOf<String, Map<String, String>?>()
        payload["topRow"] = scrapeTopRow(doc)
        payload["body"] = scrapeBody(doc)

        return payload.toMap()
    }


    override fun getStockQuote(ticker: String): StockQuote? {

        val parseDateYF = {s: String ->
            val result: Date?
            if (s.contains("N/A")){
                result = null
            }else{
                var str = s
                if (s.contains("-")){
                    str = s.substringBefore("-")
                }
                result = SimpleDateFormat("MMM dd, yyyy").parse(str)
            }
            result
        }

        val rawScrapeData = scrapeYfQuote(ticker) ?: return null

        val topRowData = rawScrapeData["topRow"]!!
        val bodyData = rawScrapeData["body"]!!

        // Verify that quote IS NOT an EtfQuote
        if (bodyData.keys.contains("Inception Date")){
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

    }


    override fun getETFQuote(ticker: String): EtfQuote? {

        val dateFromString = { s: String ->
            val result: Date?
            if (s.contains("N/A")){
                result = null
            }else{
                result = SimpleDateFormat("yyyy-MM-dd").parse(s)
            }
            result
        }

        val rawScrapeData = scrapeYfQuote(ticker) ?: return null
        val topRowData = rawScrapeData["topRow"]!!
        val bodyData = rawScrapeData["body"]!!

        // Verify that quote IS an EtfQuote
        if (!bodyData.keys.contains("Inception Date")){
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
    }


    override fun getOptionStats(ticker: String): OptionStats? {

        try{

            val stringDateToJavaData = { s: String ->
                SimpleDateFormat("MM/dd/yy").parse(s)
            }

            val tick = ticker.uppercase()

            val scrapedStats = scrapeBarchartQuoteTable(tick) ?: return null

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
                parseDouble(dataActual[0][1] .substringAfter(
                    "(").substringBefore("%")),
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
        }catch (e: Exception){
            Log.d("EXCEPTION", "Ticker $ticker CAUSED AN EXCEPTION. FAILED TO OBTAIN " +
                    "OPTION STATS\n" + e.stackTraceToString())
            return null
        }
    }


    override fun getStocksCompetitors(ticker: String): StockCompetitorsList? {

        try{
            // Used to extract company ticker from data
            val extractSymbolFromLink = {link: String ->

                var result: String? = null

                val sym = link.substringAfter("/investing/stock/")
                    .substringBefore("?mod")

                if (!sym.contains("=")) {
                    result = sym
                }
                result
            }

            val url = "https://www.marketwatch.com/investing/stock/$ticker?mod=search_symbol"
            val page = NetworkClient.getWebpage(url)
            val doc = Jsoup.parse(page)

            val rowData = mutableListOf<List<String>>()
            doc.select("table").forEach{table ->
                // Loop through table rows until start point is found, then extract data
                val rows = table.select("tr")
                var start = false
                for (r in rows){
                    if (!start){
                        if (r.text().contains("Name Chg % Market Cap")){start = true}
                    }else{
                        val data = r.select("td")
                        val cleanData = mutableListOf<String>()
                        cleanData.add(extractSymbolFromLink(data[0].select("a").attr("href"))?.uppercase() ?: "")
                        for (d in data){
                            cleanData.add(d.text())
                        }
                        rowData.add(cleanData)
                    }
                }
            }

            val competitorsList = mutableListOf<StockCompetitor>()
            for (row in rowData){
                if (row[0] != ""){
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

            if (competitorsList.isEmpty()){return null}

            return StockCompetitorsList(
                competitorsList = competitorsList
            )
        } catch (e: Exception){
            Log.d("EXCEPTION", "Ticker $ticker CAUSED AN EXCEPTION. FAILED TO OBTAIN" +
                    " StockCompetitors List\n" + e.stackTraceToString())
            return null
        }
    }


    /**
     * Scrapes the finance.yahoo.com World Index List. Only returns the Major ones as a
     * MajorIndicesData? Object filled with Index objects for...
     *
     * SP500
     * Dow
     * Nasdaq
     * Russel 2000
     * VIX
     * FTSE (Europe)
     * HengSeng (China)
     */
    override fun getMajorIndicesData(): MajorIndicesData? {

        val stripDataGetIndex = {data: List<String> ->
            try{
                Index(
                    data[0].replace(" ", ""),
                    data[1],
                    parseDouble(data[2]),
                    parseDouble(data[3]),
                    parseDouble(data[4].substringBefore("%")),
                    parseLongFromBigAbbreviatedNumbers(data[5])
                )

            }catch (e: Exception){
                Log.d("EXCEPTION", e.stackTraceToString() + "\n CAUSE...$data")
                null
            }
        }

        try{
            val url = "https://finance.yahoo.com/world-indices"
            val page = NetworkClient.getWebpage(url) ?: return null
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
            for (n in 0..dataList.lastIndex){
                val data = dataList[n]

                when(data[0]){
                    "^GSPC" ->{
                        indexMap["sp"] = stripDataGetIndex(data)
                    }
                    "^DJI" ->{
                        indexMap["dow"] = stripDataGetIndex(data)
                    }
                    "^IXIC" ->{
                        indexMap["nas"] = stripDataGetIndex(data)
                    }
                    "RUT" ->{
                        indexMap["russ"] = stripDataGetIndex(data)
                    }
                    "^VIX" ->{
                        indexMap["vix"] = stripDataGetIndex(data)
                    }
                    "^FTSE" ->{
                        indexMap["ftse"] = stripDataGetIndex(data)
                    }
                    "^HSI" ->{
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

        }catch (e: Exception){
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }


    /**
     * Scrapes the finance.yahoo.com Futures list.
     * - Retrieves data for...
     *      - All major Index Futures
     *      - US Treasury Bond, 10yr Note, 5yr Note, 2yr Note
     *      - Gold, Silver
     *      - Oil, Gas
     * - Returns
     *      - [FuturesData]? : Which is a data class containing [Future] objects
     */
    override fun getFuturesData(): MajorFuturesData?{
        try{
            val url = "https://finance.yahoo.com/commodities"

            val rawHtmlString = NetworkClient.getWebpage(url) ?: return null

            val document: Document = Jsoup.parse(rawHtmlString)


//            val document = Jsoup.connect(url).userAgent(getRandomUserAgent()).get() ?: return null

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
        }catch(e: Exception){
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }


    /**
     * Scrapes the Support and Resistance levels from Barchart.com Quote Page.
     *
     * - Includes...
     *
     *      - Approximate Last Price
     *
     *      - S1, S2, S3, R1, R2, R3
     *
     *      - 52wk High, Fib 61.8%, Fib 50%, Fib 38.2%, 52wk Low
     */
    override fun getSupportAndResistanceFromBarchartDotCom(ticker: String): SnRLevels?{
        try{
            // Get Document
            val tick = ticker.uppercase()
            val url = "https://www.barchart.com/stocks/quotes/${tick}/overview"
            val page = NetworkClient.getWebpage(url) ?: return null
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
        } catch(e: Exception){
            Log.d("EXCEPTION", e.stackTraceToString())
            return null
        }
    }

}
