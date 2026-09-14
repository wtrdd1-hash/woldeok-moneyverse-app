package com.example.woldeokmoneyverse.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val cookieStore = ConcurrentHashMap<String, Cookie>()

    init {
        loadPersistedCookies()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val editor = prefs.edit()
        for (cookie in cookies) {
            val key = cookieKey(cookie)
            if (cookie.expiresAt <= System.currentTimeMillis()) {
                cookieStore.remove(key)
                editor.remove(key)
            } else {
                cookieStore[key] = cookie
                editor.putString(key, encodeCookie(cookie))
            }
        }
        editor.apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val matchingCookies = mutableListOf<Cookie>()
        val expiredKeys = mutableListOf<String>()

        for ((key, cookie) in cookieStore) {
            if (cookie.expiresAt <= now) {
                expiredKeys.add(key)
            } else if (cookie.matches(url)) {
                matchingCookies.add(cookie)
            }
        }

        if (expiredKeys.isNotEmpty()) {
            val editor = prefs.edit()
            for (expiredKey in expiredKeys) {
                cookieStore.remove(expiredKey)
                editor.remove(expiredKey)
            }
            editor.apply()
        }

        return matchingCookies
    }

    @Synchronized
    fun clear() {
        cookieStore.clear()
        prefs.edit().clear().apply()
    }

    private fun loadPersistedCookies() {
        val allEntries = prefs.all
        val now = System.currentTimeMillis()
        val expiredKeys = mutableListOf<String>()

        for ((key, value) in allEntries) {
            if (value is String) {
                val cookie = decodeCookie(value)
                if (cookie != null) {
                    if (cookie.expiresAt <= now) {
                        expiredKeys.add(key)
                    } else {
                        cookieStore[key] = cookie
                    }
                }
            }
        }

        if (expiredKeys.isNotEmpty()) {
            val editor = prefs.edit()
            expiredKeys.forEach { editor.remove(it) }
            editor.apply()
        }
    }

    private fun cookieKey(cookie: Cookie): String {
        return "${cookie.domain}|${cookie.path}|${cookie.name}"
    }

    private fun encodeCookie(cookie: Cookie): String {
        return buildString {
            append(cookie.name).append(";")
            append(cookie.value).append(";")
            append(cookie.expiresAt).append(";")
            append(cookie.domain).append(";")
            append(cookie.path).append(";")
            append(cookie.secure).append(";")
            append(cookie.httpOnly).append(";")
            append(cookie.hostOnly)
        }
    }

    private fun decodeCookie(encoded: String): Cookie? {
        val parts = encoded.split(";")
        if (parts.size < 8) return null
        return try {
            val name = parts[0]
            val value = parts[1]
            val expiresAt = parts[2].toLong()
            val domain = parts[3]
            val path = parts[4]
            val secure = parts[5].toBoolean()
            val httpOnly = parts[6].toBoolean()
            val hostOnly = parts[7].toBoolean()

            val builder = Cookie.Builder()
                .name(name)
                .value(value)
                .expiresAt(expiresAt)
                .path(path)

            if (hostOnly) {
                builder.hostOnlyDomain(domain)
            } else {
                builder.domain(domain)
            }

            if (secure) builder.secure()
            if (httpOnly) builder.httpOnly()

            builder.build()
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val PREF_NAME = "woldeok_persistent_cookies_v2"
    }
}
