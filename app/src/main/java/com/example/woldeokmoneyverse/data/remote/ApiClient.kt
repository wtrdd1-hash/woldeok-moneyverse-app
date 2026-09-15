package com.example.woldeokmoneyverse.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /** Production builds are pinned to the official BFF. Users cannot switch API origins. */
    const val BASE_URL: String = "https://easy-scraping.com/"

    var csrfToken: String? = null
    var cookieJar: PersistentCookieJar? = null
        private set

    private var okHttpClient: OkHttpClient = createOkHttpClient()
    private var retrofit: Retrofit = createRetrofit()

    var api: MoneyverseApi = retrofit.create(MoneyverseApi::class.java)
        private set

    fun init(context: Context) {
        if (cookieJar == null) {
            cookieJar = PersistentCookieJar(context.applicationContext)
            rebuildApi()
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            redactHeader("x-csrf-token")
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        cookieJar?.let { builder.cookieJar(it) }

        builder.addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", "WoldeokMoneyverse-Android/1.0.9")

            if (chain.request().method in setOf("POST", "PUT", "PATCH", "DELETE")) {
                csrfToken?.takeIf { it.isNotBlank() }?.let { token ->
                    requestBuilder.header("x-csrf-token", token)
                }
            }

            chain.proceed(requestBuilder.build())
        }

        // Normalize the legacy Android DTO boundary to the canonical BFF contract.
        builder.addInterceptor(ApiTelemetryInterceptor())
        builder.addInterceptor(ApiContractCompatibilityInterceptor())
        builder.addInterceptor(loggingInterceptor)

        return builder.build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun rebuildApi() {
        okHttpClient = createOkHttpClient()
        retrofit = createRetrofit()
        api = retrofit.create(MoneyverseApi::class.java)
    }

    fun clearSession() {
        csrfToken = null
        cookieJar?.clear()
    }
}
