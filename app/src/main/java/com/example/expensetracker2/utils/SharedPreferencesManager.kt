package com.example.expensetracker2.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val PREF_ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at"
    private const val PREF_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun storeAccessToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_ACCESS_TOKEN, token)
        editor.apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun storeRefreshToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_REFRESH_TOKEN, token)
        editor.apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun storeAccessTokenExpiresAt(expiration: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_ACCESS_TOKEN_EXPIRES_AT, expiration)
        editor.apply()
    }

    fun getAccessTokenExpiresAt(): String? {
        return sharedPreferences.getString(PREF_ACCESS_TOKEN_EXPIRES_AT, null)
    }

    fun storeRefreshTokenExpiresAt(expiration: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_REFRESH_TOKEN_EXPIRES_AT, expiration)
        editor.apply()
    }

    fun getRefreshTokenExpiresAt(): String? {
        return sharedPreferences.getString(PREF_REFRESH_TOKEN_EXPIRES_AT, null)
    }

    fun clearSessionData() {
        val editor = sharedPreferences.edit()

        // Remove all keys related to session
        editor.remove(KEY_ACCESS_TOKEN)
        editor.remove(KEY_REFRESH_TOKEN)
        editor.remove(PREF_ACCESS_TOKEN_EXPIRES_AT)
        editor.remove(PREF_REFRESH_TOKEN_EXPIRES_AT)

        // Apply changes
        editor.apply()
    }
}