package com.sudoajay.duplication_data.Delete;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;

import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.Permission.AndroidExternalStoragePermission;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DeleteData {
    private HashMap<String, List<String>> list_Header_Child;
    private HashMap<Integer, List<Boolean>> checkBoxArray;
    private SdCardPathSharedPreference sdCardPathSharedPreference;
    private DocumentFile sdCarddocumentFile;
    private String sdCardPath, sdCardUri;
    private ShowDuplicate showDuplicate;
    private ShowDuplicate.MultiThreadingTask multiThreadingTask;

    public DeleteData(final ShowDuplicate showDuplicate, final HashMap<String, List<String>> list_Header_Child, final HashMap<Integer, List<Boolean>> checkBoxArray,
                      final ShowDuplicate.MultiThreadingTask multiThreadingTask) {
        this.list_Header_Child = list_Header_Child;
        this.checkBoxArray = checkBoxArray;
        this.multiThreadingTask = multiThreadingTask;
        this.showDuplicate = showDuplicate;

        // sd card path setup
        sdCardPathSharedPreference = new SdCardPathSharedPreference(showDuplicate);
        sdCardPath = sdCardPathSharedPreference.getSdCardPath();
        if (sdCardPathSharedPreference.getStringURI() != null) {
            sdCardUri = Split_The_URI(sdCardPathSharedPreference.getStringURI());
            Uri sd_Card_URL = Uri.parse(sdCardUri);
            sdCarddocumentFile = DocumentFile.fromTreeUri(showDuplicate, sd_Card_URL);
        }

        GetThePath();

    }


    private void GetThePath() {
        int i = 0, j = 0;

        for (HashMap.Entry<String, List<String>> entry : list_Header_Child.entrySet()) {
            for (String path : entry.getValue()) {
                if (Objects.requireNonNull(checkBoxArray.get(i)).get(j)) {
                    SeprateTheData(path);
                    multiThreadingTask.onProgressUpdate();
                }
                j++;
            }
            j = 0;
            i++;
        }
    }

    public void SeprateTheData(String path) {
        if (path.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            DeleteTheDataFromInternalStorage(path);
        } else {
            DeleteTheDataFromExternalStorage(path);
        }

    }


    public void DeleteTheDataFromInternalStorage(String path) {

        File file = new File(path);
        boolean isSuccesfull = file.delete();
        if (file.exists()) {
            try {
                isSuccesfull = file.getCanonicalFile().delete();
            } catch (IOException e) {

            }
        }

    }
    public void DeleteTheDataFromExternalStorage(String path) {
        DocumentFile sdCardDocument = sdCarddocumentFile;
        if (sdCardDocument != null) {
            String[] spiltSdPath = path.split(sdCardPath + "/");
            String[] spilt = spiltSdPath[1].split("/");
            for (String part : spilt) {
                DocumentFile nextDocument = sdCardDocument.findFile(part);
                if (nextDocument != null) {
                    sdCardDocument = nextDocument;
                }
            }
            sdCardDocument.delete();
        }

    }


    public String Split_The_URI(String url) {
        String save[] = url.split("%3A");
        return save[0] + "%3A";
    }

    public void DeleteCache(final Activity activity) {

        ArrayList<String> savePath = new ArrayList<>();
        AndroidExternalStoragePermission androidExternalStoragePermission
                = new AndroidExternalStoragePermission(showDuplicate, activity);
        AndroidSdCardPermission androidSdCardPermission
                = new AndroidSdCardPermission(showDuplicate);
        if (androidExternalStoragePermission.isExternalStorageWritable()) {
            savePath.add(androidExternalStoragePermission.getExternal_Path());
        }
        if (androidSdCardPermission.isSdStorageWritable()) {
            savePath.add(androidSdCardPermission.getSd_Card_Path_URL());
        }
        for (String path : savePath) {
            File androidDataFolder = new File(path + "/Android/data/");
            if (androidDataFolder.exists()) {
                File[] filesList = androidDataFolder.listFiles();
                if (filesList != null) {
                    for (File file : filesList) {
                        if (file.isDirectory()) {
                            file = new File(file.getAbsolutePath() + "/cache/");
                            if (file.exists()) {
                                try {
                                    boolean isSuccesfull = file.delete();
                                    if (file.exists())
                                        isSuccesfull = file.getCanonicalFile().delete();
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
