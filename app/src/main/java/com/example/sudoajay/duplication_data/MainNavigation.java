package com.example.sudoajay.duplication_data;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.sudoajay.duplication_data.MainFragments.Home;
import com.example.sudoajay.duplication_data.MainFragments.Scan;
import com.example.sudoajay.duplication_data.Permission.AndroidExternalStoragePermission;
import com.example.sudoajay.duplication_data.Permission.AndroidSdCardPermission;
import com.example.sudoajay.duplication_data.SdCard.SdCardPath;

import java.io.File;

public class MainNavigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // global variable
    private Fragment fragment;
    private Home home;
    private Scan scan;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AndroidSdCardPermission androidSdCardPermission;
    private AndroidExternalStoragePermission androidExternalStoragePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_navigation);

        // Reference and Create Object
        Reference();

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

//        // default Home
//        setTitle("Home");
//        navigationView.getMenu().getItem(0).setChecked(true);
//        onNavigationItemSelected(navigationView.getMenu().getItem(0));
        // default Home
        setTitle("Scan");
        navigationView.getMenu().getItem(1).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(1));

//        // check ExternalStorage Permission
        androidExternalStoragePermission = new AndroidExternalStoragePermission(MainNavigation.this,
                MainNavigation.this);
        androidExternalStoragePermission.call_Thread();
//
//        // check SDCard Storage Permission
        androidSdCardPermission = new AndroidSdCardPermission(MainNavigation.this, MainNavigation.this);
        androidSdCardPermission.call_Thread();
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

    }

    // on click listener
    public void OnClick(View v) {
        home.OnClick(v);
        scan.OnClick(v);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // local variable
        Uri sd_Card_URL;
        String sd_Card_Path_URL, string_URI = null;

        if (resultCode != RESULT_OK)
            return;
        sd_Card_URL = resultData.getData();
        grantUriPermission(getPackageName(), sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getContentResolver().takePersistableUriPermission(sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sd_Card_Path_URL = SdCardPath.getFullPathFromTreeUri(sd_Card_URL, MainNavigation.this);
        if (new File(sd_Card_Path_URL).exists())
            string_URI = Split_The_URI(sd_Card_URL.toString());
        androidSdCardPermission.setSd_Card_Path_URL(sd_Card_Path_URL);
        androidSdCardPermission.setString_URI(string_URI);

        File file = new File(sd_Card_Path_URL);
        if (!isSamePath(sd_Card_Path_URL) && file.exists()) {
            sd_Card_Path_URL = Split_The_URI(sd_Card_URL.toString());
            sd_Card_URL = Uri.parse(sd_Card_Path_URL);
        }
    }

    public boolean isSamePath(String sd_Card_Path_URL) {
        return androidExternalStoragePermission.getExternal_Path().equals(sd_Card_Path_URL);
    }

    public String Split_The_URI(String url) {
        String save[] = url.split("%3A");
        return save[0] + "%3A";
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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
            fragment = home.createInstance(MainNavigation.this);
        } else if (id == R.id.nav_Scan) {
            // Handle the Scan Action
            setTitle("Scan");
            fragment = scan.createInstance(MainNavigation.this);
        } else if (id == R.id.nav_Share) {

        } else if (id == R.id.nav_Rate_Us) {

        } else if (id == R.id.nav_Contact_Me) {

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
}
