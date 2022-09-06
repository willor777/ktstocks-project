package com.willor.ktstockdata.common

import okhttp3.OkHttpClient
import okhttp3.Request

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
    fun getWebpage(url: String): String?{

        val call = getClient().newCall(
            Request.Builder()
                .url(url)
                .header("user-agent", getRandomUserAgent())
                .get()
                .build()
        )

        val resp = call.execute()

        if (!resp.isSuccessful){
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
