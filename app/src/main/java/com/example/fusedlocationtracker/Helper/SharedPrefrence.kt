package com.example.fusedlocationtracker.Helper

import android.content.Context
import android.content.SharedPreferences

class SharedPrefrence {

    companion object {
        private var sharedPrefFile = "kotlinsharedpreference"
        private var instance: SharedPrefrence? = null
        private var preferences: SharedPreferences? = null
        open fun getInstance(context: Context): SharedPrefrence {
            if (instance == null) {
                instance = SharedPrefrence()
            }
            preferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            return instance as SharedPrefrence
        }
    }

    fun saveString(key: String, string: String) {
        preferences?.edit()?.putString(key, string)?.apply()
    }

    fun getString(key: String): String {
        return preferences?.getString(key, "") ?: ""
    }
    fun getJsonString(key: String): String {
        return preferences?.getString(key, "{ }") ?: "{ }"
    }

    fun saveInt(key: String, int: Int) {
        preferences?.edit()?.putInt(key, int)?.apply()
    }

    fun getInt(key: String): Int {
        return preferences?.getInt(key, 0) ?: 0
    }

    fun saveLong(key: String, int: Long) {
        preferences?.edit()?.putLong(key, int)?.apply()
    }

    fun getLong(key: String): Long {
        return preferences?.getLong(key, 0L) ?: 0L
    }

    fun saveFloat(key: String, float: Float) {
        preferences?.edit()?.putFloat(key, float)?.apply()
    }

    fun getFloat(key: String): Float {
        return preferences?.getFloat(key, 0F) ?: 0F
    }

    fun saveBoolean(key: String, boolean: Boolean) {
        preferences?.edit()?.putBoolean(key, boolean)?.apply()
    }

    fun getBoolean(key: String): Boolean {
        return preferences?.getBoolean(key, false) ?: false
    }

    fun clearData() {
        preferences?.edit()?.clear()?.apply()
    }

    fun clearKeyData(key : String) {
        preferences?.edit()?.remove(key)?.apply()
    }



}
