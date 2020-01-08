package com.sudoajay.duplication_data.receiveBootCompleted

import android.app.IntentService
import android.content.Intent
import com.sudoajay.duplication_data.foregroundService.Foreground

class ForegroundServiceBoot : IntentService(null) {
    override fun onHandleIntent(intent: Intent?) {
        if (intent!!.action != null && intent.action.equals("RebootReceiver", ignoreCase = true)) {
            val startIntent = Intent(applicationContext, Foreground::class.java)
            startIntent.putExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"
                    , "Start_Foreground")
            startService(startIntent)
        }
        //Do reboot stuff
//handle other types of callers, like a notification.
    }
}