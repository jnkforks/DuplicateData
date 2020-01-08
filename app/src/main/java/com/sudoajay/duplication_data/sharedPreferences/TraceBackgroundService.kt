package com.sudoajay.duplication_data.sharedPreferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.sudoajay.duplication_data.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Lincoln on 05/05/16.
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TraceBackgroundService @SuppressLint("CommitPrefEdits")
constructor(private val _context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor
    val taskA: String?
        get() = pref.getString(_context.getString(R.string.task_A_NextDate), nextDate(24))

    fun setTaskA() {
        editor.putString(_context.getString(R.string.task_A_NextDate), nextDate(24))
        editor.apply()
    }

    var taskB: String?
        get() = pref.getString(_context.getString(R.string.task_B_NextDate), "")
        set(taskB) {
            editor.putString(_context.getString(R.string.task_B_NextDate), taskB)
            editor.apply()
        }

    var isBackgroundServiceWorking: Boolean
        get() = pref.getBoolean(_context.getString(R.string.background_Service_Working), true)
        private set(backgroundServiceWorking) {
            editor.putBoolean(_context.getString(R.string.background_Service_Working), backgroundServiceWorking)
            editor.apply()
        }

    var isForegroundServiceWorking: Boolean
        get() = pref.getBoolean(_context.getString(R.string.foreground_Service_Working), true)
        set(foregroundServiceWorking) {
            editor.putBoolean(_context.getString(R.string.foreground_Service_Working), foregroundServiceWorking)
            editor.apply()
        }

    // today date
    val isBackgroundWorking:
    // juts add this for Yesterday
            Unit
        get() { // today date
            val calendar = Calendar.getInstance()
            // juts add this for Yesterday
            calendar.add(Calendar.DATE, -1)
            val yesterday = calendar.time
            @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            isBackgroundServiceWorking = try {
                if (taskB != "" || taskB != "Empty" || taskB != null) {
                    val getDate = dateFormat.parse(taskB)
                    !yesterday.after(getDate)
                } else {
                    true
                }
            } catch (e: Exception) {
                true
            }
        }

    companion object {
        @JvmStatic
        fun nextDate(hour: Int): String { // get Today Date as default
            @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR, hour)
            dateFormat.timeZone = calendar.timeZone
            return dateFormat.format(calendar.time)
        }
    }

    init {
        // shared pref mode
        val p1 = 0
        pref = _context.getSharedPreferences(_context.getString(R.string.MY_PREFS_NAME), p1)
        editor = pref.edit()
        // set default value
        if (!pref.contains(_context.getString(R.string.task_A_NextDate))) editor.putString(_context.getString(R.string.task_A_NextDate), nextDate(24))
        editor.apply()
    }
}