
<h1>Setup</h1>


Step 1.

```groovy
//  !! Add this to your settings.gradle repositories block under dependencyResolutionManagement!!
maven { url 'https://jitpack.io' }

// Example
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()

            maven { url 'https://jitpack.io' }
    }
}
```

Step 2.

```groovy
// Add the dependency
dependencies{
    implementation 'com.github.willor777:ktstocks-project:<...latest-version...>'
}
```


<h1>Usage</h1>

```kotlin

    // The wrapper class is a composite type (Classes can also be accessed individually)
    val ktStocks = KtStocks()

    
    // Market Data for quotes + general market data
    val etfQuote: EtfQuote? = ktStocks.marketData.getETFQuote("SPY")
    val stockQuote: StockQuote? = ktStocks.marketData.getStockQuote("AAPL")
    val futuresData: MajorFuturesData? = ktStocks.marketData.getFuturesData()

    
    // Watchlists for popular watchlist groups
    val chinaWatchlist: Watchlist? = ktStocks.watchlistData.getChinaTechAndInternet()
    val search: List<Watchlist>? = ktStocks.watchlistData.searchForByKeywords("Tech")

    
    // Historic data as simple chart (Just data + custom getters)
    val simpleChart: SimpleStockChart? = ktStocks.historicChartData.getHistoryAsSimpleStockChart("SPY")
    
    
    // getters accept negative indexing...example -1 is last item 
    val lastCandle: Candle? = simpleChart?.getCandleAtIndex(-1)
    
    
    // Historic data as advanced chart (Data + custom getters + calculation helpers)
    val advChart: AdvancedStockChart? = ktStocks.historicChartData.getHistoryAsAdvancedStockChart("SPY")
    val lastCandleSize: Double? = advChart?.getCandleHighToLowMeasurement(-1)
    
    
    // Get indicator data using chart values
    val ema = EMA(
        inputData = advChart!!.close,
        window = 10
    )
    
    val curEmaValue = ema.values[ema.lastIndex]

```