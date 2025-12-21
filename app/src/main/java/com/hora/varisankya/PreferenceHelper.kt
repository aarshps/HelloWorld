package com.hora.varisankya

import android.content.Context

object PreferenceHelper {

    private const val PREFS_NAME = "DropdownPrefs"

    fun recordUsage(context: Context, preferenceKey: String, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("${preferenceKey}_${value}", 0)
        prefs.edit().putInt("${preferenceKey}_${value}", currentCount + 1).apply()
    }

    fun getPersonalizedList(context: Context, preferenceKey: String, defaultList: Array<String>): Array<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        val usageCounts = defaultList.map { it to prefs.getInt("${preferenceKey}_${it}", 0) }
        
        val sortedList = usageCounts.sortedByDescending { it.second }.map { it.first }
        
        return sortedList.toTypedArray()
    }
}