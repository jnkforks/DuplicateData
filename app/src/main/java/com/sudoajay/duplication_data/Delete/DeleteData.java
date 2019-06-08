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
    private HashMap<String, List<String>> list_Header_Child, sdCardStore = new HashMap<>();;
    private HashMap<Integer, List<Boolean>> checkBoxArray;
    private SdCardPathSharedPreference sdCardPathSharedPreference;
    private DocumentFile sdCarddocumentFile;
    private String sdCardPath, sdCardUri;
    private ShowDuplicate showDuplicate;
    private ShowDuplicate.MultiThreadingTask multiThreadingTask;
    private List<String> sdcard = new ArrayList<>(), pathStore = new ArrayList<>();


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
            sdCardUri = (sdCardPathSharedPreference.getStringURI());
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

                }
                j++;
            }
            j = 0;
            i++;
        }
        SeprateTheSDCardPath();
        DeleteTheDataFromExternalStorage();
    }

    public void SeprateTheData(String path) {
        if (path.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            DeleteTheDataFromInternalStorage(path);
            multiThreadingTask.onProgressUpdate();
        } else {
            sdcard.add(path);
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

    private void SeprateTheSDCardPath() {
        for (String path : sdcard) {
            pathStore.clear();
            File file = new File(path);
            String parentPath = file.getParentFile().toString();
            String filePathName = file.getName();

            if (sdCardStore.get(parentPath) != null)
                pathStore.addAll(Objects.requireNonNull(sdCardStore.get(parentPath)));

            pathStore.add(filePathName);
            sdCardStore.put(parentPath, new ArrayList<>(pathStore));
        }
    }

    public void DeleteTheDataFromExternalStorage() {
        for (String getKey : sdCardStore.keySet()) {
            DocumentFile sdCardDocument = sdCarddocumentFile;
            if (sdCardDocument != null) {
                String[] spiltSdPath = getKey.split(sdCardPath);
                if (spiltSdPath.length > 1) {
                    String[] spilt = spiltSdPath[1].split("/");
                    for (String part : spilt) {
                        DocumentFile nextDocument = sdCardDocument.findFile(part);
                        if (nextDocument != null) {
                            sdCardDocument = nextDocument;
                        }
                    }
                }
                for (String value : Objects.requireNonNull(sdCardStore.get(getKey))) {
                    DocumentFile save = sdCardDocument.findFile(value);
                    save.delete();
                    multiThreadingTask.onProgressUpdate();
                }
            }
        }
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
