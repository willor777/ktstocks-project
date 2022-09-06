package com.willor.ktstockdata.marketdata

import com.willor.ktstockdata.marketdata.dataobjects.MajorFuturesData
import com.willor.ktstockdata.marketdata.dataobjects.MajorIndicesData
import com.willor.ktstockdata.marketdata.dataobjects.SnRLevels
import com.willor.ktstockdata.marketdata.dataobjects.EtfQuote
import com.willor.ktstockdata.marketdata.dataobjects.OptionStats
import com.willor.ktstockdata.marketdata.dataobjects.StockCompetitorsList
import com.willor.ktstockdata.marketdata.dataobjects.StockQuote

interface IMarketData {


    fun getSupportAndResistanceFromBarchartDotCom(ticker: String): SnRLevels?


    fun getFuturesData(): MajorFuturesData?


    fun getMajorIndicesData(): MajorIndicesData?

    fun getStockQuote(ticker: String): StockQuote?

    fun getETFQuote(ticker: String): EtfQuote?

    fun getOptionStats(ticker: String): OptionStats?

    fun getStocksCompetitors(ticker: String): StockCompetitorsList?
}
