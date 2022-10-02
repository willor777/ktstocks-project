package com.willor.ktstockdata.watchlistsdata

import com.willor.ktstockdata.watchlistsdata.dataobjects.Watchlist

interface PopularWatchlistData {

    fun getPopularWatchlist(wl: PopularWatchlistOptions): Watchlist?

    suspend fun getPopularWatchlistAsync(wl: PopularWatchlistOptions): Watchlist?

    fun getPopularWatchlistsByKeywords(vararg keywords: String): List<Watchlist>?

    suspend fun getPopularWatchlistsByKeywordsAsync(vararg keywords: String): List<Watchlist>?

    fun getAllPopularWatchlistOptions(): List<PopularWatchlistOptions>
}