package com.sudoajay.duplication_data.BackgroundProcess;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WorkMangerTaskManager extends Worker {

    private List<OneTimeWorkRequest> list = new ArrayList<>();

    public WorkMangerTaskManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {


        OneTimeWorkRequest everyDayWork =
                new OneTimeWorkRequest.Builder(WorkMangerProcess1.class).addTag("Regular Duplicate Size").setInitialDelay(20
                        , TimeUnit.MINUTES).build();

        OneTimeWorkRequest onceAWeekWork =
                new OneTimeWorkRequest.Builder(WorkMangerProcess2.class).addTag("Background Delete Duplicate").setInitialDelay(1
                        , TimeUnit.HOURS).build();


        TraceBackgroundService traceBackgroundService = new TraceBackgroundService(getApplicationContext());
        traceBackgroundService.setTaskA();

        Calendar calendars = Calendar.getInstance();
        Date todayDate = calendars.getTime();

        // specific date from database
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);


        // Check for Date A Task
        Date date;
        try {
            date = dateFormat.parse(traceBackgroundService.getTaskA());

            if (dateFormat.format(todayDate).equals(dateFormat.format(date)) || date.before(todayDate)) {
            list.add(everyDayWork);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check for Date B Task
        try {
            date = dateFormat.parse(traceBackgroundService.getTaskB());


            if (dateFormat.format(todayDate).equals(dateFormat.format(date)) || date.before(todayDate)) {
            list.add(onceAWeekWork);
        }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        WorkManager.getInstance(getApplicationContext())
                .beginWith(list)
                .enqueue();


        return Result.success();
    }


}
