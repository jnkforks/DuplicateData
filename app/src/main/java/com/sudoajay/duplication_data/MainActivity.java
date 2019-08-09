package com.sudoajay.duplication_data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.navigation.NavigationView;
import com.sudoajay.duplication_data.BackgroundProcess.WorkMangerTaskManager;
import com.sudoajay.duplication_data.Custom_Dialog.CustomDialogForBackgroundTimer;
import com.sudoajay.duplication_data.Custom_Dialog.CustomDialogForForegroundService;
import com.sudoajay.duplication_data.ForegroundService.Foreground;
import com.sudoajay.duplication_data.ForegroundService.ForegroundDialog;
import com.sudoajay.duplication_data.MainFragments.Home;
import com.sudoajay.duplication_data.MainFragments.Scan;
import com.sudoajay.duplication_data.Permission.AndroidExternalStoragePermission;
import com.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.sudoajay.duplication_data.SdCard.SdCardPath;
import com.sudoajay.duplication_data.Toast.CustomToast;
import com.sudoajay.duplication_data.sharedPreferences.PrefManager;
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // global variable
    private Fragment fragment;
    private Home home;
    private Scan scan;
    private boolean doubleBackToExitPressedOnce;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AndroidSdCardPermission androidSdCardPermission;
    private AndroidExternalStoragePermission androidExternalStoragePermission;
    private final String rating_link = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover";
    private TraceBackgroundService traceBackgroundService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_navigation);

        // local variable
        String value = "Scan";
        // get data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("passing");
        }

        // Reference and Create Object
        Reference();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        if (value == null) {
//             default Home
            setTitle("Home");
            navigationView.getMenu().getItem(0).setChecked(true);
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        } else {
            // scan screen
            setTitle("Scan");
            navigationView.getMenu().getItem(1).setChecked(true);
            onNavigationItemSelected(navigationView.getMenu().getItem(1));
        }

        if (getIntent().getAction() != null) {
            if (Objects.requireNonNull(getIntent().getAction()).equalsIgnoreCase("Stop_Foreground(Setting)")) {
                navigationView.getMenu().getItem(1).setChecked(true);
                onNavigationItemSelected(navigationView.getMenu().getItem(3));
            }

        }

        // check ExternalStorage Permission
        androidExternalStoragePermission = new AndroidExternalStoragePermission(MainActivity.this,
                MainActivity.this);
        androidExternalStoragePermission.call_Thread();


//        // call Background
        Task();

        traceBackgroundService = new TraceBackgroundService(getApplicationContext());
        PrefManager prefManager = new PrefManager(getApplicationContext());
        //    first time check
        if (!traceBackgroundService.isBackgroundServiceWorking()) {
            if (traceBackgroundService.isForegroundServiceWorking()) {
                if (!isServiceRunningInForeground(getApplicationContext(), Foreground.class)) {
                    ForegroundDialog foregroundService = new ForegroundDialog(MainActivity.this,
                            MainActivity.this);
                    foregroundService.call_Thread();

                }
            }
        }


    }

    // Reference and Create Object
    private void Reference() {

        //reference
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        // create object
        home = new Home();
        scan = new Scan();

        // check SDCard Storage Permission
        androidSdCardPermission = new AndroidSdCardPermission(getApplicationContext(), MainActivity.this, MainActivity.this);

    }

    // on click listener
    public void OnClick(View v) {

        switch (v.getId()) {
            case R.id.duplicateDataImageView:
            case R.id.duplicateDataTextView:
                // default Home
                setTitle("Home");
                navigationView.getMenu().getItem(0).setChecked(true);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
                break;
        }

        if (getVisibleFragment().equals(home)) {
            home.OnClick(v);
        } else if (getVisibleFragment().equals(scan)) {
            scan.OnClick(v);
        }


    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if ((fragment != null && fragment.isVisible())) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // local variable
        Uri sd_Card_URL;
        String sd_Card_Path_URL, string_URI = null;

        if (resultCode != RESULT_OK)
            return;
        sd_Card_URL = resultData.getData();
        grantUriPermission(getPackageName(), sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        assert sd_Card_URL != null;
        getContentResolver().takePersistableUriPermission(sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sd_Card_Path_URL = SdCardPath.getFullPathFromTreeUri(sd_Card_URL, MainActivity.this);
            string_URI = sd_Card_URL.toString();
            sd_Card_Path_URL = Spilit_The_Path(string_URI, sd_Card_Path_URL);

        if (!isSelectSdRootDirectory(sd_Card_URL.toString()) || isSamePath(sd_Card_Path_URL)) {
                CustomToast.ToastIt(getApplicationContext(), getResources().getString(R.string.errorMes));
                return;
            }
            androidSdCardPermission.setSd_Card_Path_URL(sd_Card_Path_URL);
            androidSdCardPermission.setString_URI(string_URI);
        }


    public boolean isSamePath(String sd_Card_Path_URL) {
        return androidExternalStoragePermission.getExternal_Path().equals(sd_Card_Path_URL);
    }

    private boolean isSelectSdRootDirectory(String path) {
        if (path.substring(path.length() - 3).equals("%3A")) return true;
        return false;

    }
    public String Spilit_The_Path(final String url,final String path) {
        String[] spilt = url.split("%3A");
        String[] getPaths = spilt[0].split("/");
        String[] paths = path.split(getPaths[getPaths.length-1]);
        return  paths[0]+getPaths[getPaths.length-1];
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            onBack();
        }
    }

    public void onBack() {
        if (doubleBackToExitPressedOnce) {
            Finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        CustomToast.ToastIt(getApplicationContext(), "Click Back Again To Exit");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void Finish() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_Home) {
            // Handle the Home Action
            setTitle("Home");
            fragment = home.createInstance(MainActivity.this);
        } else if (id == R.id.nav_Scan) {
            // Handle the Scan Action
            setTitle("Scan");
            fragment = scan.createInstance(MainActivity.this);
        }else if(id == R.id.nav_background_Timer){
            CallCustomDailogBackgroundTimer();


        }else if(id == R.id.nav_Foreground_Setting) {
            CallCustomDailogForeground();
        }

        else if (id == R.id.nav_Share) {
            Share();

        } else if (id == R.id.nav_Rate_Us) {
            Rate_Us();

        } else if (id == R.id.nav_Contact_Me) {
            Open_Email();
        }
        Replace_Fragments();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Replace Fragments
    public void Replace_Fragments() {

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_Layout, fragment);
            ft.commit();
        }
    }

    private void Task() {

        // set the Task is started
        PeriodicWorkRequest.Builder myWorkBuilder =
                new PeriodicWorkRequest.Builder(WorkMangerTaskManager.class, 12, TimeUnit.HOURS);

        PeriodicWorkRequest myWork = myWorkBuilder.build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("Manages", ExistingPeriodicWorkPolicy.KEEP, myWork);

    }


    public void CallCustomDailogBackgroundTimer() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CustomDialogForBackgroundTimer customDialogForBackgroundTimer
                = new CustomDialogForBackgroundTimer(this);
        customDialogForBackgroundTimer.show(ft, "dialog");
    }
    public void CallCustomDailogForeground() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CustomDialogForForegroundService custom_dialog_for_changes_options
                = new CustomDialogForForegroundService();
        custom_dialog_for_changes_options.show(ft, "dialog");
    }

    private void Share() {

        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Link-Share");
        i.putExtra(android.content.Intent.EXTRA_TEXT, R.string.share_info + rating_link);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    public void Rate_Us() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(rating_link));
        startActivity(i);
    }


    public void Open_Email() {

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "sudoajay@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(intent);
        } catch (Exception e) {
            CustomToast.ToastIt(getApplicationContext(), "There is no Email App");
        }

    }

    public boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            if (!ServicesWorking()) return true;
            return false;
        }
    }

    public boolean ServicesWorking() {
        traceBackgroundService.isBackgroundWorking();
        return !traceBackgroundService.isBackgroundServiceWorking();
    }

}
