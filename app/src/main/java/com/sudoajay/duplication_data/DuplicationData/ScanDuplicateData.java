package com.sudoajay.duplication_data.DuplicationData;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.sudoajay.duplication_data.Toast.CustomToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScanDuplicateData {
    private List<File> getAllData = new LinkedList<>();
    private ArrayList<String> dataStore = new ArrayList<>();
    private static Context context;

    public ScanDuplicateData(final Context context) {
        ScanDuplicateData.context = context;
    }

    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            CustomToast.ToastIt(context, "cannot initialize SHA-512 hash function");
        }
    }

    public void Duplication(String external_dir, String sd_Card_dir, int internal_Visible, int external_Visible) {


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

        for (String name: lists.keySet()) {
            String key = name.toString();
            String value = lists.get(name).toString();
            Log.d("Get_All_Path",key + " " + value);
        }
            for (List<String> list : lists.values()) {
            if (list.size() > 1) {
                dataStore.addAll(list);
                dataStore.add("And");
            }

        }
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


    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
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
               int nouse = fileInput.read(fileData);
                fileInput.close();
                String uniqueFileHash = new BigInteger(1, messageDigest.digest(fileData)).toString(16);
                List<String> list = lists.get(uniqueFileHash);

                if (list == null) {
                    list = new LinkedList<>();
                    lists.put(uniqueFileHash, list);
                }
                list.add(child.getAbsolutePath());
            } catch (IOException e) {

            }
        }
    }

    private void Get_All_Path(File directory) {
        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                Get_All_Path(child);
            } else {
                if (!child.getName().equals(".nomedia"))
                    getAllData.add(child);
            }
        }
    }

    public ArrayList<String> getList() {
        return dataStore;
    }

    private boolean FileExist(String path) {
        return (new File(path).listFiles() != null && new File(path).exists());
    }
}
