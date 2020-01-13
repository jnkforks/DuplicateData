package com.sudoajay.duplication_data.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.sudoajay.duplication_data.R

class ExternalPathSharedPreference constructor(context: Context) {
    // global varibale
    private val editor: Editor
    private val context: Context
    private val pref: SharedPreferences = context.getSharedPreferences(context.getString(R.string.MY_PREFS_NAME), Context.MODE_PRIVATE)
    // send thd data to shared preferences
    var externalPath: String?
        get() = pref.getString(context.getString(R.string.externalPath), "")
        set(sdCardPath) { // send thd data to shared preferences
            editor.putString(context.getString(R.string.externalPath), sdCardPath)
            editor.apply()
        }

    var stringURI: String?
        get() = pref.getString(context.getString(R.string.externalStringUri), "")
        set(stringURI) {
            editor.putString(context.getString(R.string.externalStringUri), stringURI)
            editor.apply()
        }
    // constructor
    init {
        editor = pref.edit()
        editor.apply()
        this.context = context
    }
}