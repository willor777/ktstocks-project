package com.willor.ktstockdata.watchlistsdata


enum class PopularWatchlistOptions(
    val url: String,
) {
    GAINERS("https://finance.yahoo.com/gainers"),
    LOSERS("https://finance.yahoo.com/losers"),
    TECH_STOCKS_THAT_MOVE_THE_MARKET(
        "https://finance.yahoo.com/u/yahoo-finance/w" +
                "atchlists/tech-stocks-that-move-the-market"
    ),
    MOST_ACTIVE("https://finance.yahoo.com/most-active"),
    MOST_ACTIVE_PENNY_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                "ists/most-active-penny-stocks"
    ),
    MOST_ACTIVE_SMALL_CAP_STOCKS(
        "https://finance.yahoo.com/u/yahoo" +
                "-finance/watchlists/most-active-small-cap-stocks"
    ),
    MOST_BOUGHT_BY_ACTIVE_HEDGE_FUNDS(
        "https://finance.yahoo.com/u/yahoo-finance" +
                "/watchlists/most-bought-by-activist-hedge-funds"
    ),
    MOST_BOUGHT_BY_HEDGE_FUNDS(
        "https://finance.yahoo.com/u/yahoo" +
                "-finance/watchlists/most-bought-by-hedge-funds"
    ),
    MOST_SOLD_BY_ACTIVE_HEDGE_FUNDS(
        "https://finance.yahoo.com/u/yahoo-finance/watch" +
                "lists/most-sold-by-activist-hedge-funds"
    ),
    MOST_SOLD_BY_HEDGE_FUNDS(
        "https://finance.yahoo.com/u/yahoo-finance/watchlists/m" +
                "ost-sold-by-hedge-funds"
    ),
    MOST_NEWLY_ADDED_TO_WATCHLISTS(
        "https://finance.yahoo.com/u/yahoo-finance/w" +
                "atchlists/most-added"
    ),
    MOST_WATCHED_BY_RETAIL_TRADERS(
        "https://finance.yahoo.com/u/yaho" +
                "o-finance/watchlists/most-watched"
    ),
    LARGEST_FIFTY_TWO_WEEK_GAINS(
        "https://finance.yahoo.com/u/yahoo-fi" +
                "nance/watchlists/fiftytwo-wk-gain"
    ),
    LARGEST_FIFTY_TWO_WEEK_LOSSES(
        "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                "ists/fiftytwo-wk-loss"
    ),
    RECENT_FIFTY_TWO_WEEK_HIGHS(
        "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                "ists/fiftytwo-wk-high"
    ),
    RECENT_FIFTY_TWO_WEEK_LOWS(
        "https://finance.yahoo.com/u/yahoo" +
                "-finance/watchlists/fiftytwo-wk-low"
    ),
    BIG_EARNINGS_MISSES(
        "https://finance.yahoo.com/u/yahoo-finance/w" +
                "atchlists/earnings-miss"
    ),
    BIG_EARNINGS_BEATS(
        "https://finance.yahoo.com/u/yahoo-finance/" +
                "watchlists/earnings-beat"
    ),
    CHINA_TECH_AND_INTERNET_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance/watchlist" +
                "s/china-tech-and-internet-stocks"
    ),
    E_COMMERCE_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance/" +
                "watchlists/e-commerce-stocks"
    ),
    CANNABIS_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance/watchlist" +
                "s/420_stocks"
    ),
    SELF_DRIVING_CAR_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance" +
                "/watchlists/the-autonomous-car"
    ),
    VIDEO_GAME_DEVELOPER_STOCKS(
        "https://finance.yahoo.com/u/yahoo-f" +
                "inance/watchlists/video-game-stocks"
    ),
    BANKS_AND_FINANCIAL_SERVICES_STOCKS(
        "https://finance.yahoo.com/u/yahoo-fi" +
                "nance/watchlists/bank-and-financial-services-stocks"
    ),
    MEDICAL_DEVICE_AND_RESEARCH_STOCKS(
        "https://finance.yahoo." +
                "com/u/yahoo-finance/watchlists/medical-device-and-research-stocks"
    ),
    SMART_MONEY_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                "ists/smart-money-stocks"
    ),
    DIVIDEND_GROWTH_MARKET_LEADERS(
        "https://finance.yahoo.com/u/motley-fool/watchlists/div" +
                "idend-growth-market-leaders"
    ),
    BERKSHIRE_HATHAWAY_PORTFOLIO(
        "https://finance.yahoo.com/u/yahoo-finance/watchl" +
                "ists/the-berkshire-hathaway-portfolio"
    ),
    BIOTECH_AND_DRUG_STOCKS(
        "https://finance.yahoo.com/u/yahoo-finance/watchlists/biotech-and-drug-stocks"
    ),
}