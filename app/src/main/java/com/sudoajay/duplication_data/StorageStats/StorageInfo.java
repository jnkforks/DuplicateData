package com.sudoajay.duplication_data.StorageStats;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.text.DecimalFormat;

public class StorageInfo {

    private String sd_Card_Path_URL;
    private String availableInternalPercent;

    private long internal_Available_Size , internal_Total_Size
            , external_Available_Size , external_Total_Size;


    // get sd card path url constructor
    public StorageInfo(String sd_Card_Path_URL ){
        this.sd_Card_Path_URL = sd_Card_Path_URL;

        getAvailableInternalMemorySize(); 
        getAvailableExternalMemorySize();
    }


    private boolean externalMemoryAvailable() {

        return new File(sd_Card_Path_URL).exists();
    }

    private void getAvailableInternalMemorySize() {
        try{
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        internal_Available_Size = availableBlocks* blockSize;
        }catch (Exception ignored){

        }
    }
    public  String getUsedInternalMemorySize() {
        try{
          return Convert_It(internal_Total_Size-internal_Available_Size);
        }catch (Exception ignored){
            return "0.00 GB";
        }
    }

    public  String getTotalInternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        internal_Total_Size = totalBlocks* blockSize;
        return Convert_It(internal_Total_Size);
    }

    public String getAvailableInternalPercentage(){
        return availableInternalPercent = GetDecimal2Round(((double)(internal_Available_Size*100)/internal_Total_Size));
    }
    public String getUsedInternalPercentage(){
        return GetDecimal2Round((((double) (internal_Total_Size - internal_Available_Size) * 100) / internal_Total_Size));
    }


    private void getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            StatFs stat = new StatFs(sd_Card_Path_URL);
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            external_Available_Size = availableBlocks * blockSize;
        } else {
            external_Available_Size = 0;
        }
    }
    public  String getUsedExternalMemorySize() {
        try{
            if (externalMemoryAvailable())
                return Convert_It(external_Total_Size-external_Available_Size);
            else {
                return "0.00 GB";
            }
        }catch (Exception ignored){
            return "0.00 GB";
        }
    }
    public  String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            StatFs stat = new StatFs(sd_Card_Path_URL);
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            external_Total_Size  =  totalBlocks*blockSize ;
            return Convert_It(external_Total_Size);
        } else {
            return "0.0 GB";
        }
    }

    public String getAvailableExternalPercentage(){
        return GetDecimal2Round(((double) (external_Available_Size * 100) / external_Total_Size));
    }
    public String getUsedExternalPercentage(){
        return GetDecimal2Round((((double) (external_Total_Size - external_Available_Size) * 100) / external_Total_Size));
    }

    public static String Convert_It(long size) {
        try {
            if (size > (1024 * 1024 * 1024)) {
                // GB
                return GetDecimal2Round((double) size / (1024 * 1024 * 1024)) + " GB";
            } else if (size > (1024 * 1024)) {
                // MB
                return GetDecimal2Round((double) size / (1024 * 1024)) + " MB";

            } else {
                // KB
                return GetDecimal2Round((double) size / (1024)) + " KB";
            }
        }catch (Exception e){
            return "";
        }

    }


//    public long getFileSizeInBytes(String fileName) {
//        long ret = 0;
//        try {
//        File f = new File(fileName);
//
//            if (f.exists()) {
//                if (f.isFile()) {
//                    return f.length();
//                } else if (f.isDirectory()) {
//                    File[] contents = f.listFiles();
//                    for (int i = 0; i < contents.length; i++) {
//                        if (contents[i].exists()) {
//                            if (contents[i].isFile()) {
//                                ret += contents[i].length();
//                            } else if (contents[i].isDirectory())
//                                ret += getFileSizeInBytes(contents[i].getPath());
//                        }
//                    }
//                }
//            } else {
//                ret = 0;
//            }
//        }catch (Exception ignored){
//        }
//        return ret;
//    }

    private static String GetDecimal2Round(double time) {
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.valueOf(df.format(time)).toString();
    }

    public long getInternal_Available_Size() {
        return internal_Available_Size;
    }

    public long getExternal_Available_Size() {
        return external_Available_Size;
    }

    public long getInternal_Total_Size() {
        return internal_Total_Size;
    }

    public long getExternal_Total_Size() {
        return external_Total_Size;
    }

    public String getAvialableInternalPercent() {
        return availableInternalPercent;
    }

}
