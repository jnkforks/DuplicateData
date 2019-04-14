package com.example.sudoajay.duplication_data.Notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.sudoajay.duplication_data.MainNavigation;
import com.example.sudoajay.duplication_data.R;

/**
 * Helper class for showing and canceling alert
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class NotifyNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Alert_";
    private Context context;
    private NotificationManager notificationManager;


    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of alert  notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */

    // Constructor
    public NotifyNotification(final Context context) {
        this.context = context;
    }

    public void notify(final String textPass, final String notficationHint) {

        // local variable
        String channel_id;
        Intent intent;


        // setup intent and passing value
        intent = new Intent(context, MainNavigation.class);

        if (notficationHint.equalsIgnoreCase(context.getString(R.string.transfer_Done_title)))
            intent.putExtra("passing", "DuplicateData");


        // setup according Which Type
        // if There is no data match with query

        channel_id = context.getString(R.string.transfer_Done_Id); // channel_id


        // now check for null notification manger
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // this check for android Oero In which Channel Id Come as New Feature
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            assert notificationManager != null;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(channel_id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(channel_id, notficationHint, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        // Default ringtone
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)
                // Set required fields, including the small icon, the
                // notification title, and text.
                .setContentTitle(notficationHint)
                .setContentText(textPass)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(uri)
                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                // Set ticker text (preview) information for this notification.
                .setTicker(notficationHint)

                // Show a number. This is useful when stacking notifications of
                // a single type.
                .setNumber(1)
                .setSmallIcon(R.drawable.data_deleted_icon)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                // If this notification relates to a past or upcoming event, you
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
                                PendingIntent.FLAG_UPDATE_CURRENT))

                // Show an expanded list of items on devices running Android 4.1
                // or later.


                // Example additional actions for this notification. These will
                // only show on devices running Android 4.1 or later, so you
                // should ensure that the activity in this notification's
                // content intent provides access to the same actions in
                // another way.


                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        // check if there ia data with empty
        // more and view button classification
        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private void notify(final Context context, final Notification notification) {

        notificationManager.notify(NOTIFICATION_TAG, 0, notification);
    }


    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }


}
