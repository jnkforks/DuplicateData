package com.sudoajay.duplication_data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate


class ScanningService : Service() {


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show()


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show()
    }

    private class MultiThreadingTask1
    internal constructor(private val showDuplicate: ShowDuplicate) : AsyncTask<String?, String?, String?>() {

        private var internalCheck = 0
        private var externalCheck = 0
        override fun onPreExecute() {
            internalCheck = showDuplicate.internalCheck
            externalCheck = showDuplicate.externalCheck
            showDuplicate.loadingAnimation!!.start()
            super.onPreExecute()
        }

        @SuppressLint("ObsoleteSdkInt")
        override fun doInBackground(vararg p0: String?): String? {
            //             Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= showDuplicate.getString(R.string.apiLevel).toInt()) {
                showDuplicate.scanDuplicateDataWithFile!!.duplication(internalCheck, externalCheck)
            } else {
                showDuplicate.scanDuplicateDataWithDoc!!.duplication(internalCheck, externalCheck)
            }
            return null
        }

        override fun onPostExecute(s: String?) {

            showDuplicate.valueTransfer()

            showDuplicate.afterLoading()

            super.onPostExecute(s)
        }

    }

}