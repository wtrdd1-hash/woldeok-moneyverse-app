package com.example.woldeokmoneyverse.data.remote

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.woldeokmoneyverse.BuildConfig
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /** Debug is pinned to isolated test BFF; release is pinned to the production BFF. */
    val BASE_URL: String = BuildConfig.API_BASE_URL
    const val APP_VERSION: String = "1.0.19"
    private val ALLOWED_HOST: String = BuildConfig.API_HOST

    var csrfToken: String? = null
    private var debugNetworkLogging = false
    var cookieJar: PersistentCookieJar? = null
        private set

    private var okHttpClient: OkHttpClient = createOkHttpClient()
    private var retrofit: Retrofit = createRetrofit()

    var api: MoneyverseApi = retrofit.create(MoneyverseApi::class.java)
        private set

    fun init(context: Context) {
        debugNetworkLogging = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (cookieJar == null) {
            cookieJar = PersistentCookieJar(context.applicationContext)
            rebuildApi()
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (debugNetworkLogging) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            redactHeader("x-csrf-token")
            redactHeader("Authorization")
            redactHeader("x-play-integrity-token")
        }

        val builder = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        cookieJar?.let { builder.cookieJar(it) }

        builder.addInterceptor { chain ->
            chain.proceed(decorateRequest(chain.request()))
        }

        // Request IDs and Android client metadata are logged without secrets or bodies.
        builder.addInterceptor(ApiTelemetryInterceptor(debugNetworkLogging))
        builder.addInterceptor(ApiContractCompatibilityInterceptor())
        builder.addInterceptor(loggingInterceptor)

        return builder.build()
    }

    internal fun decorateRequest(original: Request): Request {
        require(original.url.isHttps && original.url.host == ALLOWED_HOST) {
            "Blocked unexpected API destination: ${original.url.host}"
        }
        val requestBuilder = original.newBuilder()
            .header("User-Agent", "WoldeokMoneyverse-Android/$APP_VERSION")
        if (original.header("Accept").isNullOrBlank()) {
            requestBuilder.header("Accept", "application/json")
        }
        if (original.method in setOf("POST", "PUT", "PATCH", "DELETE")) {
            csrfToken?.takeIf { it.isNotBlank() }?.let { token ->
                requestBuilder.header("x-csrf-token", token)
            }
        }
        return requestBuilder.build()
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
