package com.sudoajay.duplication_data.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import com.sudoajay.duplication_data.R

class PrefManager constructor(private val _context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(_context.getString(R.string.isFirstTimeLaunch), true)
        set(isFirstTime) {
            editor.putBoolean(_context.getString(R.string.isFirstTimeLaunch), isFirstTime)
            editor.apply()
        }

    init {
        // shared pref mode
        val p1 = 0
        pref = _context.getSharedPreferences(_context.getString(R.string.MY_PREFS_NAME), p1)
        editor = pref.edit()
        editor.apply()
    }
}