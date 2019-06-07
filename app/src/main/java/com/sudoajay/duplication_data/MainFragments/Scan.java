package com.sudoajay.duplication_data.MainFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sudoajay.duplication_data.DuplicationData.ScanDuplicateData;
import com.sudoajay.duplication_data.DuplicationData.ShowDuplicate;
import com.sudoajay.duplication_data.MainNavigation;
import com.sudoajay.duplication_data.Permission.AndroidExternalStoragePermission;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.R;
import com.sudoajay.duplication_data.SdCard.SdCardPath;
import com.sudoajay.duplication_data.StorageStats.StorageInfo;
import com.sudoajay.duplication_data.Toast.CustomToast;

import java.io.File;

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class Scan extends Fragment {

    // global variable
    private MainNavigation main_navigation;
    private View layout;
    private ImageView internal_Check, external_Check;
    private AndroidExternalStoragePermission androidExternalStoragePermission;
    private AndroidSdCardPermission androidSdCardPermission;
    private StorageInfo storageInfo;
    private Button file_Size_Text;
    private long totalSizeLong;
    private View customToastLayout;
    private Toast toast;

    public Scan() {
        // Required empty public constructor
    }

    public Scan createInstance(MainNavigation main_navigation) {
        this.main_navigation = main_navigation;
        return this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.main_fragment_scan, container, false);


        LayoutInflater inflaters = getLayoutInflater();
        customToastLayout = inflaters.inflate(R.layout.activity_custom_toast,
                (ViewGroup) layout.findViewById(R.id.toastcustom));

        // Reference and Create Object
        Reference();

        // check if internal and sd card is write able
        isWritable();

        return layout;
    }

    // Reference and Create Object
    private void Reference() {

        internal_Check = layout.findViewById(R.id.internal_Check);
        external_Check = layout.findViewById(R.id.external_Check);
        file_Size_Text = layout.findViewById(R.id.file_Size_Text);


        // create object
        androidExternalStoragePermission = new AndroidExternalStoragePermission(getContext(), getActivity());

        androidSdCardPermission = new AndroidSdCardPermission(getContext(), Scan.this, getActivity());

        storageInfo = new StorageInfo(androidSdCardPermission.getSd_Card_Path_URL());

    }

    @SuppressLint("SetTextI18n")
    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.scan_Button:
            case R.id.scan_Button1:

                // if nothing check
                if (internal_Check.getVisibility() == View.GONE &&
                        external_Check.getVisibility() == View.GONE)
                    Toast_It("You Supposed To Select Something");
                else {
                    try {
                        MultiThreadingTask multiThreadingTask = new MultiThreadingTask();
                        multiThreadingTask.execute();
                    } catch (Exception ignored) {
                    }

                }
                break;

            case R.id.internal_Image_View:
            case R.id.internal_Text_View:
            case R.id.internal_Check:

                // call this method to get size of data
                storageInfo.getTotalInternalMemorySize();

                if (internal_Check.getVisibility() == View.GONE) {
                    if (androidExternalStoragePermission.isExternalStorageWritable()) {
                        internal_Check.setVisibility(View.VISIBLE);
                        totalSizeLong += storageInfo.getInternal_Total_Size() - storageInfo.getInternal_Available_Size();
                    } else {

                        androidExternalStoragePermission.call_Thread();
                    }
                } else {
                    internal_Check.setVisibility(View.GONE);
                    totalSizeLong -= storageInfo.getInternal_Total_Size() - storageInfo.getInternal_Available_Size();
                }
                break;

            case R.id.external_Image_View:
            case R.id.external_Text_View:
            case R.id.external_Check:

                // call this method to get size of data
                storageInfo.getTotalExternalMemorySize();

                if (external_Check.getVisibility() == View.GONE) {
                    if (androidSdCardPermission.isSdStorageWritable()) {
                        external_Check.setVisibility(View.VISIBLE);

                        totalSizeLong += storageInfo.getExternal_Total_Size() - storageInfo.getExternal_Available_Size();
                    } else {
                        Toast_It("Select the SD Card");
                        androidSdCardPermission.call_Thread();
                    }
                } else {
                    external_Check.setVisibility(View.GONE);
                    totalSizeLong -= storageInfo.getExternal_Total_Size() - storageInfo.getExternal_Available_Size();
                }
                break;
        }

        file_Size_Text.setText(getResources().getString(R.string.file_Size_Text).substring(0, 12) +
                StorageInfo.Convert_It(totalSizeLong));
    }

    @SuppressLint("SetTextI18n")
    private void isWritable() {
        totalSizeLong = 0;
        if (!androidExternalStoragePermission.isExternalStorageWritable()) {
            internal_Check.setVisibility(View.GONE);
        } else {
            storageInfo.getTotalInternalMemorySize();
            totalSizeLong += storageInfo.getInternal_Total_Size() - storageInfo.getInternal_Available_Size();
        }
        if (!androidSdCardPermission.isSdStorageWritable()) {
            external_Check.setVisibility(View.GONE);
        } else {
            storageInfo.getTotalExternalMemorySize();
            totalSizeLong += storageInfo.getExternal_Total_Size() - storageInfo.getExternal_Available_Size();
        }
        file_Size_Text.setText(getResources().getString(R.string.file_Size_Text).substring(0, 12) +
                StorageInfo.Convert_It(totalSizeLong));
    }

    public void Toast_It(String message) {
        TextView toast_TextView = customToastLayout.findViewById(R.id.text);
        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast = new Toast(main_navigation.getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(customToastLayout);
            toast_TextView.setText(message);
            toast.show();
        } else {
            toast.cancel();
        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // local variable
        Uri sd_Card_URL ;
        String sd_Card_Path_URL, string_URI = null;

        if (resultCode != Activity.RESULT_OK)
            return;
        sd_Card_URL = data.getData();
        main_navigation.grantUriPermission(main_navigation.getPackageName(), sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        assert sd_Card_URL != null;
        main_navigation.getContentResolver().takePersistableUriPermission(sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sd_Card_Path_URL = SdCardPath.getFullPathFromTreeUri(sd_Card_URL, main_navigation);
        if (!isSamePath(sd_Card_Path_URL)) {
            string_URI = sd_Card_URL.toString();
            sd_Card_Path_URL = Spilit_The_Path(string_URI, sd_Card_Path_URL);

            if (!isSelectSdRootDirectory(sd_Card_URL.toString()) || !new File(sd_Card_Path_URL).exists()) {
                CustomToast.ToastIt(getContext(), getResources().getString(R.string.errorMes));
                return;
            }
            androidSdCardPermission.setSd_Card_Path_URL(sd_Card_Path_URL);
            androidSdCardPermission.setString_URI(string_URI);
        }

    }
    public boolean isSamePath(String sd_Card_Path_URL) {
        return androidExternalStoragePermission.getExternal_Path().equals(sd_Card_Path_URL);
    }

    private boolean isSelectSdRootDirectory(String path) {
        if (path.substring(path.length() - 3).equals("%3A")) return true;
        return false;

    }

    public String Spilit_The_Path(final String url, final String path) {
        String[] spilt = url.split("%3A");
        String[] getPaths = spilt[0].split("/");
        String[] paths = path.split(getPaths[getPaths.length - 1]);
        return paths[0] + getPaths[getPaths.length - 1];
    }
    @SuppressLint("StaticFieldLeak")
    public class MultiThreadingTask extends AsyncTask<String, String, String> {

        private AlertDialog alertDialog;
        private ScanDuplicateData scanDuplicateData;

        @Override
        protected void onPreExecute() {
            alertDialog = new SpotsDialog.Builder()
                    .setContext(main_navigation)
                    .setMessage("Scanning...")
                    .setCancelable(false)
                    .setTheme(R.style.Custom)
                    .build();

            alertDialog.show();

            // create object
            scanDuplicateData = new ScanDuplicateData(getContext());

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            scanDuplicateData.Duplication(androidExternalStoragePermission.getExternal_Path(),
                    androidSdCardPermission.getSd_Card_Path_URL(),
                    internal_Check.getVisibility(), external_Check.getVisibility());
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            alertDialog.dismiss();

            Intent intent = new Intent(main_navigation, ShowDuplicate.class);
            intent.putExtra("Duplication_Class_Data", scanDuplicateData.getList());
            startActivity(intent);
            super.onPostExecute(s);


        }

    }
}

