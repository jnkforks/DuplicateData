package com.sudoajay.duplication_data.sharedPreferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.sudoajay.duplication_data.R

/**
 * Created by Lincoln on 05/05/16.
 */
class BackgroundProcess @SuppressLint("CommitPrefEdits") constructor(private val _context: Context) {
    private val editor: SharedPreferences.Editor
    fun setTaskCDone(task: Boolean) {
        editor.putBoolean(_context.getString(R.string.task_C_Done), task)
        editor.apply()
    }

    init {
        // shared pref mode
        val p1 = 0
        val pref = _context.getSharedPreferences(_context.getString(R.string.MY_PREFS_NAME), p1)
        editor = pref.edit()
    }
}