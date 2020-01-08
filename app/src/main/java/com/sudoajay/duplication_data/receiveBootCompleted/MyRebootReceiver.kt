package com.sudoajay.duplication_data.receiveBootCompleted

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService

class MyRebootReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val traceBackgroundService = TraceBackgroundService(context)
        if (!traceBackgroundService.isBackgroundServiceWorking) {
            val serviceIntent = Intent(context, ForegroundServiceBoot::class.java)
            serviceIntent.action = "RebootReceiver"
            context.startService(serviceIntent)
        }
    }
}