package com.sudoajay.duplication_data.intentService

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.delete.DeleteDataUsingDoc
import com.sudoajay.duplication_data.delete.DeleteDataUsingFile
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.helperClass.FileSize
import com.sudoajay.duplication_data.notification.NotifyNotification
import java.util.*

class DeletingTask : IntentService("Deleting Task") {

    private var progress = 0
    private var totalCount = 0
    private var totalSize = 0L
    private var contentView: RemoteViews? = null
    private var notification: Notification? = null
    private var deletedList: MutableList<String> = ArrayList()
    private var notificationManager: NotificationManager? = null

    override fun onHandleIntent(intent: Intent?) {

        deletedList = ShowDuplicate.DataHolder.instance.dataList
        totalCount = intent!!.getIntExtra("TotalCount", 0)
        totalSize = intent.getLongExtra("TotalSize", 0L)
        intent.action = ShowDuplicate.actionKey

        notification()
        //  Here use of DocumentFile in android 10 not File is using anymore
        if (Build.VERSION.SDK_INT <= 28) {
            DeleteDataUsingFile(this@DeletingTask, deletedList)
        } else {
            DeleteDataUsingDoc(this@DeletingTask, deletedList)
        }

        progressDone()
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent.putExtra("broadcastMessage", true))

    }

    fun updateProgress() {
        progress++
        contentView!!.setTextViewText(R.id.size_Title, "$progress/$totalCount")
        contentView!!.setTextViewText(R.id.percent_Text, (progress * 100 / totalCount).toString() + "%")
        contentView!!.setTextViewText(R.id.time_Tittle, getCurrentTime())
        contentView!!.setProgressBar(R.id.progressBar, totalCount, progress, false)
        notificationManager!!.notify(1, notification)
    }

    private fun progressDone() {
        notificationManager!!.cancel(1)
        val notifyNotification = NotifyNotification(applicationContext)
        notifyNotification.notify("You Have Saved " + FileSize.convertIt(totalSize) + " Of Data ", getString(R.string.delete_Done_title))
    }

    fun notification() {
        val id = getString(R.string.duplicate_Id) // default_channel_id
        val title = getString(R.string.duplicate_title) // Default Channel
        val mBuilder: NotificationCompat.Builder
        val closeButton = Intent()
        closeButton.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        contentView = RemoteViews(packageName, R.layout.activity_custom_notification)
        contentView!!.setImageViewResource(R.id.image, R.mipmap.ic_launcher)
        contentView!!.setTextViewText(R.id.title, "Deletion...")
        contentView!!.setTextViewText(R.id.time_Tittle, getCurrentTime())
        contentView!!.setProgressBar(R.id.progressBar, 100, 0, false)
        contentView!!.setTextViewText(R.id.size_Title, "0/$totalCount")
        contentView!!.setTextViewText(R.id.percent_Text, "00%")
        if (notificationManager == null) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            assert(notificationManager != null)
            var mChannel = notificationManager!!.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, title, importance)
                notificationManager!!.createNotificationChannel(mChannel)
            }
        }
        mBuilder = NotificationCompat.Builder(applicationContext, id)
                .setSmallIcon(R.mipmap.ic_launcher) // required
                .setContent(contentView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setLights(Color.parseColor("#075e54"), 3000, 3000)

        if (Build.VERSION.SDK_INT < 18) mBuilder.setSmallIcon(R.drawable.internal_storage_icon).color =
                ContextCompat.getColor(applicationContext, R.color.colorPrimary)


        mBuilder.setContentIntent(
                PendingIntent.getActivity(
                        applicationContext,
                        0,
                        closeButton,
                        PendingIntent.FLAG_UPDATE_CURRENT))
        notification = mBuilder.build()
        notification!!.flags = notification!!.flags or Notification.FLAG_AUTO_CANCEL
        notificationManager!!.notify(1, notification)
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val hours = calendar[Calendar.HOUR_OF_DAY]
        val minutes = calendar[Calendar.MINUTE]
        return if (hours < 12) {
            "$hours:$minutes AM"
        } else {
            (hours - 12).toString() + ":" + minutes + " PM"
        }
    }


}

