package com.willor.ktstockdata.marketdata.dataobjects

data class MajorFuturesData(
    val sp500Future: Future? = null,
    val dowFuture: Future? = null,
    val nasdaqFuture: Future? = null,
    val russel2000Future: Future? = null,
    val usTreasuryBondFuture: Future? = null,
    val usTenYearTreasuryNoteFuture: Future? = null,
    val usFiveYearTreasuryNoteFuture: Future? = null,
    val usTwoYearTreasureNoteFuture: Future? = null,
    val goldFuture: Future? = null,
    val goldMicroFuture: Future? = null,
    val silverFuture: Future? = null,
    val microSilverFuture: Future? = null,
    val crudeOilWTIFuture: Future? = null,
    val rbobGasolineFuture: Future? = null,
    val brentCrudeOilFuture: Future? = null,
)
