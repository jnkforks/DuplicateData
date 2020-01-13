package com.sudoajay.duplication_data.permission

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.helperClass.CustomToast

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

    fun customAlertDialog() {

        AlertDialog.Builder(activity)
                .setIcon(R.drawable.alert_icon)
                .setTitle(activity.getString(R.string.custom_notification_permission_heading))
                .setMessage(activity.getString(R.string.custom_notification_text))
                .setCancelable(true)
                .setPositiveButton(activity.getString(R.string.custom_dialog_yes)) { _, _ ->
                    openSetting()
                }
                .setNegativeButton(activity.getString(R.string.custom_dialog_no)) { _, _ ->

                }
                .show()
    }

}