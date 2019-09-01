package com.sudoajay.duplication_data.BackgroundProcess;


import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.view.View;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sudoajay.duplication_data.DuplicationData.ScanDuplicateData;
import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.Notification.NotifyNotification;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.R;
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService;

import java.io.File;
import java.util.ArrayList;


public class WorkMangerProcess1 extends Worker {


    public WorkMangerProcess1(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
            GetWorkDone(getApplicationContext());
        return Result.success();
    }

    public static void GetWorkDone(final Context context){
        // local variable
        int sdcard, internal = View.INVISIBLE;
        long size = 0;
        String textPass;

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

        for (String path : scanDuplicateData.getList()) {
            if (!path.equalsIgnoreCase("and")) {
                size += new File(path).length();
            }
        }

        if (size != 0) {
            textPass = "We Have Found " + ShowDuplicate.Convert_It(size) + " Of Duplicate Files";
            NotifyNotification notifyNotification = new NotifyNotification(context);
            notifyNotification.notify(textPass, context.getString(R.string.file_found_title));
        }


        TraceBackgroundService traceBackgroundService = new TraceBackgroundService(context);
        // set next date
        traceBackgroundService.setTaskA();
    }

}

