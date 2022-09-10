package com.willor.ktstockdata.marketdata

import com.willor.ktstockdata.marketdata.dataobjects.*

interface IMarketData {


    fun getSupportAndResistanceFromBarchartDotCom(ticker: String): SnRLevels?

    fun getFuturesData(): MajorFuturesData?

    fun getMajorIndicesData(): MajorIndicesData?

    fun getStockQuote(ticker: String): StockQuote?

    fun getETFQuote(ticker: String): EtfQuote?

    fun getOptionStats(ticker: String): OptionStats?

    fun getStocksCompetitors(ticker: String): StockCompetitorsList?

    fun getUnusualOptionsActivity(pageNumber: Int): UnusualOptionsActivityPage?
}
