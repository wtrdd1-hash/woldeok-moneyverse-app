package com.example.woldeokmoneyverse.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SessionManager {

    private const val PREF_NAME = "woldeok_session_prefs_v2"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_SESSION_COOKIE = "session_cookie"
    private const val KEY_CSRF_TOKEN = "csrf_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_TITLE = "user_title"
    private const val KEY_LEVEL = "user_level"
    private const val KEY_TERMS_AGREED = "terms_agreed"
    private const val KEY_TERMS_VERSION = "terms_version"
    private const val KEY_PRIVACY_VERSION = "privacy_version"

    private fun getPrefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        return EncryptedSharedPreferences.create(
            appContext,
            PREF_NAME,
            MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveSession(
        context: Context,
        sessionCookie: String?,
        csrfToken: String?,
        userId: String?,
        displayName: String?,
        email: String?,
        title: String? = null,
        level: Int = 1
    ) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_SESSION_COOKIE, sessionCookie)
            putString(KEY_CSRF_TOKEN, csrfToken)
            putString(KEY_USER_ID, userId)
            putString(KEY_DISPLAY_NAME, displayName)
            putString(KEY_EMAIL, email)
            putString(KEY_TITLE, title)
            putInt(KEY_LEVEL, level)
            apply()
        }
    }

    fun updateProfileInfo(
        context: Context,
        displayName: String?,
        title: String?,
        level: Int,
        email: String?
    ) {
        getPrefs(context).edit().apply {
            if (!displayName.isNullOrBlank()) putString(KEY_DISPLAY_NAME, displayName)
            if (!title.isNullOrBlank()) putString(KEY_TITLE, title)
            if (!email.isNullOrBlank()) putString(KEY_EMAIL, email)
            putInt(KEY_LEVEL, level)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSessionCookie(context: Context): String? {
        return getPrefs(context).getString(KEY_SESSION_COOKIE, null)
    }

    fun getCsrfToken(context: Context): String? {
        return getPrefs(context).getString(KEY_CSRF_TOKEN, null)
    }

    fun getDisplayName(context: Context): String {
        return getPrefs(context).getString(KEY_DISPLAY_NAME, null) ?: "월덕 회원"
    }

    fun getTitle(context: Context): String {
        return getPrefs(context).getString(KEY_TITLE, null) ?: "머니버서 패스트트랙"
    }

    fun getLevel(context: Context): Int {
        return getPrefs(context).getInt(KEY_LEVEL, 1)
    }

    fun getEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, null) ?: "user@woldeok.com"
    }

    fun isTermsAgreed(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TERMS_AGREED, false)
    }

    fun saveTermsAgreed(context: Context, agreed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TERMS_AGREED, agreed).apply()
    }

    fun hasAcceptedPolicyVersions(
        context: Context,
        termsVersion: String,
        privacyVersion: String
    ): Boolean =
        getPrefs(context).getString(KEY_TERMS_VERSION, null) == termsVersion &&
            getPrefs(context).getString(KEY_PRIVACY_VERSION, null) == privacyVersion

    fun saveAcceptedPolicyVersions(
        context: Context,
        termsVersion: String,
        privacyVersion: String
    ) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_TERMS_AGREED, true)
            putString(KEY_TERMS_VERSION, termsVersion)
            putString(KEY_PRIVACY_VERSION, privacyVersion)
            apply()
        }
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
