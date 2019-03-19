package com.example.sudoajay.duplication_data.Permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;

import com.example.sudoajay.duplication_data.MainNavigation;
import com.example.sudoajay.duplication_data.SdCard.SdCardDialog;
import com.example.sudoajay.duplication_data.Toast.CustomToast;
import com.example.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference;

import java.io.File;

@SuppressLint("Registered")
public class AndroidSdCardPermission {
    private Context context;
    private String sd_Card_Path_URL = "", string_URI;
    private MainNavigation mainNavigation;
    private SdCardPathSharedPreference sdCardPathSharedPreference;


    public AndroidSdCardPermission(Context context, MainNavigation mainNavigation) {
        this.context = context;
        this.mainNavigation = mainNavigation;
        Grab();
    }

    public AndroidSdCardPermission(Context context) {
        this.context = context;
        Grab();
    }


    public void call_Thread() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Call_Custom_Dailog_Changes();
            }
        }, 1800);
    }

    public void Storage_Access_FrameWork() {
        try {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;
            if (mainNavigation != null)
                mainNavigation.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);

        } catch (Exception e) {
            CustomToast.ToastIt(context, "There is Error Please Report It");
        }
    }

    public void Call_Custom_Dailog_Changes() {
        try {
            FragmentTransaction ft = (mainNavigation).getSupportFragmentManager().beginTransaction();
            SdCardDialog sd_card_dialog = new SdCardDialog(this);
            sd_card_dialog.show(ft, "dialog");
        } catch (Exception ignored) {

        }
    }

    public boolean isGetting() {
        return (sd_Card_Path_URL.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) ||
                (!new File(sd_Card_Path_URL).exists());
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

    public String getString_URI() {
        return string_URI;
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
