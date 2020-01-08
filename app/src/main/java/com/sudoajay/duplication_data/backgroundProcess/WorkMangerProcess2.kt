package com.sudoajay.duplication_data.backgroundProcess

import android.content.Context
import android.os.Build
import android.view.View
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.databaseClasses.BackgroundTimerDataBase
import com.sudoajay.duplication_data.delete.DeleteDataUsingDoc
import com.sudoajay.duplication_data.delete.DeleteDataUsingFile
import com.sudoajay.duplication_data.duplicationData.ExpandableDuplicateListAdapter
import com.sudoajay.duplication_data.duplicationData.ScanDuplicateDataWithDoc
import com.sudoajay.duplication_data.duplicationData.ScanDuplicateDataWithFile
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate.Companion.convertIt
import com.sudoajay.duplication_data.notification.NotifyNotification
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService.Companion.nextDate
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WorkMangerProcess2(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        getWorkDone(applicationContext)
        return Result.success()
    }

    companion object {
        private var fileSize: Long = 0
        fun getWorkDone(context: Context) {
            var sdcardCheck = View.INVISIBLE
            var internalCheck = View.INVISIBLE
            val androidSdCardPermission = AndroidSdCardPermission(context)

            val listHeaderChild: LinkedHashMap<Int, List<String>>
            val deletedList: MutableList<String> = ArrayList()

            if (File(AndroidExternalStoragePermission.getExternalPath(context).toString()).exists())
                internalCheck = View.VISIBLE

            if (androidSdCardPermission.isSdStorageWritable) sdcardCheck = View.VISIBLE


            // Its supports till android 9 & api 28
            listHeaderChild = if (Build.VERSION.SDK_INT <= 22) {
                val scanDuplicateDataWithFile = ScanDuplicateDataWithFile(context)
                scanDuplicateDataWithFile.duplication(internalCheck, sdcardCheck)
                scanDuplicateDataWithFile.listHeaderChild
            } else {
                val scanDuplicateDataWithDoc = ScanDuplicateDataWithDoc(context)
                scanDuplicateDataWithDoc.duplication(internalCheck, sdcardCheck)
                scanDuplicateDataWithDoc.listHeaderChild
            }

            for (lists in listHeaderChild.values) {
                deletedList.addAll(lists)
                for (i in lists){
                    fileSize += ExpandableDuplicateListAdapter.getFileSizeInBytes(i, context)
                }
            }

            if (Build.VERSION.SDK_INT <= 22) {
                DeleteDataUsingFile(context, deletedList)
            } else {
                DeleteDataUsingDoc(context, deletedList)
            }


            if (fileSize != 0L) {
                val notifyNotification = NotifyNotification(context)
                notifyNotification.notify("You Have Saved " + convertIt(fileSize) +
                        " Of Data ", context.resources.getString(R.string.delete_Done_title))
                // this is just for backup plan
                getNextDate(context)
            }
        }


        private fun getNextDate(context: Context) {
            val backgroundTimerDataBase = BackgroundTimerDataBase(context)
            val traceBackgroundService = TraceBackgroundService(context)
            var hour = 0
            if (!backgroundTimerDataBase.checkForEmpty()) {
                val cursor = backgroundTimerDataBase.getTheChooseTypeRepeatedlyEndlessly()
                if (cursor.moveToFirst()) {
                    cursor.moveToFirst()
                    when (cursor.getInt(0)) {
                        0 -> hour = 12
                        1 -> hour = 24
                        2 ->  // At Every 2 Day
                            hour = 24 * 2
                        3 -> {
                            val calendar = Calendar.getInstance()
                            val currentDay = calendar[Calendar.DAY_OF_WEEK]
                            val weekdays = cursor.getString(1)
                            val splits = weekdays.split("").toTypedArray()
                            val listWeekdays: MutableList<Int> = ArrayList()
                            for (ints in splits) {
                                listWeekdays.add(ints.toInt())
                            }
                            hour = 24 * countDay(currentDay, listWeekdays)
                        }
                        4 -> hour = 24 * 30
                    }
                    if (hour != 0) { // set next date
                        traceBackgroundService.taskB = nextDate(hour)
                    }
                }
                try { // check for endlessly and delete the database
                    if (!cursor.getString(2).equals("No Date Fixed", ignoreCase = true)) {
                        val format: DateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                        val date = format.parse(cursor.getString(2))
                        val calendars = Calendar.getInstance()
                        val todayDate = calendars.time
                        if (date!!.before(todayDate) || format.format(todayDate) == format.format(date)) {
                            if (!backgroundTimerDataBase.checkForEmpty()) {
                                backgroundTimerDataBase.deleteData()
                            }
                            traceBackgroundService.taskB = "Empty"
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        }

        fun countDay(day: Int, week_Days: List<Int>): Int {
            var temp = day
            var count = 0
            do {
                count++
                temp++
                if (temp == 8) temp = 1
                for (week in week_Days) {
                    if (temp == week) return count
                }
            } while (temp != day)
            return 0
        }
    }
}