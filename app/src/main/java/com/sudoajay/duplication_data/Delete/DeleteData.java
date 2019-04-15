package com.sudoajay.duplication_data.Delete;

import android.app.Activity;
import android.content.Context;
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
    private String sdCardPath;
    private Context context;
    private ShowDuplicate.MultiThreadingTask multiThreadingTask;

    public DeleteData(final Context context, final HashMap<String, List<String>> list_Header_Child, final HashMap<Integer, List<Boolean>> checkBoxArray,
                      final ShowDuplicate.MultiThreadingTask multiThreadingTask) {
        this.list_Header_Child = list_Header_Child;
        this.checkBoxArray = checkBoxArray;
        this.multiThreadingTask = multiThreadingTask;
        this.context = context;

        // sd card path setup
        sdCardPathSharedPreference = new SdCardPathSharedPreference(context);
        sdCardPath = sdCardPathSharedPreference.getSdCardPath();
        if (sdCardPathSharedPreference.getStringURI() != null) {
            String sd_Card_Uri = Split_The_URI(sdCardPathSharedPreference.getStringURI());
            Uri sd_Card_URL = Uri.parse(sd_Card_Uri);
            sdCarddocumentFile = DocumentFile.fromTreeUri(context, sd_Card_URL);
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
                e.printStackTrace();
            }
        }

    }

    public void DeleteTheDataFromExternalStorage(String path) {
        if (sdCarddocumentFile != null) {
            String[] spiltSdPath = path.split(sdCardPath + "/");
            String[] spilt = spiltSdPath[1].split("/");
            for (int i = 0; i < spilt.length; i++) {
                if (i == spilt.length - 1) {
                    sdCarddocumentFile.delete();
                } else {
                    sdCarddocumentFile = getIntoDocument(sdCarddocumentFile, spilt[i]);
                }
            }
        }
    }

    public DocumentFile getIntoDocument(final DocumentFile documentFile, final String name) {
        return documentFile.findFile(check_For_Duplicate(documentFile, name));
    }


    public String check_For_Duplicate(DocumentFile file, String name) {
        DocumentFile[] Files = file.listFiles();
        for (DocumentFile files : Files) {
            if (Objects.requireNonNull(files.getName()).equalsIgnoreCase(name)) {
                return files.getName();
            }
        }
        return name;
    }

    public String Split_The_URI(String url) {
        String save[] = url.split("%3A");
        return save[0] + "%3A";
    }

    public void DeleteCache(final Activity activity) {

        ArrayList<String> savePath = new ArrayList<>();
        AndroidExternalStoragePermission androidExternalStoragePermission
                = new AndroidExternalStoragePermission(context, activity);
        AndroidSdCardPermission androidSdCardPermission
                = new AndroidSdCardPermission(context);
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
