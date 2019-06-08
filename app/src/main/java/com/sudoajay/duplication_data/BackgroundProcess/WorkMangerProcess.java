package com.sudoajay.duplication_data.BackgroundProcess;


import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.sudoajay.duplication_data.DuplicationData.ScanDuplicateData;
import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.Notification.NotifyNotification;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.R;

import java.io.File;
import java.util.ArrayList;

import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class WorkMangerProcess extends Worker {


    public WorkMangerProcess(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        // local variable
        int sdcard, internal = View.INVISIBLE;
        long size = 0;
        String textPass;

        ArrayList<String> savePath = new ArrayList<>();
        AndroidSdCardPermission androidSdCardPermission
                = new AndroidSdCardPermission(getApplicationContext());
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

        ScanDuplicateData scanDuplicateData = new ScanDuplicateData(getApplicationContext());
        scanDuplicateData.Duplication(savePath.get(0), savePath.get(1), internal, sdcard);

        for (String path : scanDuplicateData.getList()) {
            if (!path.equalsIgnoreCase("and")) {
                size += new File(path).length();
            }
        }

        textPass = "We Have Found " + ShowDuplicate.Convert_It(size) + " Of Duplicate Files";
        NotifyNotification notifyNotification = new NotifyNotification(getApplicationContext());
        notifyNotification.notify(textPass, getApplicationContext().getString(R.string.transfer_Done_title));

        return Result.success();
    }

}

