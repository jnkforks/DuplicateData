package com.sudoajay.duplication_data.DuplicationData;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScanDuplicateData {
    private List<File> getAllData = new LinkedList<>();
    private ArrayList<String> dataStore = new ArrayList<>();
    private static Context context;
    private String external_Path_Url,whatsapp_Path;

    public ScanDuplicateData(final Context context) {
        ScanDuplicateData.context = context;
        external_Path_Url = Environment.getExternalStorageDirectory().getAbsolutePath();
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
        UnnecessaryData();
        CacheData(external_dir, sd_Card_dir, internal_Visible, external_Visible);
    }

    private   void UnnecessaryData() {
        if (new File(external_Path_Url + whatsapp_Path + ".Shared/").exists())
            DigDeep( external_Path_Url + whatsapp_Path + ".Shared/");
        if (new File(external_Path_Url + whatsapp_Path + ".Trash").exists())
            DigDeep(external_Path_Url + whatsapp_Path + ".Trash/");
        if (new File(external_Path_Url + whatsapp_Path + "cache").exists())
            DigDeep(external_Path_Url + whatsapp_Path + "cache/");
        if (new File(external_Path_Url + whatsapp_Path + "Theme").exists())
            DigDeep(external_Path_Url + whatsapp_Path + "Theme/");
        if (new File(external_Path_Url + whatsapp_Path + ".Thumbs").exists())
            DigDeep(external_Path_Url + whatsapp_Path + ".Thumbs/");
        if (new File(external_Path_Url + whatsapp_Path + "Databases").exists())
            WhatsappDatabase(new File(external_Path_Url + whatsapp_Path + "Databases/"));


    }
    public void WhatsappDatabase(File database_File){
        try{
            List<File> files = new ArrayList<>(Arrays.asList(database_File.listFiles()));
            Convert_Into_Last_Modified(files);
            if (files.size() > 1) {
                for (int i = files.size() - 1; i >= 1; i--) {
                    dataStore.add(files.get(i).getAbsolutePath());
                }
                dataStore.add("And");
            }
        }catch (Exception e){

        }
    }

    public void Convert_Into_Last_Modified(List<File> files){
        File temp_File;
        for (int i = 0 ; i < files.size();i++){
            for (int j = i ; j < files.size()-1;j++){
                Date date1 = new Date(files.get(i).lastModified());
                Date date2 = new Date(files.get(j+1).lastModified());
                if(date1.compareTo(date2) < 0){
                    temp_File=files.get(i);
                    files.set(i,files.get(j+1));
                    files.set(j+1,temp_File);
                }
            }
        }
    }


    private void DigDeep(final String folder){
        File[] files = new File(folder).listFiles();
        if (files.length != 0) {
            for (File data : files) {
                dataStore.add(data.getAbsolutePath());
            }
            dataStore.add("And");
        }
    }

    public void CacheData(final String external_dir, final String sd_Card_dir, final int internal_Visible,
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
                            if (new File(file.getAbsolutePath() + "/cache/").exists()) {
                                SaveCacheFiles(new File(file.getAbsolutePath() + "/cache/").listFiles());
                            }

                        }
                    }
                }
            }
        }
        if (!dataStore.isEmpty())
            dataStore.add("And");
    }

    private void SaveCacheFiles(final File[] file) {
        for (File getFile : file) {
            if (getFile.isDirectory()) {
                SaveCacheFiles(getFile.listFiles());
            } else {
                dataStore.add(getFile.getAbsolutePath());
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
