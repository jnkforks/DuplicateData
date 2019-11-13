package com.sudoajay.duplication_data.DuplicationData;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.sudoajay.duplication_data.HelperClass.CustomToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScanDuplicateData {
    private List<File> getAllData = new LinkedList<>();
    private ArrayList<String> dataStore = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private String external_Path_Url, whatsapp_Path, sd_Card_dir;
    private ArrayList<String> rejectedFolder = new ArrayList<>(), apkFile = new ArrayList<>(), emptyFolder = new ArrayList<>(), logFolder = new ArrayList<>();

    public ScanDuplicateData(final Context context) {
        ScanDuplicateData.context = context;
        whatsapp_Path = "/WhatsApp/";

    }

    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            CustomToast.ToastIt(context, "cannot initialize SHA-512 hash function");
        }
    }

    public void Duplication(final String external_dir, final String sd_Card_dir, final int internal_Visible,
                            final int external_Visible) {

        this.sd_Card_dir = sd_Card_dir;
        this.external_Path_Url = external_dir;
        rejectedFolder.add(external_dir + "/Android/data");
        rejectedFolder.add(sd_Card_dir + "/Android/data");
        rejectedFolder.add(external_dir + "/Android/data");
        rejectedFolder.add(sd_Card_dir + "/Android/data");


        whatsappUnnecessaryData();

        Map<String, List<String>> lists = new HashMap<>();

        if (internal_Visible == View.VISIBLE) {
            if (FileExist(external_dir)) Get_All_Path(new File(external_dir));
        }
        if (external_Visible == View.VISIBLE) {
            if (FileExist(sd_Card_dir)) Get_All_Path(new File(sd_Card_dir));
        }
        // check for length in file_icon
        DuplicatedFilesUsingLength();

        // check for mime type
        DuplicateFileType();

        // check for hash using "SHA-512"
        DuplicatedFilesUsingHashTable(lists);

        for (List<String> list : lists.values()) {
            if (list.size() > 1) {
                dataStore.addAll(list);
                dataStore.add("And");
            }
        }
        ApkFile();
        CacheData(external_dir, sd_Card_dir, internal_Visible, external_Visible);
        EmptyFolder();
    }

    private void whatsappUnnecessaryData() {

        int size = 0;
        String path = external_Path_Url;
        ArrayList<String> folderName = new ArrayList<>(Arrays.asList(".Shared", ".Trash", "cache", ".Thumbs", "Backups","Databases"));
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 5; j++) {
                File file = new File(path + whatsapp_Path + folderName.get(j));
                if (j == 5) size = 1;
                if (file.exists() && Objects.requireNonNull(file.listFiles()).length > size) {
                    dataStore.add(file.getAbsolutePath());
                    rejectedFolder.add(file.getAbsolutePath());
                }
            }
            path = sd_Card_dir;
        }
        if (!dataStore.isEmpty())
            dataStore.add("WhatsApp Unnecessary Data");
    }


    private void CacheData(final String external_dir, final String sd_Card_dir, final int internal_Visible,
                           final int external_Visible) {

        ArrayList<String> savePath = new ArrayList<>();

        if (internal_Visible == View.VISIBLE) {
            savePath.add(external_dir);
        }
        if (external_Visible == View.VISIBLE) {
            savePath.add(sd_Card_dir);
        }
        for (String path : savePath) {
            File androidDataFolder = new File(path + "/Android/data/");
            if (androidDataFolder.exists()) {
                File[] filesList = androidDataFolder.listFiles();
                if (filesList != null) {
                    for (File file : filesList) {
                        if (file.isDirectory()) {
                            File file1 = new File(file.getAbsolutePath() + "/cache/");
                            if (file1.exists() && Objects.requireNonNull(file1.listFiles()).length > 0) {
                                dataStore.add(file.getAbsolutePath());
                            }

                        }
                    }
                }
            }
        }
        if (!dataStore.isEmpty())
            dataStore.add("App Memory");
    }


    private void DuplicatedFilesUsingLength() {
        ArrayList<Long> getAllDataLength = new ArrayList<>();
        for (File data : getAllData) {
            getAllDataLength.add(data.length());
        }

        for (int i = getAllDataLength.size() - 1; i > 0; i--) {
            for (int j = 0; j < getAllDataLength.size(); j++) {
                if (i != j) {
                    if (getAllDataLength.get(i).equals(getAllDataLength.get(j))) break;
                    if (j == getAllDataLength.size() - 1) {
                        getAllDataLength.remove(i);
                        getAllData.remove(i);
                    }
                }
            }
        }
    }

    private void DuplicateFileType() {
        ArrayList<String> getAllDataType = new ArrayList<>();
        for (File data : getAllData) {
            getAllDataType.add(getMimeType(Uri.fromFile(data)));
        }

        for (int i = getAllDataType.size() - 1; i > 0; i--) {
            for (int j = 0; j < getAllDataType.size(); j++) {
                if (i != j) {
                    if (getAllDataType.get(i) == null || getAllDataType.get(i).equals(getAllDataType.get(j)))
                        break;
                    if (j == getAllDataType.size() - 1) {
                        getAllDataType.remove(i);
                        getAllData.remove(i);
                    }
                }
            }
        }
    }


    private String getMimeType(Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private void DuplicatedFilesUsingHashTable(Map<String, List<String>> lists) {
        for (File child : getAllData) {
            try {
                FileInputStream fileInput = new FileInputStream(child);
                byte[] fileData = new byte[(int) child.length()];
                fileInput.close();
                String uniqueFileHash = new BigInteger(1, messageDigest.digest(fileData)).toString(16);
                List<String> list = lists.get(uniqueFileHash);

                if (list == null) {
                    list = new LinkedList<>();
                    lists.put(uniqueFileHash, list);
                }
                list.add(child.getAbsolutePath());
            } catch (IOException ignored) {

            }
        }
    }

    private void Get_All_Path(File directory) {
        try {
            String getname, getExt;

            for (File child : Objects.requireNonNull(directory.listFiles())) {
                if (child.isDirectory()) {
                    if (Objects.requireNonNull(child.listFiles()).length == 0) {
                        emptyFolder.add(child.getAbsolutePath());
                    } else if (child.getName().contains("log") || child.getName().contains("Log")) {
                        logFolder.add(child.getAbsolutePath());
                    } else if (!isRejectedFolder(child.getAbsolutePath())) {
                        Get_All_Path(child);
                    }
                } else {
                    getname = child.getName();
                    getExt = getExtension(getname);
                    if (getExt.equals("apk"))
                        apkFile.add(child.getAbsolutePath());
                    else if (!getname.equals(".nomedia"))
                        getAllData.add(child);

                }
            }
        } catch (Exception ignored) {

        }
    }

    private boolean isRejectedFolder(final String path) {
        for (int i = rejectedFolder.size() - 1; i > 0; i--) {
            if (path.equals(rejectedFolder.get(i))) {
                rejectedFolder.remove(i);
                return true;
            }
        }
        return false;
    }

    private void EmptyFolder() {
        dataStore.addAll(logFolder);
        dataStore.addAll(emptyFolder);
        if (!dataStore.isEmpty())
            dataStore.add("Obsolete Folder");
    }
    private void ApkFile() {
        dataStore.addAll(apkFile);
        if (!dataStore.isEmpty())
            dataStore.add("Apk");
    }

    private String getExtension(final String path) {
        int i = path.lastIndexOf('.');
        String extension = "";
        if (i > 0) {
            extension = path.substring(i + 1);
        }
        return extension;
    }
    public  ArrayList<String> getList() {
        return dataStore;
    }

    private boolean FileExist(String path) {
        return (new File(path).listFiles() != null && new File(path).exists());
    }
}
