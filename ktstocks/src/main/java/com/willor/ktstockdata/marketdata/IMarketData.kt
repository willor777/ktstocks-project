package com.willor.ktstockdata.marketdata

import com.willor.ktstockdata.marketdata.dataobjects.*

interface IMarketData {

    /**
     * Synchronous Requests
     */

    fun getSnrLevels(ticker: String): SnRLevels?

    fun getFuturesData(): MajorFuturesData?

    fun getMajorIndicesData(): MajorIndicesData?

    fun getStockQuote(ticker: String): StockQuote?

    fun getEtfQuote(ticker: String): EtfQuote?

    fun getOptionStats(ticker: String): OptionStats?

    fun getStocksCompetitors(ticker: String): StockCompetitorsList?

    fun getUnusualOptionsActivity(pageNumber: Int = 0): UnusualOptionsActivityPage?

    fun getAnalystsUpgradesDowngrades(): AnalystsUpgradesDowngrades?

    /**
     * Async Requests
     */

    suspend fun getSnrLevelsAsync(ticker: String): SnRLevels?

    suspend fun getFuturesDataAsync(): MajorFuturesData?

    suspend fun getMajorIndicesDataAsync(): MajorIndicesData?

    suspend fun getStockQuoteAsync(ticker: String): StockQuote?

    suspend fun getEtfQuoteAsync(ticker: String): EtfQuote?

    suspend fun getOptionStatsAsync(ticker: String): OptionStats?

    suspend fun getStocksCompetitorsAsync(ticker: String): StockCompetitorsList?

    suspend fun getUnusualOptionsActivityAsync(pageNumber: Int = 0): UnusualOptionsActivityPage?

    suspend fun getAnalystsUpgradesDowngradesAsync(): AnalystsUpgradesDowngrades?

}
