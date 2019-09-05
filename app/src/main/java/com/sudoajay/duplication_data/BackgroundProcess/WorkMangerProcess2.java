package com.sudoajay.duplication_data.BackgroundProcess;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sudoajay.duplication_data.Database_Classes.BackgroundTimerDataBase;
import com.sudoajay.duplication_data.Delete.DeleteData;
import com.sudoajay.duplication_data.DuplicationData.ScanDuplicateData;
import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.Notification.NotifyNotification;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.R;
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;


public class WorkMangerProcess2 extends Worker {

    private static LinkedHashMap<String, List<String>> list_Header_Child = new LinkedHashMap<>();
    @SuppressLint("UseSparseArrays")
    private static LinkedHashMap<Integer, List<Boolean>> checkBoxArray = new LinkedHashMap<>();
    private static List<String> list_Header = new ArrayList<>(), sets = new ArrayList<>();
    private static List<Boolean> setsBoolean = new ArrayList<>();
    private static List<String> unnecessaryList;
    private static long fileSize = 0;


    public WorkMangerProcess2(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {

        GetWorkDone(getApplicationContext());
        return Result.success();
    }

    public static void GetWorkDone(final Context context){
        int sdcard, internal = View.INVISIBLE;

        ArrayList<String> savePath = new ArrayList<>();
        AndroidSdCardPermission androidSdCardPermission
                = new AndroidSdCardPermission(context);
        if (new File(Environment.getExternalStorageDirectory().getAbsolutePath()).exists()) {
            savePath.add(Environment.getExternalStorageDirectory().getAbsolutePath());
            internal = View.VISIBLE;
        }
        if (androidSdCardPermission.isSdStorageWritable()) {
            savePath.add(androidSdCardPermission.getSd_Card_Path_URL());
            sdcard = View.VISIBLE;
        } else {
            savePath.add(null);
            sdcard = View.INVISIBLE;
        }

        ScanDuplicateData scanDuplicateData = new ScanDuplicateData(context);
        scanDuplicateData.Duplication(savePath.get(0), savePath.get(1), internal, sdcard);

        // add unnecessary data
        String whatsapp_Path = "/WhatsApp/";
        unnecessaryList = new ArrayList<>();
        unnecessaryList.add(whatsapp_Path + ".Shared/");
        unnecessaryList.add(whatsapp_Path + ".Trash/");
        unnecessaryList.add(whatsapp_Path + "cache/");
        unnecessaryList.add(whatsapp_Path + "Theme/");
        unnecessaryList.add(whatsapp_Path + ".Thumbs/");
        unnecessaryList.add(whatsapp_Path + "Databases");
        unnecessaryList.add("/Android/data/");



        int i = 0;
        for (String path: scanDuplicateData.getList()) {

            if (path.equalsIgnoreCase("And")) {
                if (!sets.isEmpty()) {

                    list_Header.add("Group " + (i + 1));

                    list_Header_Child.put(list_Header.get(i), new ArrayList<>(sets));
                    checkBoxArray.put(i, new ArrayList<>(setsBoolean));
                    i++;
                    sets.clear();
                    setsBoolean.clear();

                }
            } else {
                fileSize += new File(path).length();
                sets.add(path);
                if (setsBoolean.size() == 0 && !IsMatchUnnecessary(path)) {
                    setsBoolean.add(false);
                } else {
                    setsBoolean.add(true);
                }
            }
        }



        new DeleteData(context,list_Header_Child,checkBoxArray,
                androidSdCardPermission.getSd_Card_Path_URL(),androidSdCardPermission.getString_URI());
//        if(fileSize != 0) {
            call_Thread(context);


    }

    public static void call_Thread(final Context context) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotifyNotification notifyNotification = new NotifyNotification(context);
                notifyNotification.notify("You Have Saved " + ShowDuplicate.Convert_It(fileSize) +
                        " Of Data ", context.getResources().getString(R.string.delete_Done_title));

                // this is just for backup plan
                getNextDate(context);
            }
        }, 2000);
    }

    private static boolean IsMatchUnnecessary(final String path) {

        for (String gets : unnecessaryList) {
            if (path.contains(gets)) return true;
        }
        return false;
    }

    private static void getNextDate(final Context context) {
        BackgroundTimerDataBase backgroundTimerDataBase = new BackgroundTimerDataBase(context);
        TraceBackgroundService traceBackgroundService = new TraceBackgroundService(context);
        int hour = 0;
        if (!backgroundTimerDataBase.check_For_Empty()) {
            Cursor cursor = backgroundTimerDataBase.GetTheChoose_TypeRepeatedlyEndlessly();
            if (cursor != null && cursor.moveToFirst()) {
                cursor.moveToFirst();
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
                        String[] splits = weekdays.split("");
                        List<Integer> listWeekdays = new ArrayList<>();
                        for (String ints : splits) {
                            listWeekdays.add(Integer.parseInt(ints));
                        }

                        hour = 24 * CountDay(currentDay, listWeekdays);

                        break;
                    case 4:  // At Every month(Same Date)
                        hour = (24 * 30);
                        break;
                }
                if (hour != 0) {

                    // set next date

                }
            }
            String TAG = "Gotcha";
            try {

                assert cursor != null;
                if (!cursor.getString(2).equalsIgnoreCase("No Date Fixed")) {
                    DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

                    Date date = format.parse(cursor.getString(2));
                    Calendar calendars = Calendar.getInstance();
                    Date todayDate = calendars.getTime();
                    if (date.before(todayDate) || format.format(todayDate).equals(format.format(date))) {


                        if (!backgroundTimerDataBase.check_For_Empty()) {
                            backgroundTimerDataBase.deleteData();
                        }
                        traceBackgroundService.setTaskB("Empty");
                    }
                }


            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }
    public static int CountDay(int day, List<Integer> week_Days) {
        int temp = day, count = 0;
        do {
            count++;
            temp++;
            if (temp == 8) temp = 1;

            for (Integer week : week_Days) {
                if (temp == week) return count;
            }
        } while (temp != day);
        return 0;
    }
}



