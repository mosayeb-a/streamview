package com.ma.streamview.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PlatformPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("stream_app", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    fun putInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int {
        return sharedPreferences.getInt(key, default)
    }

    fun putString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun putBool(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getBool(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }
}
