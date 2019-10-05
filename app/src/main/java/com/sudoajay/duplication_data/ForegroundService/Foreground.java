package com.sudoajay.duplication_data.ForegroundService;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sudoajay.duplication_data.BackgroundProcess.WorkMangerProcess1;
import com.sudoajay.duplication_data.BackgroundProcess.WorkMangerProcess2;
import com.sudoajay.duplication_data.Database_Classes.BackgroundTimerDataBase;
import com.sudoajay.duplication_data.MainActivity;
import com.sudoajay.duplication_data.R;
import com.sudoajay.duplication_data.Receive_Boot_Completed.ForegroundServiceBoot;
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class Foreground extends Service {

    public static final String CHANNEL_ID = "Foreground Service";
    private TraceBackgroundService traceBackgroundService;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // create object
        traceBackgroundService = new TraceBackgroundService(getApplicationContext());

        if (Objects.requireNonNull(intent.getStringExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"))
                .equalsIgnoreCase("Start_Foreground")) {

            createNotificationChannel();

            final String url = "https://dontkillmyapp.com/problem";
            Intent knowMoreIntent = new Intent(Intent.ACTION_VIEW);
            knowMoreIntent.setData(Uri.parse(url));

            Intent stopIntent = new Intent(getApplicationContext(), MainActivity.class);
            stopIntent.setAction("Stop_Foreground(Setting)");

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setDefaults(Notification.DEFAULT_ALL)
                    // Set required fields, including the small icon, the
                    // notification title, and text.
                    .setContentTitle("Foreground Service")
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setVibrate(new long[]{0L})

                    // All fields below this line are optional.

                    // Use a default priority (recognized on devices running Android
                    // 4.1 or later)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    //     .setSound(uri)
                    // Provide a large icon, shown with the notification in the
                    // notification drawer on devices running Android 3.0 or later.

                    // Show a number. This is useful when stacking notifications of
                    // a single type.
                    .setNumber(1)

                    // If this notification relates to a past or upcoming event, you
                    // should set the relevant time information using the setWhen
                    // method below. If this call is omitted, the notification's
                    // timestamp will by set to the time at which it was shown.
                    // TODO: Call setWhen if this notification relates to a past or
                    // upcoming event. The sole argument to this method should be
                    // the notification timestamp in milliseconds.
                    //.setWhen(...)
                    .setSmallIcon(R.drawable.scan_icon)
                    // Set the pending intent to be initiated when the user touches
                    // the notification.
                    .addAction(R.drawable.know_more_icon,
                            this.getString(R.string.why_This_Foreground_Service),
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    knowMoreIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.stop_icon,
                            this.getString(R.string.stop_Foreground_Service),
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    stopIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT))

                    .setOnlyAlertOnce(true)
                    // Show an expanded list of items on devices running Android 4.1
                    // or later.


                    // Example additional actions for this notification. These will
                    // only show on devices running Android 4.1 or later, so you
                    // should ensure that the activity in this notification's
                    // content intent provides access to the same actions in
                    // another way.


                    // Automatically dismiss the notification when it is touched.
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(
                            this,
                            0,
                            new Intent(this, MainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT));

            startForeground(1337, notification.build());

            // check if date matches then run the process

//            // first Process or Task A
            if (DatesMatches(traceBackgroundService.getTaskA(), 1))
                WorkMangerProcess1.GetWorkDone(getApplicationContext());

            // Second Process or Task B
            if (DatesMatches(traceBackgroundService.getTaskB(), 2))
                WorkMangerProcess2.GetWorkDone(getApplicationContext());


            Task();
        } else if (Objects.requireNonNull(intent.getStringExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"))
                .equalsIgnoreCase("Stop_Foreground")) {
            //your  service end here
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT

            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private void startForeground() {
        Intent serviceIntent = new Intent(getApplicationContext(), ForegroundServiceBoot.class);
        serviceIntent.setAction("RebootReceiver");
        getApplication().startService(serviceIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean DatesMatches(final String date, final int type) {
        try {

            // set The Today Date
            Calendar todayCalender = Calendar.getInstance();
            Date todayDate = todayCalender.getTime();

            // convert to Date
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

            Date curDate = format.parse(date);
            if (todayDate.after(curDate)) {
                if (!format.format(todayDate).equals(format.format(curDate))) {
                    if (type == 1) {
                        traceBackgroundService.setTaskA();
                    } else {

                        traceBackgroundService.setTaskB
                                (TraceBackgroundService.NextDate(getHours(getApplicationContext())));
                    }
                }
            }
            if (format.format(todayDate).equals(format.format(curDate)))
                return true;
            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    private void Task() {
        Intent startIntent = new Intent(getApplicationContext(), Foreground.class);
        startIntent.putExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"
                , "Start_Foreground");
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, startIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        startAlarm(alarmManager, pintent);

    }

    private void startAlarm(final AlarmManager alarmManager, final PendingIntent pendingIntent) {
        long setTime = 3600000 * 3; // 3 hours
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + setTime, pendingIntent);

        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + setTime, pendingIntent);
        }

    }

    public static int getHours(final Context context) {

        BackgroundTimerDataBase backgroundTimerDataBase = new BackgroundTimerDataBase(context);
        // set the Task is started

        // this task for cleaning and show today task
        int hour = 0;

        // grab the data From Database


        if (!backgroundTimerDataBase.check_For_Empty()) {
            Cursor cursor = backgroundTimerDataBase.GetTheRepeatedlyWeekdays();
            if (cursor != null && cursor.moveToFirst()) {
                cursor.moveToFirst();

                try {

                    switch (cursor.getInt(0)) {
                        case 0: // At Every 1/2 Day
                            hour = 12;
                            break;
                        case 1:// At Every 1 Day
                            hour = 24;
                            break;
                        case 2:
                            // At Every 2 Day
                            hour = (24 * 2);
                            break;
                        case 3:

                            Calendar calendar = Calendar.getInstance();
                            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                            String weekdays = cursor.getString(1);
                            List<Integer> listWeekdays = new ArrayList<>();
                            for (int i = 0; i < weekdays.length(); i++) {
                                listWeekdays.add(Character.getNumericValue(weekdays.charAt(i)));
                            }

                            hour = 24 * WorkMangerProcess2.CountDay(currentDay, listWeekdays);

                            break;
                        case 4:  // At Every month(Same Date)
                            hour = (24 * 30);
                            break;
                    }

                } catch (Exception e) {
                }
            }
        }
        return hour;
    }
}