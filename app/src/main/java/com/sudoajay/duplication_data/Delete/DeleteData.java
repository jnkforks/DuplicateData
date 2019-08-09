package com.sudoajay.duplication_data.Delete;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.Permission.AndroidExternalStoragePermission;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class DeleteData {
    private LinkedHashMap<String, List<String>> list_Header_Child, sdCardStore = new LinkedHashMap<>();
    private LinkedHashMap<Integer, List<Boolean>> checkBoxArray;
    private SdCardPathSharedPreference sdCardPathSharedPreference;
    private DocumentFile sdCarddocumentFile;
    private String sdCardPath, sdCardUri;
    private ShowDuplicate showDuplicate;
    private ShowDuplicate.MultiThreadingTask multiThreadingTask;
    private List<String> sdcard = new ArrayList<>(), pathStore = new ArrayList<>();
    private Boolean atBackground;


    public DeleteData(final ShowDuplicate showDuplicate, final LinkedHashMap<String, List<String>> list_Header_Child, final LinkedHashMap<Integer, List<Boolean>> checkBoxArray,
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
        atBackground = false;
        GetThePath();

    }

    public DeleteData(final Context context,final LinkedHashMap<String, List<String>> list_Header_Child, final LinkedHashMap<Integer, List<Boolean>> checkBoxArray,final String sdCardPath,
                      final String sdCardUri){

        this.list_Header_Child = list_Header_Child;
        this.checkBoxArray =checkBoxArray;
        this.sdCardPath = sdCardPath;
        if (sdCardUri != null) {
            this.sdCardUri = sdCardUri;
            Uri sd_Card_URL = Uri.parse(sdCardUri);
            sdCarddocumentFile = DocumentFile.fromTreeUri(context, sd_Card_URL);
        }
        atBackground = true;
        GetThePath();
    }

    private void GetThePath() {
        int i = 0, j = 0;
        
        for (LinkedHashMap.Entry<String, List<String>> entry : list_Header_Child.entrySet()) {
            for (String path : entry.getValue()) {
                if (Objects.requireNonNull(checkBoxArray.get(i)).get(j)){
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
            if(!atBackground) multiThreadingTask.onProgressUpdate();
        } else {
            sdcard.add(path);
        }

    }
    public static void DeleteTheDataFromInternalStorage(String path) {

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
                    assert save != null;
                    save.delete();
                    if(!atBackground) multiThreadingTask.onProgressUpdate();
                }
            }
        }
    }

}
