package com.willor.ktstockdata.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.coroutines.coroutineContext

internal object NetworkClient {

    private var client: OkHttpClient? = null


    /**
     * Returns the Singleton Instance of OkHttpclient
     */
    fun getClient(): OkHttpClient {
        if (client == null) {
            client = buildClient()
        }

        return client!!
    }


    /**
     * Attempts to retrieve a webpage using a GET request on the url
     */
    fun getWebpage(url: String): String? {

        val call = getClient().newCall(
            Request.Builder()
                .url(url)
                .header("user-agent", getRandomUserAgent())
                .get()
                .build()
        )

        val resp = call.execute()

        if (!resp.isSuccessful) {
            Log.w(
                "NETWORK", "getWebpage call failed for url: $url." +
                        " Response Code: ${resp.code}"
            )
            return null
        }

        return resp.body?.string()
    }


    /**
     * Attempts to retrieve webpage using async GET request on url
     */
    suspend fun getWebpageAsync(url: String): String? {

        val call = getClient().newCall(
            Request.Builder()
                .url(url)
                .header("user-agent", getRandomUserAgent())
                .get()
                .build()
        )

        // Launch async call
        val deferredResp = CoroutineScope(coroutineContext).async(Dispatchers.Unconfined) {
            call.execute()
        }

        // Await
        val resp = deferredResp.await()

        if (!resp.isSuccessful) {
            Log.w(
                "NETWORK", "getWebpageAsync call failed for url: $url." +
                        " Response Code: ${resp.code}"
            )
            return null
        }

        return resp.body?.string()
    }


    /**
     * Used to initially build the OkHttpClient. Only called once during program life.
     */
    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .followRedirects(true)      // VERY IMPORTANT FOR ANDROID
            .followSslRedirects(true)       // ALSO MAYBE
            .build()
    }
}

/** Creates the URL with query parameters added to the end of it (from Map<String,*>)
 *
 * Map of parameters is converted to a params string '?key=value&key=value'
 */
internal fun addParamsToUrl(urlString: String, params: Map<String, *>): String {

    var queryString = "?"

    for (k: String in params.keys) {
        queryString += "$k=${params[k].toString()}&"
    }

    return urlString + queryString.substring(0, queryString.length - 2)
}


/**
 * Returns one of 6 of the most used user-agents randomly
 */
internal fun getRandomUserAgent(): String {
    val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                " Chrome/58.0.3029.110 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0",

        "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0; Trident/5.0)",

        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0; MDDCJS)",

        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                " Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393",

        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"
    )

    return userAgents[(0..5).random()]
}

