package com.sudoajay.duplication_data.MainFragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.sudoajay.duplication_data.MainNavigation;
import com.sudoajay.duplication_data.Permission.AndroidExternalStoragePermission;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.R;
import com.sudoajay.duplication_data.StorageStats.StorageInfo;
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class Home extends Fragment {

    // global variable
    private MainNavigation main_navigation;
    private View layout;
    private CircularProgressBar circularProgressBarInternal, circularProgressBarExternal;
    private final int animationDuration = 4000;
    private TextView textViewInternal2, textViewInternal3,
            textViewUsedSpaceSizeInternal, textViewInternal4, textViewInternal5,
            textViewUsedSpaceSizeExternal, textViewExternal2, textViewExternal4,
            textViewExternal5, textViewExternal3;
    private StorageInfo storageInfo;
    private AndroidExternalStoragePermission externalStoragePermission;
    private AndroidSdCardPermission sdCardPermission;
    private View customToastLayout;
    private Toast toast;

    public Home() {
        // Required empty public constructor
    }

    public Home createInstance(MainNavigation main_navigation) {
        this.main_navigation = main_navigation;
        return this;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.main_fragment_home, container, false);

        // Reference and Create Object
        Reference();

        //Set Storage Stats
        SetInternalStorageStats();
        SetExternalStorageStats();

        LayoutInflater inflaters = getLayoutInflater();
        customToastLayout = inflaters.inflate(R.layout.activity_custom_toast,
                (ViewGroup) layout.findViewById(R.id.toastcustom));

        // custom progress bar
        circularProgressBarInternal.setProgressWithAnimation(Float.valueOf(storageInfo.getAvialableInternalPercent()), animationDuration); // Default duration = 1500ms
        circularProgressBarExternal.setProgressWithAnimation(Float.valueOf(storageInfo.getAvailableExternalPercentage()), animationDuration);

        return layout;
    }

    // Reference and Create Object
    private void Reference() {
        textViewUsedSpaceSizeInternal = layout.findViewById(R.id.textViewUsedSpaceSizeInternal);
        textViewInternal2 = layout.findViewById(R.id.textViewInternal2);
        textViewInternal3 = layout.findViewById(R.id.textViewInternal3);
        textViewInternal4 = layout.findViewById(R.id.textViewInternal4);
        textViewInternal5 = layout.findViewById(R.id.textViewInternal5);
        circularProgressBarInternal = layout.findViewById(R.id.circularProgressBarInternal);

        textViewExternal4 = layout.findViewById(R.id.textViewExternal4);
        textViewUsedSpaceSizeExternal = layout.findViewById(R.id.textViewUsedSpaceSizeExternal);
        textViewExternal2 = layout.findViewById(R.id.textViewExternal2);
        textViewExternal5 = layout.findViewById(R.id.textViewExternal5);
        textViewExternal3 = layout.findViewById(R.id.textViewExternal3);
        circularProgressBarExternal = layout.findViewById(R.id.circularProgressBarExternal);

        //Sd Card shared Preference
        SdCardPathSharedPreference sdCardPathSharedPreference = new SdCardPathSharedPreference(Objects.requireNonNull(getContext()));
        externalStoragePermission = new AndroidExternalStoragePermission(getContext(), main_navigation);
        sdCardPermission = new AndroidSdCardPermission(getContext(), main_navigation, main_navigation);
        // create object
        storageInfo = new StorageInfo(sdCardPathSharedPreference.getSdCardPath());
    }

    // on click listener
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.cardViewInternal3:
                if (externalStoragePermission.isExternalStorageWritable()) {
                    if (circularProgressBarInternal.getColor() == -13665742) {
                        circularProgressBarInternal.setBackgroundColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.progressBarColor));
                        circularProgressBarInternal.setColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.background_Progress_Bar_Color));
                        circularProgressBarInternal.setProgressWithAnimation(Float.valueOf(storageInfo.getUsedInternalPercent())
                                , 0); // Default duration = 1500ms

                        textViewInternal2.setText(StorageInfo.Convert_It((storageInfo.getInternal_Total_Size() -
                                storageInfo.getInternal_Available_Size())));

                        textViewInternal3.setText(getResources().getString(R.string.text_Used));

                    } else {
                        circularProgressBarInternal.setBackgroundColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.background_Progress_Bar_Color));
                        circularProgressBarInternal.setColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.progressBarColor));

                        circularProgressBarInternal.setProgressWithAnimation(Float.valueOf(storageInfo.getAvialableInternalPercent())
                                , 0); // Default duration = 1500ms
                        textViewInternal2.setText(StorageInfo.Convert_It(storageInfo.getInternal_Available_Size()));
                        textViewInternal3.setText(getResources().getString(R.string.text_Free));

                    }
                } else {
                    externalStoragePermission.call_Thread();
                }
                break;
            case R.id.cardViewExternal3:
                if (sdCardPermission.isSdStorageWritable()) {
                    if (circularProgressBarExternal.getColor() == -13665742) {
                        circularProgressBarExternal.setBackgroundColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.progressBarColor));
                        circularProgressBarExternal.setColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.background_Progress_Bar_Color));
                        circularProgressBarExternal.setProgressWithAnimation(Float.valueOf(storageInfo.getUsedExternalPercentage())
                                , 0); // Default duration = 1500ms

                        textViewExternal2.setText(StorageInfo.Convert_It((storageInfo.getExternal_Total_Size() -
                                storageInfo.getExternal_Available_Size())));

                        textViewExternal3.setText(getResources().getString(R.string.text_Used));

                    } else {
                        circularProgressBarExternal.setBackgroundColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.background_Progress_Bar_Color));
                        circularProgressBarExternal.setColor(ContextCompat.getColor
                                (Objects.requireNonNull(getContext()), R.color.progressBarColor));

                        circularProgressBarExternal.setProgressWithAnimation(Float.valueOf(storageInfo.getAvailableExternalPercentage())
                                , 0); // Default duration = 1500ms
                        textViewExternal2.setText(StorageInfo.Convert_It(storageInfo.getExternal_Available_Size()));
                        textViewExternal3.setText(getResources().getString(R.string.text_Free));

                    }
                } else {
                    Toast_It("Select the SD Card");
                    sdCardPermission.call_Thread();
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void SetInternalStorageStats() {
        String totalInternal = storageInfo.getTotalInternalMemorySize(),
                availableInternal = storageInfo.getUsedInternalMemorySize(),
                availableInternalPercent = storageInfo.getUsedInternalPercentage(),
                usedInternalPercent = storageInfo.getAvailableInternalPercentage();

        // set text to used spaced size
        textViewUsedSpaceSizeInternal.setText(availableInternal + " / " + totalInternal);

        // set text to internal
        textViewInternal2.setText(availableInternal);

        // set Text to used Percentage
        textViewInternal4.setText(availableInternalPercent + " % Used");
        textViewInternal5.setText(usedInternalPercent + " % Free");
    }

    @SuppressLint("SetTextI18n")
    private void SetExternalStorageStats() {
        String totalExternal = storageInfo.getTotalExternalMemorySize(),
                availableExternal = storageInfo.getUsedExternalMemorySize(),
                availableExternalPercent = storageInfo.getUsedExternalPercentage(),
                usedExternalPercent = storageInfo.getAvailableExternalPercentage();

        // set text to used spaced size
        textViewUsedSpaceSizeExternal.setText(availableExternal + " / " + totalExternal);

        // set text to internal
        textViewExternal2.setText(availableExternal);

        // set Text to used Percentage
        textViewExternal4.setText(availableExternalPercent + " % Used");
        textViewExternal5.setText(usedExternalPercent + " % Free");
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
}

