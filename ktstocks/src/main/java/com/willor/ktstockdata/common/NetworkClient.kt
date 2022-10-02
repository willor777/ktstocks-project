package com.willor.ktstockdata.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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


    // TODO Maybe make an async version of this... or do it in each of the classes
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
            Log.d("NETWORK", "getWebpage call failed for url: $url." +
                    " Response Code: ${resp.code}")
            return null
        }

        return resp.body?.string()
    }


    /**
     * Attempts to retrieve webpage using async GET request on url
     */
    suspend fun getWebpageAsync(url: String): String?{

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
            Log.d("NETWORK", "getWebpageAsync call failed for url: $url." +
                    " Response Code: ${resp.code}")
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
