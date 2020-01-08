package com.sudoajay.duplication_data.permission

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import com.sudoajay.duplication_data.R

class NotificationPermissionCheck(private val activity: Activity) {
    private fun openSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.packageName))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    fun checkNotificationPermission(): Boolean {
        return NotificationManagerCompat.from(activity.applicationContext).areNotificationsEnabled()
    }

    fun customAertDialog() {
        val dialog = Dialog(activity)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_custom_notification_permission)
        val buttonNo = dialog.findViewById<TextView>(R.id.no_button)
        val buttonYes = dialog.findViewById<TextView>(R.id.yes_Button)
        // if button is clicked, close the custom dialog
        buttonYes.setOnClickListener {
            openSetting()
            dialog.dismiss()
        }
        buttonNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

}