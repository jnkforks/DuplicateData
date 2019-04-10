package com.example.sudoajay.duplication_data.DuplicationData;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoajay.duplication_data.BuildConfig;
import com.example.sudoajay.duplication_data.R;
import com.example.sudoajay.duplication_data.StorageStats.StorageInfo;
import com.example.sudoajay.duplication_data.Toast.CustomToast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class ShowDuplicate extends AppCompatActivity {
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private List<Integer> arrow_Image_Resource = new ArrayList<>();
    private Expandable_Duplicate_List_Adapter expandable_duplicate_list_adapter;
    private List<String> list_Header = new ArrayList<>(), sets = new ArrayList<>();
    private HashMap<String, List<String>> list_Header_Child = new LinkedHashMap<>();
    private Button deleteDuplicateButton;
    private TextView textViewNothing;
    private long total_Size;
    private ImageView refreshImage_View;
    private final String rating_link = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover";

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
        ArrayList<String> Data = bundle.getStringArrayList("Duplication_Class_Data");
        int i = 0;
        assert Data != null;
        if (Data.isEmpty()) {
            deleteDuplicateButton.setVisibility(View.INVISIBLE);
            textViewNothing.setVisibility(View.VISIBLE);

        } else {
            for (String get : Data) {
                if (get.equalsIgnoreCase("And")) {
                    i++;
                    list_Header.add("Group " + i);
                    arrow_Image_Resource.add(R.drawable.arrow_up_icon);
                }
            }
            i = 0;
            for (String get : Data) {
                if (get.equalsIgnoreCase("And")) {
                    list_Header_Child.put(list_Header.get(i), new ArrayList<>(sets));
                    i++;
                    sets.clear();
                } else {
                    sets.add(get);
                }
            }
        }
        expandable_duplicate_list_adapter = new Expandable_Duplicate_List_Adapter(this, list_Header, list_Header_Child, arrow_Image_Resource);
        expandableListView.setAdapter(expandable_duplicate_list_adapter);

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

                open_With(new File(list_Header_Child.get(list_Header.get(groupPosition)).get(childPosition)));
                expandable_duplicate_list_adapter.getChildView(groupPosition, childPosition, false, v, parent);

                return false;
            }

        });
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return false;
            }
        });
        expandableListView.invalidate();

    }

    private void reference() {

        // reference
        deleteDuplicateButton = findViewById(R.id.deleteDuplicateButton);
        textViewNothing = findViewById(R.id.textViewNothing);
        refreshImage_View = findViewById(R.id.refreshImage_View);
        expandableListView = findViewById(R.id.duplicateExpandableListView);

    }

    public void On_Click_Process(final View v) {
        switch (v.getId()) {
            case R.id.backImageView:
                onBackPressed();
                break;
            case R.id.shareImageView:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Link-Share");
                i.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.shareText) + rating_link);
                startActivity(Intent.createChooser(i, "Share via"));
                break;
            case R.id.refreshImage_View:
                if (refreshImage_View.getRotation() % 360 == 0)
                    refreshImage_View.animate().rotationBy(360f).setDuration(1000);
                break;

        }
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

}
