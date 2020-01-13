package com.sudoajay.duplication_data.backgroundProcess

import android.content.Context
import android.os.Build
import android.view.View
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.duplicationData.ExpandableDuplicateListAdapter
import com.sudoajay.duplication_data.duplicationData.ScanDuplicateDataWithDoc
import com.sudoajay.duplication_data.duplicationData.ScanDuplicateDataWithFile
import com.sudoajay.duplication_data.helperClass.FileSize
import com.sudoajay.duplication_data.notification.NotifyNotification
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService
import java.io.File
import java.util.*

class WorkMangerProcess1(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        getWorkDone(applicationContext)
        return Result.success()
    }

    companion object {
        fun getWorkDone(context: Context) { // local variable
            var sdcardCheck = View.INVISIBLE
            var internalCheck = View.INVISIBLE
            var fileSize: Long = 0
            val textPass: String
            val listHeaderChild: LinkedHashMap<Int, List<String>>
            val androidSdCardPermission = AndroidSdCardPermission(context)


            if (File(AndroidExternalStoragePermission.getExternalPath(context).toString()).exists())
                internalCheck = View.VISIBLE

            if (androidSdCardPermission.isSdStorageWritable) sdcardCheck = View.VISIBLE


            //  Here use of DocumentFile in android 10 not File is using anymore
            listHeaderChild = if (Build.VERSION.SDK_INT <= 29) {
                val scanDuplicateDataWithFile = ScanDuplicateDataWithFile(context)
                scanDuplicateDataWithFile.duplication(internalCheck, sdcardCheck)
                scanDuplicateDataWithFile.listHeaderChild

            }else{
                val scanDuplicateDataWithDoc = ScanDuplicateDataWithDoc(context)
                scanDuplicateDataWithDoc.duplication(internalCheck, sdcardCheck)
                scanDuplicateDataWithDoc.listHeaderChild

            }

            for (lists in listHeaderChild.values) {
                for (i in lists){
                    fileSize += ExpandableDuplicateListAdapter.getFileSizeInBytes(i, context)
                }
            }

            if (fileSize != 0L) {
                textPass = "We Have Found " + FileSize.convertIt(fileSize) + " Of unnecessary Files"
                val notifyNotification = NotifyNotification(context)
                notifyNotification.notify(textPass, context.getString(R.string.file_found_title))
            }
            val traceBackgroundService = TraceBackgroundService(context)
            // set next date
            traceBackgroundService.setTaskA()
        }
    }
}