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

    /*
    TODO - UOA sort options from server
        Sort Options (param = "sort_by")...
        - OTM % = po
        - Strike = s
        - Type (call /put) = t
        - Expiry = expiry
        - Last Price = op
        - Volume = v
        - Open Int = oi
        - Vol/OI = vol/oi
        - IV = iv
        * Notes
            * 'sort_asc=<bool>' param must be there too
     */

    fun getUnusualOptionsActivity(pageNumber: Int = 0, sortAsc: Boolean = false, sortBy: UoaSortByOptions): UnusualOptionsActivityPage?

    fun getLatestAnalystPriceTargetRatings(): AnalystPriceTargetRatings?

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


    suspend fun getUnusualOptionsActivityAsync(pageNumber: Int = 0, sortAsc: Boolean = false, sortBy: UoaSortByOptions): UnusualOptionsActivityPage?


    suspend fun getLatestAnalystPriceTargetRatingsAsync(): AnalystPriceTargetRatings?

}

//- OTM % = po
//- Strike = s
//- Type (call /put) = t
//- Expiry = expiry
//- Last Price = op
//- Volume = v
//- Open Int = oi
//- Vol/OI = vol/oi
//- IV = iv

enum class UoaSortByOptions(val key: String){
    Otm_Percentage(key = "po"),
    Strike(key = "s"),
    Type(key = "t"),
    Expiry("expiry"),
    LastPrice("op"),
    Volume("v"),
    OpenInt("oi"),
    VolOiRatio("vol/oi"),
    ImpVol("iv")
}