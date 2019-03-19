package com.example.sudoajay.duplication_data.SdCard;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.example.sudoajay.duplication_data.R;


/**
 * Created by sudoajay on 4/15/18.
 */

public class SdCardDialog extends DialogFragment {

    private AndroidSdCardPermission android_sdCard_permission;

    public SdCardDialog() {

    }

    @SuppressLint("ValidFragment")
    public SdCardDialog(AndroidSdCardPermission android_sdCard_permission) {
        this.android_sdCard_permission = android_sdCard_permission;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View rootview = inflater.inflate(R.layout.activity_custom_dialog_sd_select, container, false);

            Button continue_Button = rootview.findViewById(R.id.continue_Button);
            Button see_More_button = rootview.findViewById(R.id.see_More_button);

            continue_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    android_sdCard_permission.Storage_Access_FrameWork();

                    dismiss();

                }
            });

            see_More_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final String url = "https://developer.android.com/guide/topics/providers/document-provider.html";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } catch (Exception ignored) {

                    }
                }
            });

            setCancelable(false);
            return rootview;
        } catch (Exception e) {
            return null;

        }


    }

    public void onStart() {
        // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
