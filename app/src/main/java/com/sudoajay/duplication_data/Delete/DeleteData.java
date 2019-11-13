package com.sudoajay.duplication_data.Delete;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class DeleteData {
    private LinkedHashMap<String, List<String>> list_Header_Child, sdCardStore = new LinkedHashMap<>();
    private LinkedHashMap<Integer, List<Boolean>> checkBoxArray;
    private DocumentFile sdCarddocumentFile;
    private String sdCardPath, sdCardUri;
    private ShowDuplicate.MultiThreadingTask2 multiThreadingTask2;
    private List<String> sdcard = new ArrayList<>(), pathStore = new ArrayList<>();
    private Boolean atBackground;


    public DeleteData(final ShowDuplicate showDuplicate, final LinkedHashMap<String, List<String>> list_Header_Child, final LinkedHashMap<Integer, List<Boolean>> checkBoxArray,
                      final ShowDuplicate.MultiThreadingTask2 multiThreadingTask2) {
        this.list_Header_Child = list_Header_Child;
        this.checkBoxArray = checkBoxArray;
        this.multiThreadingTask2 = multiThreadingTask2;

        // sd card path setup
        SdCardPathSharedPreference sdCardPathSharedPreference = new SdCardPathSharedPreference(showDuplicate);
        sdCardPath = sdCardPathSharedPreference.getSdCardPath();
        if (!sdCardPathSharedPreference.getStringURI().isEmpty()) {
            sdCardUri = (sdCardPathSharedPreference.getStringURI());
            Uri sd_Card_URL = Uri.parse(sdCardUri);
            sdCarddocumentFile = DocumentFile.fromTreeUri(showDuplicate, sd_Card_URL);
        }
        atBackground = false;
        GetThePath();

    }

//        public enum DataHolder {
//        INSTANCE;
//
//        private ArrayList<String> mObjectList;
//
//        public static boolean hasData() {
//            return INSTANCE.mObjectList != null;
//        }
//
//        public static void setData(final ArrayList<String> objectList) {
//            INSTANCE.mObjectList = objectList;
//        }
//
//        public static ArrayList<String> getData() {
//            final ArrayList<String> retList = INSTANCE.mObjectList;
//            INSTANCE.mObjectList = null;
//            return retList;
//        }
//    }

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

    private void SeprateTheData(String path) {
        if (path.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            if (path.contains("/WhatsApp/Databases")) {
                WhatsappDatabase(new File(path));
            } else {
                DeleteTheDataFromInternalStorage(path);
            }
            if (!atBackground) multiThreadingTask2.onProgressUpdate();
        } else {
            sdcard.add(path);
        }

    }

    private void DeleteTheDataFromInternalStorage(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                DeleteTheDataFromInternalStorage(child.getAbsolutePath());
            }
            if (Objects.requireNonNull(file.listFiles()).length == 0) file.delete();
        } else {
            file.delete();
            if (file.exists()) {
                try {
                    file.getCanonicalFile().delete();
                } catch (IOException ignored) {

                }
            }
        }

    }

    private void SeprateTheSDCardPath() {
        for (String path : sdcard) {
            pathStore.clear();
            File file = new File(path);
            String parentPath = Objects.requireNonNull(file.getParentFile()).toString();
            String filePathName = file.getName();

            if (sdCardStore.get(parentPath) != null)
                pathStore.addAll(Objects.requireNonNull(sdCardStore.get(parentPath)));

            pathStore.add(filePathName);
            sdCardStore.put(parentPath, new ArrayList<>(pathStore));
        }
    }

    private void DeleteTheDataFromExternalStorage() {
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
                    if (!save.isDirectory()) {
                        save.delete();
                    } else {
                        for (DocumentFile get : save.listFiles()) {
                            DocumentFile file = save.findFile(Objects.requireNonNull(get.getName()));
                            assert file != null;
                            file.delete();
                        }
                    }
                    save.delete();

                    if (!atBackground) multiThreadingTask2.onProgressUpdate();
                }
            }
        }
    }

    private void WhatsappDatabase(File database_File) {
        try {
            List<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(database_File.listFiles())));
            Convert_Into_Last_Modified(files);
            if (files.size() > 1) {
                for (int i = files.size() - 1; i >= 1; i--) {
                    DeleteTheDataFromInternalStorage(files.get(i).getAbsolutePath());
                }

            }
        } catch (Exception ignored) {

        }
    }

    private void Convert_Into_Last_Modified(List<File> files) {
        File temp_File;
        for (int i = 0; i < files.size(); i++) {
            for (int j = i; j < files.size() - 1; j++) {
                Date date1 = new Date(files.get(i).lastModified());
                Date date2 = new Date(files.get(j + 1).lastModified());
                if (date1.compareTo(date2) < 0) {
                    temp_File = files.get(i);
                    files.set(i, files.get(j + 1));
                    files.set(j + 1, temp_File);
                }
            }
        }
    }

}
