package com.sudoajay.duplication_data.Permission;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.NotificationManagerCompat;
import android.view.View;
import android.widget.TextView;

import com.sudoajay.duplication_data.R;

public class NotificationPermissionCheck {


    private Activity activity;

    public NotificationPermissionCheck(final Activity activity) {
        this.activity = activity;
    }


    private void Open_Setting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public boolean check_Notification_Permission() {
        return NotificationManagerCompat.from(activity.getApplicationContext()).areNotificationsEnabled();
    }

    public void Custom_AertDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_custom_notification_permission);
        TextView button_No = dialog.findViewById(R.id.no_button);
        TextView button_Yes = dialog.findViewById(R.id.yes_Button);
        // if button is clicked, close the custom dialog

        button_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Open_Setting();
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
}
