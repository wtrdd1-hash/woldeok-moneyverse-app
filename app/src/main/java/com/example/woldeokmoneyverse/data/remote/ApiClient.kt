package com.example.woldeokmoneyverse.data.remote

import android.content.Context
import com.example.woldeokmoneyverse.data.model.ServerEndpointPreset
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    var selectedPreset: ServerEndpointPreset = ServerEndpointPreset.OFFICIAL
        set(value) {
            field = value
            baseUrl = value.url
            isMockModeEnabled = (value == ServerEndpointPreset.MOCK)
        }

    var baseUrl: String = "https://easy-scraping.com/"
        private set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            rebuildApi()
        }

    var isMockModeEnabled: Boolean = false

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
        // Redact sensitive headers to comply with security specification
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

        cookieJar?.let {
            builder.cookieJar(it)
        }

        builder.addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // The BFF contract requires CSRF only on state-changing requests.
            // Keeping it off reads also prevents exposure to public media URLs.
            if (chain.request().method in setOf("POST", "PUT", "PATCH", "DELETE")) {
                csrfToken?.takeIf { it.isNotBlank() }?.let { token ->
                    requestBuilder.header("x-csrf-token", token)
                }
            }

            chain.proceed(requestBuilder.build())
        }

        builder.addInterceptor(MockApiInterceptor())
        builder.addInterceptor(loggingInterceptor)

        return builder.build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
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
