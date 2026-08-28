package com.smarola.core

import android.content.Context
import androidx.core.content.edit

class Preferences(context: Context) {
    private val values = context.applicationContext
        .getSharedPreferences("smarola_common", Context.MODE_PRIVATE)

    fun putString(key: String, value: String) = values.edit { putString(key, value) }
    fun getString(key: String, fallback: String = ""): String = values.getString(key, fallback) ?: fallback
    fun putBoolean(key: String, value: Boolean) = values.edit { putBoolean(key, value) }
    fun getBoolean(key: String, fallback: Boolean = false): Boolean = values.getBoolean(key, fallback)
    fun putInt(key: String, value: Int) = values.edit { putInt(key, value) }
    fun getInt(key: String, fallback: Int = 0): Int = values.getInt(key, fallback)
    fun remove(key: String) = values.edit { remove(key) }
    fun clear() = values.edit { clear() }
}
