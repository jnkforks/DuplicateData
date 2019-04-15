package com.sudoajay.duplication_data.Permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.sudoajay.duplication_data.MainFragments.Scan;
import com.sudoajay.duplication_data.MainNavigation;
import com.sudoajay.duplication_data.SdCard.SdCardDialog;
import com.sudoajay.duplication_data.Toast.CustomToast;
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference;

import java.io.File;

@SuppressLint("Registered")
public class AndroidSdCardPermission {
    private Context context;
    private String sd_Card_Path_URL = "", string_URI;
    private MainNavigation mainNavigation;
    private Scan scan;
    private SdCardPathSharedPreference sdCardPathSharedPreference;
    private Activity activity;

    public AndroidSdCardPermission(Context context, MainNavigation mainNavigation, Activity activity) {
        this.context = context;
        this.mainNavigation = mainNavigation;
        this.activity =activity;
        Grab();
    }

    public AndroidSdCardPermission(Context context, Scan scan, Activity activity) {
        this.context = context;
        this.scan = scan;
        this.activity =activity;
        Grab();
    }


    public AndroidSdCardPermission(Context context) {
        this.context = context;
        Grab();
    }


    public void call_Thread() {
        if (!isSdStorageWritable()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Call_Custom_Dailog_Changes();
                }
            }, 1800);
        }
    }

    public void Storage_Access_FrameWork() {
        try {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;

            if (mainNavigation != null) {
                Log.d("Storage_Access","Donee");
                mainNavigation.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
            }
            else if (scan != null) {
                Log.d("Storage_Access","Doneeeeeee");
                scan.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
            }

        } catch (Exception e) {
            CustomToast.ToastIt(context, "There is Error Please Report It");
        }
    }

    public void Call_Custom_Dailog_Changes() {
        try {
            FragmentTransaction ft = (((FragmentActivity) activity)).getSupportFragmentManager().beginTransaction();
            SdCardDialog sd_card_dialog = new SdCardDialog(this);
            sd_card_dialog.show(ft, "dialog");
        } catch (Exception ignored) {

        }
    }

    public boolean isSdStorageWritable() {
        return (!sd_Card_Path_URL.equals(Environment.getExternalStorageDirectory().getAbsolutePath()) &&
                new File(sd_Card_Path_URL).exists() && new File(sd_Card_Path_URL).listFiles() != null );
    }

    public void Grab() {
        // gran the data from shared preference
        sdCardPathSharedPreference = new SdCardPathSharedPreference(context);
        try {

            sd_Card_Path_URL = sdCardPathSharedPreference.getSdCardPath();
            string_URI = sdCardPathSharedPreference.getStringURI();
        } catch (Exception ignored) {


        }
    }

    public String getSd_Card_Path_URL() {
        return sd_Card_Path_URL;
    }


    public void setSd_Card_Path_URL(String sd_Card_Path_URL) {
        this.sd_Card_Path_URL = sd_Card_Path_URL;
        sdCardPathSharedPreference.setSdCardPath(sd_Card_Path_URL);
    }

    public void setString_URI(String string_URI) {
        this.string_URI = string_URI;
        sdCardPathSharedPreference.setStringURI(string_URI);

    }
}
