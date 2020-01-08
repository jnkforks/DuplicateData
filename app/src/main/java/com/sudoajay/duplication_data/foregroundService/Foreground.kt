package com.sudoajay.duplication_data.foregroundService

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sudoajay.duplication_data.backgroundProcess.WorkMangerProcess1
import com.sudoajay.duplication_data.backgroundProcess.WorkMangerProcess2
import com.sudoajay.duplication_data.databaseClasses.BackgroundTimerDataBase
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService.Companion.nextDate
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Foreground : Service() {
    private var traceBackgroundService: TraceBackgroundService? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int { // create object
        traceBackgroundService = TraceBackgroundService(applicationContext)
        if (Objects.requireNonNull(intent.getStringExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"))
                        .equals("Start_Foreground", ignoreCase = true)) {
            createNotificationChannel()
            val url = "https://dontkillmyapp.com/problem"
            val knowMoreIntent = Intent(Intent.ACTION_VIEW)
            knowMoreIntent.data = Uri.parse(url)
            val stopIntent = Intent(applicationContext, MainActivity::class.java)
            stopIntent.action = "Stop_Foreground(Setting)"
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setDefaults(Notification.DEFAULT_ALL) // Set required fields, including the small icon, the
// notification title, and text.
                    .setContentTitle("Foreground Service")
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(longArrayOf(0L)) // All fields below this line are optional.
// Use a default priority (recognized on devices running Android
// 4.1 or later)
                    .setPriority(NotificationCompat.PRIORITY_MAX) //     .setSound(uri)
// Provide a large icon, shown with the notification in the
// notification drawer on devices running Android 3.0 or later.
// Show a number. This is useful when stacking notifications of
// a single type.
                    .setNumber(1) // If this notification relates to a past or upcoming event, you
// should set the relevant time information using the setWhen
// method below. If this call is omitted, the notification's
// timestamp will by set to the time at which it was shown.
// TODO: Call setWhen if this notification relates to a past or
// upcoming event. The sole argument to this method should be
// the notification timestamp in milliseconds.
//.setWhen(...)
                    .setSmallIcon(R.drawable.scan_icon) // Set the pending intent to be initiated when the user touches
// the notification.
                    .addAction(R.drawable.know_more_icon,
                            this.getString(R.string.why_This_Foreground_Service),
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    knowMoreIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.stop_icon,
                            this.getString(R.string.stop_Foreground_Service),
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    stopIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT))
                    .setOnlyAlertOnce(true) // Show an expanded list of items on devices running Android 4.1
// or later.
// Example additional actions for this notification. These will
// only show on devices running Android 4.1 or later, so you
// should ensure that the activity in this notification's
// content intent provides access to the same actions in
// another way.
// Automatically dismiss the notification when it is touched.
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(
                            this,
                            0,
                            Intent(this, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT))
            startForeground(1337, notification.build())
            // check if date matches then run the process
//            // first Process or Task A
            if (datesMatches(traceBackgroundService!!.taskA, 1)) WorkMangerProcess1.getWorkDone(applicationContext)
            // Second Process or Task B
            if (datesMatches(traceBackgroundService!!.taskB, 2)) WorkMangerProcess2.getWorkDone(applicationContext)
            task()
        } else if (Objects.requireNonNull(intent.getStringExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"))
                        .equals("Stop_Foreground", ignoreCase = true)) { //your  service end here
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun datesMatches(date: String?, type: Int): Boolean {
        return try { // set The Today Date
            val todayCalender = Calendar.getInstance()
            val todayDate = todayCalender.time
            // convert to Date
            val format: DateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val curDate = format.parse(date)
            if (todayDate.after(curDate)) {
                if (format.format(todayDate) != format.format(curDate)) {
                    if (type == 1) {
                        traceBackgroundService!!.setTaskA()
                    } else {
                        traceBackgroundService!!.taskB = nextDate(getHours(applicationContext))
                    }
                }
            }
            format.format(todayDate) == format.format(curDate)
        } catch (e: ParseException) {
            false
        }
    }

    private fun task() {
        val startIntent = Intent(applicationContext, Foreground::class.java)
        startIntent.putExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"
                , "Start_Foreground")
        val pintent = PendingIntent.getService(applicationContext, 0, startIntent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        startAlarm(alarmManager, pintent)
    }

    private fun startAlarm(alarmManager: AlarmManager, pendingIntent: PendingIntent) {
        val setTime = 3600000 * 3.toLong() // 3 hours
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + setTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + setTime, pendingIntent)
        }
    }

    companion object {
        const val CHANNEL_ID = "Foreground Service"
        fun getHours(context: Context?): Int {
            val backgroundTimerDataBase = BackgroundTimerDataBase(context)
            // set the Task is started
// this task for cleaning and show today task
            var hour = 0
            // grab the data From Database
            if (!backgroundTimerDataBase.checkForEmpty()) {
                val cursor = backgroundTimerDataBase.getTheRepeatedlyWeekdays()
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.moveToFirst()
                    try {
                        when (cursor.getInt(0)) {
                            0 -> hour = 12
                            1 -> hour = 24
                            2 ->  // At Every 2 Day
                                hour = 24 * 2
                            3 -> {
                                val calendar = Calendar.getInstance()
                                val currentDay = calendar[Calendar.DAY_OF_WEEK]
                                val weekdays = cursor.getString(1)
                                val listWeekdays: MutableList<Int> = ArrayList()
                                var i = 0
                                while (i < weekdays.length) {
                                    listWeekdays.add(Character.getNumericValue(weekdays[i]))
                                    i++
                                }
                                hour = 24 * WorkMangerProcess2.countDay(currentDay, listWeekdays)
                            }
                            4 -> hour = 24 * 30
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }
            return hour
        }
    }
}