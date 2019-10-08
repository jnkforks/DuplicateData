package com.sudoajay.duplication_data.sharedPreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.sudoajay.duplication_data.R;

/**
 * Created by Lincoln on 05/05/16.
 */
public class BackgroundProcess {
    private SharedPreferences.Editor editor;
    private Context _context;


    @SuppressLint("CommitPrefEdits")
    public BackgroundProcess(final Context context) {
        this._context = context;
        // shared pref mode
        int PRIVATE_MODE = 0;
        SharedPreferences pref = _context.getSharedPreferences(context.getString(R.string.MY_PREFS_NAME), PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setTaskCDone(final boolean task) {
        editor.putBoolean(_context.getString(R.string.task_C_Done), task);
        editor.apply();
    }


}
