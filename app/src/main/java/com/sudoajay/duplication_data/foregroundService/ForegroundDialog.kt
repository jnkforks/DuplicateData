package com.sudoajay.duplication_data.foregroundService

import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.widget.Button
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService

class ForegroundDialog // constructor
(private val mContext: Context, private val activity: Activity) {
    private var traceBackgroundService: TraceBackgroundService? = null
    fun callThread() {
        val handler = Handler()
        handler.postDelayed({ callCustomPermissionDailog() }, 500)
    }

    private fun callCustomPermissionDailog() {
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_custom_foreground_permission)
        val buttonLearnMore = dialog.findViewById<Button>(R.id.see_More_button)
        val buttonContinue = dialog.findViewById<Button>(R.id.ok_Button)
        // if button is clicked, close the custom dialog
        buttonLearnMore.setOnClickListener {
            try {
                val url = "https://dontkillmyapp.com/problem?1"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                activity.startActivity(i)
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        buttonContinue.setOnClickListener {
            try {
                val url = getUrl()
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                activity.startActivity(i)
                traceBackgroundService = TraceBackgroundService(mContext)
                traceBackgroundService!!.isForegroundServiceWorking = true
                if (!isServiceRunningInForeground(mContext)) { // call Foreground Thread();
                    val startIntent = Intent(mContext, Foreground::class.java)
                    startIntent.putExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"
                            , "Start_Foreground")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity.startForegroundService(startIntent)
                    } else {
                        activity.startService(startIntent)
                    }
                }
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getUrl(): String {
        return when (Build.MANUFACTURER) {
            "Xiaomi" -> "https://dontkillmyapp.com/xiaomi"
            "Nokia" -> "https://dontkillmyapp.com/nokia"
            "OnePlus" -> "https://dontkillmyapp.com/oneplus"
            "Huawei" -> "https://dontkillmyapp.com/huawei"
            "Meizu" -> "https://dontkillmyapp.com/meizu"
            "Samsung" -> "https://dontkillmyapp.com/samsung"
            "Sony" -> "https://dontkillmyapp.com/sony"
            "HTC" -> "https://dontkillmyapp.com/htc"
            "Google" -> "https://dontkillmyapp.com/stock_android"
            "Lenovo" -> "https://dontkillmyapp.com/lenovo"
            else -> "https://dontkillmyapp.com/"
        }
    }

    private fun isServiceRunningInForeground(context: Context): Boolean {
        return try {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (Foreground::class.java.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            !servicesWorking()
        }
    }

    private fun servicesWorking(): Boolean {
        traceBackgroundService!!.isBackgroundWorking
        return !traceBackgroundService!!.isBackgroundServiceWorking
    }

}