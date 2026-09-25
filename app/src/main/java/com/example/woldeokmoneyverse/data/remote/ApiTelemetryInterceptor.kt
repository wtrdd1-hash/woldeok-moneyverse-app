package com.example.woldeokmoneyverse.data.remote

import android.os.Build
import android.os.SystemClock
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.UUID

/**
 * Native API diagnostic boundary. Request and response bodies, cookies,
 * credentials, CSRF values and integrity tokens are never logged.
 */
class ApiTelemetryInterceptor(private val verbose: Boolean = false) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestId = original.header("x-request-id") ?: UUID.randomUUID().toString()
        val request = original.newBuilder()
            .header("x-request-id", requestId)
            .header("x-moneyverse-client", "android")
            .header("x-moneyverse-app-version", "1.0.18")
            .header("x-moneyverse-android-sdk", Build.VERSION.SDK_INT.toString())
            .build()

        val started = SystemClock.elapsedRealtime()
        if (verbose) {
            Log.i(
                TAG,
                "api.request id=$requestId method=${request.method} sdk=${Build.VERSION.SDK_INT}"
            )
        }

        return try {
            val response = chain.proceed(request)
            val elapsed = SystemClock.elapsedRealtime() - started
            if (verbose || response.code >= 400) {
                Log.i(
                    TAG,
                    "api.response id=$requestId status=${response.code} ms=$elapsed " +
                        "type=${response.header("content-type") ?: "-"} " +
                        "serverRequestId=${response.header("x-request-id") ?: "-"} " +
                        "apiVersion=${response.header("x-moneyverse-api-version") ?: "-"} " +
                        "contract=${response.header("x-moneyverse-contract-version") ?: "-"}"
                )
            }
            response
        } catch (error: IOException) {
            val elapsed = SystemClock.elapsedRealtime() - started
            Log.e(
                TAG,
                "api.failure id=$requestId ms=$elapsed type=${error.javaClass.simpleName}"
            )
            throw error
        }
    }

    private companion object {
        const val TAG = "MoneyverseApi"
    }
}
