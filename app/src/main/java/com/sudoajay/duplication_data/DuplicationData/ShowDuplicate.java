package com.sudoajay.duplication_data.DuplicationData;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.sudoajay.duplication_data.AdFolder.InterstitialAds;
import com.sudoajay.duplication_data.BuildConfig;
import com.sudoajay.duplication_data.Delete.DeleteData;
import com.sudoajay.duplication_data.MainActivity;
import com.sudoajay.duplication_data.Notification.NotifyNotification;
import com.sudoajay.duplication_data.Permission.NotificationPermissionCheck;
import com.sudoajay.duplication_data.R;
import com.sudoajay.duplication_data.StorageStats.StorageInfo;
import com.sudoajay.duplication_data.Toast.CustomToast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class ShowDuplicate extends AppCompatActivity {
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private List<Integer> arrow_Image_Resource = new ArrayList<>();
    private ExpandableDuplicateListAdapter expandableduplicatelistadapter;
    private List<String> list_Header = new ArrayList<>(), sets = new ArrayList<>();
    private LinkedHashMap<String, List<String>> list_Header_Child = new LinkedHashMap<>();
    @SuppressLint("UseSparseArrays")
    private LinkedHashMap<Integer, List<Boolean>> checkBoxArray = new LinkedHashMap<>();
    private List<Boolean> setsBoolean = new ArrayList<>();
    private Button deleteDuplicateButton;
    private View deleteDuplicateButton1;
    private RemoteViews contentView;
    private long total_Size,totalCount=0;
    private ImageView refreshImage_View;
    private MultiThreadingTask multiThreadingtask;
    private NotificationPermissionCheck notificationPermissionCheck;
    private Notification notification;
    private NotificationManager notificationManager;
    private List<String> unnecessaryList ;
    private ConstraintLayout nothingToShow_ConstraintsLayout;
    private ArrayList<String> Data;
    private InterstitialAds interstitialAds;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_duplicate);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reference();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        Data = bundle.getStringArrayList("Duplication_Class_Data");
        int i = 0;
        assert Data != null;
        if (Data.isEmpty()) {
            deleteDuplicateButton.setVisibility(View.INVISIBLE);
            deleteDuplicateButton1.setVisibility(View.INVISIBLE);
            nothingToShow_ConstraintsLayout.setVisibility(View.VISIBLE);

        } else {

            OnRefresh(true);
        }

        expandableduplicatelistadapter = new ExpandableDuplicateListAdapter(this, list_Header, list_Header_Child, arrow_Image_Resource
                , checkBoxArray);
        expandableListView.setAdapter(expandableduplicatelistadapter);

        for (i = 0; i < list_Header.size(); i++) {
            expandableListView.collapseGroup(i);
            for (int j = 0; j < Objects.requireNonNull(list_Header_Child.get(list_Header.get(i))).size(); j++) {
                total_Size += new File(Objects.requireNonNull(list_Header_Child.get(list_Header.get(i))).get(j)).length();
            }

        }

        deleteDuplicateButton.setText("Delete (" + StorageInfo.Convert_It(total_Size) + ")");
        // Listview Group click listener
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return false;
            }
        });
        // Listview Group expanded listener
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                arrow_Image_Resource.set(groupPosition, R.drawable.arrow_up_icon);

            }
        });

        // Listview Group collasped listener
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                arrow_Image_Resource.set(groupPosition, R.drawable.arrow_down_icon);
            }
        });

        // Listview on child click listener
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub

                open_With(new File(Objects.requireNonNull(list_Header_Child.get(list_Header.get(groupPosition))).get(childPosition)));
                expandableduplicatelistadapter.getChildView(groupPosition, childPosition, false, v, parent);

                return true;
            }

        });
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return false;
            }
        });
        expandableListView.invalidate();

        interstitialAds = new InterstitialAds(getApplicationContext(), 1);
    }


    private void reference() {

        // reference
        deleteDuplicateButton = findViewById(R.id.deleteDuplicateButton);
        nothingToShow_ConstraintsLayout = findViewById(R.id.nothingToShow_ConstraintsLayout);
        refreshImage_View = findViewById(R.id.refreshImage_View);
        expandableListView = findViewById(R.id.duplicateExpandableListView);
        deleteDuplicateButton1 = findViewById(R.id.deleteDuplicateButton1);


        // create object
        multiThreadingtask = new MultiThreadingTask();
        notificationPermissionCheck = new NotificationPermissionCheck(ShowDuplicate.this);

        // add unnecessary data
        String whatsapp_Path = "/WhatsApp/";
        unnecessaryList = new ArrayList<>();
        unnecessaryList.add(whatsapp_Path + ".Shared/");
        unnecessaryList.add(whatsapp_Path + ".Trash/");
        unnecessaryList.add(whatsapp_Path + "cache/");
        unnecessaryList.add(whatsapp_Path + "Theme/");
        unnecessaryList.add(whatsapp_Path + ".Thumbs/");
        unnecessaryList.add(whatsapp_Path + "Databases");
        unnecessaryList.add("/Android/data/");


    }

    public void OnClick(final View v) {

        switch (v.getId()) {
            case R.id.backImageView:
                onBackPressed();
                break;
            case R.id.shareImageView:
                Share();
                break;
            case R.id.refreshImage_View:
                if (refreshImage_View.getRotation() % 360 == 0) {
                    refreshImage_View.animate().rotationBy(360f).setDuration(1000);
                    OnRefresh(false);
                    expandableListView.invalidate();
                }
                break;
            case R.id.deleteDuplicateButton:
            case R.id.deleteDuplicateButton1:
                if (!notificationPermissionCheck.check_Notification_Permission()) {
                    notificationPermissionCheck.Custom_AertDialog();
                } else {
                    Call_Custom_Dailog("   Are You Sure To Delete ?");
                }

                break;

        }
    }

    private void OnRefresh(final boolean whenStart) {
        int i;
        if (!whenStart) {

            for (i = 0; i < list_Header.size(); i++) {
                expandableListView.collapseGroup(i);
            }

            arrow_Image_Resource.clear();
            list_Header_Child.clear();
            list_Header.clear();
            setsBoolean.clear();
            checkBoxArray.clear();
        }
        i = 0;

        for (String get : Data) {

            if (get.equalsIgnoreCase("And")) {
                if (!sets.isEmpty()) {

                    list_Header.add("Group " + (i + 1));
                    arrow_Image_Resource.add(R.drawable.arrow_up_icon);

                    list_Header_Child.put(list_Header.get(i), new ArrayList<>(sets));
                    checkBoxArray.put(i, new ArrayList<>(setsBoolean));
                    i++;
                    sets.clear();
                    setsBoolean.clear();
                }
            } else {
                sets.add(get);
                if (setsBoolean.size() == 0 && !IsMatchUnnecessary(get)) {
                    setsBoolean.add(false);
                } else {
                    setsBoolean.add(true);
                }
            }


        }

    }
    private boolean IsMatchUnnecessary(final String path){

        for(String gets:unnecessaryList){
            if(path.contains(gets)) return true;
        }
        return false;
    }
    public void open_With(File file) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(Objects.requireNonNull(fileExt(file.getAbsolutePath())).substring(1));
        Uri URI = FileProvider.getUriForFile(getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        newIntent.setDataAndType(URI, mimeType);
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            this.startActivity(newIntent);
        } catch (Exception e) {
            CustomToast.ToastIt(getApplicationContext(), "No handler for this type of file_icon.");
        }
    }

    private String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public void Call_Custom_Dailog(String Message) {

        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.activity_custom_dialog);
        TextView text_Message = dialog.findViewById(R.id.text_Message);
        text_Message.setText(Message);
        TextView button_No = dialog.findViewById(R.id.button_No);
        TextView button_Yes = dialog.findViewById(R.id.button_Yes);
        // if button is clicked, close the custom dialog

        button_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAds();
                multiThreadingtask.execute();

                dialog.dismiss();
            }
        });
        button_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void OpenAds() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (interstitialAds.isLoaded())
                    interstitialAds.getmInterstitialAd().show();
            }
        }, 3000);
    }

    public void SendBack() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("passing", "Duplication");
        startActivity(intent);

    }

    @SuppressLint("StaticFieldLeak")
    public class MultiThreadingTask extends AsyncTask<String, String, String> {
        int progress = 0;

        @Override
        protected void onPreExecute() {
            AlertDialog alertDialog = new SpotsDialog.Builder()
                    .setContext(ShowDuplicate.this)
                    .setMessage("Delete....")
                    .setCancelable(false)
                    .setTheme(R.style.Custom)
                    .build();

            alertDialog.show();
            SendBack();
            CustomToast.ToastIt(getApplicationContext(), "Deletion");

            totalCount =0 ;
            for (Map.Entry<Integer, List<Boolean>> entry : expandableduplicatelistadapter.getCheckBoxArray().entrySet()) {
                for (Boolean checked : entry.getValue()) {
                    if(checked) totalCount++;
                   }
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            call_Thread();

            super.onPostExecute(s);
        }

        @Override
        public void onProgressUpdate(String... values) {
            progress++;
            contentView.setTextViewText(R.id.size_Title, progress + "/" + totalCount);
            contentView.setTextViewText(R.id.percent_Text, ((progress * 100) / totalCount) + "%");
            contentView.setTextViewText(R.id.time_Tittle, get_Current_Time());
            contentView.setProgressBar(R.id.progressBar, (int)totalCount, progress, false);
            notificationManager.notify(1, notification);
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... strings) {
            Notification();
         new DeleteData(ShowDuplicate.this, list_Header_Child,
                 expandableduplicatelistadapter.getCheckBoxArray(), multiThreadingtask);
            return null;
        }
    }



    public static String Convert_It(long size) {
        if (size > (1024 * 1024 * 1024)) {
            // GB
            return Convert_To_Decimal((float) size / (1024 * 1024 * 1024)) + " GB";
        } else if (size > (1024 * 1024)) {
            // MB
            return Convert_To_Decimal((float) size / (1024 * 1024)) + " MB";

        } else {
            // KB
            return Convert_To_Decimal((float) size / (1024)) + " KB";
        }

    }

    public static String Convert_To_Decimal(float value) {
        String size = value + "";
        if (value >= 1000) {
            return size.substring(0, 4);
        } else if (value >= 100) {
            return size.substring(0, 3);
        } else {
            if (size.length() == 2 || size.length() == 3) {
                return size.substring(0, 1);
            }
            return size.substring(0, 4);

        }

    }

    public void call_Thread() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(1);
                NotifyNotification notifyNotification = new NotifyNotification(getApplicationContext());
                notifyNotification.notify("You Have Saved " + Convert_It(total_Size) + " Of Data ", getResources().getString(R.string.delete_Done_title));
                CustomToast.ToastIt(getApplicationContext(), "Successfully Data Deleted");

            }
        }, 2000);
    }


    public void Notification() {
        String id = this.getString(R.string.duplicate_Id); // default_channel_id
        String title = this.getString(R.string.duplicate_title); // Default Channel
        NotificationCompat.Builder mBuilder;

        contentView = new RemoteViews(getPackageName(), R.layout.activity_custom_notification);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        contentView.setTextViewText(R.id.title, "Deletion...");
        contentView.setTextViewText(R.id.time_Tittle, get_Current_Time());
        contentView.setProgressBar(R.id.progressBar, 100, 0, false);
        contentView.setTextViewText(R.id.size_Title, "0/" + totalCount);
        contentView.setTextViewText(R.id.percent_Text, "00%");

        if (notificationManager == null) {
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            assert notificationManager != null;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
        mBuilder = new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.mipmap.ic_launcher)   // required
                .setContent(contentView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setLights(Color.parseColor("#075e54"), 3000, 3000);

        notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(1, notification);
    }

    public String get_Current_Time() {

        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        if (hours < 12) {
            return hours + ":" + minutes + " AM";
        } else {

            return (hours - 12) + ":" + minutes + " PM";
        }
    }

    private void Share() {
        String rating_link = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover";
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Link-Share");
        i.putExtra(android.content.Intent.EXTRA_TEXT, R.string.share_info + rating_link);
        startActivity(Intent.createChooser(i, "Share via"));
    }


}
