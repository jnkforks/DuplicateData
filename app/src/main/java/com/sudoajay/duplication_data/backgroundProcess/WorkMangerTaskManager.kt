package com.sudoajay.duplication_data.backgroundProcess

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WorkMangerTaskManager(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val list: MutableList<OneTimeWorkRequest> = ArrayList()
    override fun doWork(): Result {
        val everyDayWork = OneTimeWorkRequest.Builder(WorkMangerProcess1::class.java).addTag("Regular Duplicate Size").setInitialDelay(20
                , TimeUnit.MINUTES).build()
        val onceAWeekWork = OneTimeWorkRequest.Builder(WorkMangerProcess2::class.java).addTag("Background Delete Duplicate").setInitialDelay(1
                , TimeUnit.HOURS).build()
        val traceBackgroundService = TraceBackgroundService(applicationContext)
        traceBackgroundService.setTaskA()
        val calendars = Calendar.getInstance()
        val todayDate = calendars.time
        // specific date from database
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        // Check for Date A Task
        var date: Date
        try {
            date = dateFormat.parse(traceBackgroundService.taskA.toString())!!
            if (dateFormat.format(todayDate) == dateFormat.format(date) || date.before(todayDate)) {
                list.add(everyDayWork)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Check for Date B Task
        return try {
            date = dateFormat.parse(traceBackgroundService.taskB.toString())!!
            if (dateFormat.format(todayDate) == dateFormat.format(date) || date.before(todayDate)) {
                list.add(onceAWeekWork)
            }

            WorkManager.getInstance(applicationContext)
                    .beginWith(list)
                    .enqueue()
            Result.success()
        } catch (ignored: java.lang.Exception) {
            Result.retry()
        }
    }
}