package com.sudoajay.duplication_data.notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R

/**
 * Helper class for showing and canceling alert
 * notifications.
 *
 *
 * This class makes heavy use of the [NotificationCompat.Builder] helper
 * class to create notifications in a backward-compatible way.
 */
class NotifyNotification
/**
 * Shows the notification, or updates a previously shown notification of
 * this type, with the given parameters.
 *
 *
 * TODO: Customize this method's arguments to present relevant content in
 * the notification.
 *
 *
 * TODO: Customize the contents of this method to tweak the behavior and
 * presentation of alert  notifications. Make
 * sure to follow the
 * [
 * Notification design guidelines](https://developer.android.com/design/patterns/notifications.html) when doing so.
 *
 */
// Constructor
(private val context: Context) {
    private var notificationManager: NotificationManager? = null
    fun notify(textPass: String?, notificationHint: String) { // local variable
        // setup intent and passing value
        val intent = Intent(context, MainActivity::class.java)
        if (notificationHint.equals(context.getString(R.string.delete_Done_title), ignoreCase = true)) intent.putExtra("passing", "DuplicateData")
        // setup according Which Type
// if There is no data match with query
        val channelId: String = context.getString(R.string.transfer_Done_Id) // channel_id
        // now check for null notification manger
        if (notificationManager == null) {
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        // this check for android Oero In which Channel Id Come as New Feature
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            assert(notificationManager != null)
            var mChannel = notificationManager!!.getNotificationChannel(channelId)
            if (mChannel == null) {
                mChannel = NotificationChannel(channelId, notificationHint, importance)
                notificationManager!!.createNotificationChannel(mChannel)
            }
        }
        // Default ringtone
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, channelId) // Set appropriate defaults for the notification light, sound,
// and vibration.
                .setDefaults(Notification.DEFAULT_ALL) // Set required fields, including the small icon, the
// notification title, and text.
                .setContentTitle(notificationHint)
                .setContentText(textPass) // All fields below this line are optional.
// Use a default priority (recognized on devices running Android
// 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(uri) // Provide a large icon, shown with the notification in the
// notification drawer on devices running Android 3.0 or later.
// Set ticker text (preview) information for this notification.
                .setTicker(notificationHint) // Show a number. This is useful when stacking notifications of
// a single type.
                .setNumber(1)
                .setSmallIcon(R.drawable.data_deleted_icon)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary)) // If this notification relates to a past or upcoming event, you
// should set the relevant time information using the setWhen
// method below. If this call is omitted, the notification's
// timestamp will by set to the time at which it was shown.
// TODO: Call setWhen if this notification relates to a past or
// upcoming event. The sole argument to this method should be
// the notification timestamp in milliseconds.
//.setWhen(...)
// Set the pending intent to be initiated when the user touches
// the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT)) // Show an expanded list of items on devices running Android 4.1
// or later.
// Example additional actions for this notification. These will
// only show on devices running Android 4.1 or later, so you
// should ensure that the activity in this notification's
// content intent provides access to the same actions in
// another way.
// Automatically dismiss the notification when it is touched.
                .setAutoCancel(true)
        // check if there ia data with empty
// more and view button classification
        notify(builder.build())
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private fun notify(notification: Notification) {
        notificationManager!!.notify(NOTIFICATION_TAG, 0, notification)
    }

    companion object {
        /**
         * The unique identifier for this type of notification.
         */
        private const val NOTIFICATION_TAG = "Alert_"
    }

}