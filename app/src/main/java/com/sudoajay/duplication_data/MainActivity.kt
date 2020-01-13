package com.sudoajay.duplication_data

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.sudoajay.duplication_data.backgroundProcess.WorkMangerTaskManager
import com.sudoajay.duplication_data.customDialog.CustomDialogForBackgroundTimer
import com.sudoajay.duplication_data.customDialog.CustomDialogForForegroundService
import com.sudoajay.duplication_data.foregroundService.Foreground
import com.sudoajay.duplication_data.foregroundService.ForegroundDialog
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.mainFragments.Home
import com.sudoajay.duplication_data.mainFragments.Scan
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sdCard.SdCardPath
import com.sudoajay.duplication_data.sharedPreferences.ExternalPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // global variable
    private var fragment: Fragment? = null
    private var home: Home? = null
    private var scan: Scan? = null
    private var doubleBackToExitPressedOnce = false
    private var externalSharedPreferences: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null
    private var toolbar: Toolbar? = null
    private var drawer: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var androidSdCardPermission: AndroidSdCardPermission? = null
    private val ratingLink = "https://play.google.com/store/apps/details?id=com.sudoajay.duplication_data"
    private var traceBackgroundService: TraceBackgroundService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_navigation)

        // get data from intent
        val extras = intent.extras
        extras?.getString("passing")
        // Reference and Create Object
        reference()
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle)
        toggle.syncState()
        navigationView!!.setNavigationItemSelectedListener(this)
        // scan screen
        title = "Scan"
        navigationView!!.menu.getItem(1).isChecked = true
        onNavigationItemSelected(navigationView!!.menu.getItem(1))
        if (intent.action != null) {
            if (intent.action.equals("Stop_Foreground(Setting)", ignoreCase = true)) {
                navigationView!!.menu.getItem(1).isChecked = true
                onNavigationItemSelected(navigationView!!.menu.getItem(3))
            }
        }

        externalSharedPreferences = ExternalPathSharedPreference(applicationContext)
        sdCardPathSharedPreference = SdCardPathSharedPreference(applicationContext)
        //        // call Background
        task()
        traceBackgroundService = TraceBackgroundService(applicationContext)
        if (traceBackgroundService!!.isBackgroundServiceWorking) {
            traceBackgroundService!!.isBackgroundWorking
        }
        if (!traceBackgroundService!!.isBackgroundServiceWorking) { // if the background service not working then
            traceBackgroundService!!.setTaskA()
            traceBackgroundService!!.taskB = TraceBackgroundService.nextDate(24)
        }
        //    first time check
        if (!traceBackgroundService!!.isBackgroundServiceWorking) {
            if (traceBackgroundService!!.isForegroundServiceWorking) {
                if (!isServiceRunningInForeground(applicationContext, Foreground::class.java)) {
                    val foregroundService = ForegroundDialog(this@MainActivity,
                            this@MainActivity)
                    foregroundService.callThread()
                }
            }
        }
    }

    // Reference and Create Object
    private fun reference() { //reference
        toolbar = findViewById(R.id.toolbar)
        drawer = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        // create object
        home = Home()
        scan = Scan()
        // check SDCard Storage Permission
        androidSdCardPermission = AndroidSdCardPermission(applicationContext, this@MainActivity)
    }

    // on click listener
    fun onClick(v: View) {
        when (v.id) {
            R.id.duplicateDataImageView, R.id.duplicateDataTextView -> {
                // default Home
                title = "Home"
                navigationView!!.menu.getItem(0).isChecked = true
                onNavigationItemSelected(navigationView!!.menu.getItem(0))
            }
        }
        if (visibleFragment == home) {
            home!!.onClick(v)
        } else if (visibleFragment == scan) {
            scan!!.onClick(v)
        }
    }

    private val visibleFragment: Fragment?
        get() {
            val fragmentManager = this@MainActivity.supportFragmentManager
            val fragments = fragmentManager.fragments
            if (fragment != null && fragment!!.isVisible) {
                for (fragment in fragments) {
                    if (fragment != null && fragment.isVisible) return fragment
                }
            }
            return null
        }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) { // If request is cancelled, the result arrays are empty.
            if (!(grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission denied, boo! Disable the functionality that depends on this permission.
                CustomToast.toastIt(applicationContext, getString(R.string.giveUsPermission))
            } else {
                val externalPathSharedPreference = ExternalPathSharedPreference(applicationContext)
                externalPathSharedPreference.externalPath = AndroidExternalStoragePermission.getExternalPath(applicationContext)
            }

        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) { // local variable
        super.onActivityResult(requestCode, resultCode, resultData)
        val sdCardPathURL: String?
        val stringURI: String
        val spiltPart: String?

        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == 42 || requestCode == 58) {
            val sdCardURL: Uri? = resultData!!.data

            grantUriPermission(this@MainActivity.packageName, sdCardURL, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this@MainActivity.contentResolver.takePersistableUriPermission(sdCardURL!!, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            sdCardPathURL = SdCardPath.getFullPathFromTreeUri(sdCardURL, this@MainActivity)
            stringURI = sdCardURL.toString()

            // Its supports till android 9 & api 28
            if (requestCode == 42) {
                spiltPart = "%3A"
                sdCardPathSharedPreference!!.sdCardPath = spiltThePath(stringURI, sdCardPathURL)
                sdCardPathSharedPreference!!.stringURI = spiltUri(stringURI, spiltPart)
                if (!androidSdCardPermission!!.isSdStorageWritable) {
                    CustomToast.toastIt(applicationContext, resources.getString(R.string.wrongDirectorySelected))
                    return
                }

            } else {
                val realExternalPath = AndroidExternalStoragePermission.getExternalPath(applicationContext).toString()
                if (realExternalPath in sdCardPathURL.toString() + "/") {
                    spiltPart = "primary%3A"
                    externalSharedPreferences!!.externalPath = realExternalPath
                    externalSharedPreferences!!.stringURI = spiltUri(stringURI, spiltPart)
                } else {
                    CustomToast.toastIt(applicationContext, getString(R.string.wrongDirectorySelected))
                    return
                }


            }
            // refresh when you get sd card path & External Path
        } else {
            CustomToast.toastIt(applicationContext, getString(R.string.reportIt))
        }
    }

    private fun spiltUri(uri: String, spiltPart: String): String {
        return uri.split(spiltPart)[0] + spiltPart
    }

    private fun spiltThePath(url: String, path: String?): String {

        val spilt = url.split("%3A")
        val getPaths = spilt[0].split("/")
        val specificName = getPaths[getPaths.size - 1] + "/"
        val paths: List<String>
        paths = path!!.split(getPaths[getPaths.size - 1])
        return paths[0] + specificName

    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            onBack()
        }
    }

    private fun onBack() {
        if (doubleBackToExitPressedOnce) {
            closeApp()
            return
        }
        doubleBackToExitPressedOnce = true
        CustomToast.toastIt(applicationContext, "Click Back Again To Exit")
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun closeApp() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_Home -> { // Handle the Home Action
                title = "Home"
                fragment = home!!.createInstance(this@MainActivity)
            }
            R.id.nav_Scan -> { // Handle the Scan Action
                title = "Scan"
                fragment = scan!!.createInstance(this@MainActivity)
            }
            R.id.nav_background_Timer -> callCustomDailogBackgroundTimer()

            R.id.nav_Foreground_Setting -> callCustomDailogForeground()

            R.id.nav_Share -> share()

            R.id.nav_Rate_Us -> rateUs()

            R.id.nav_More_Apps->openMoreApp()

            R.id.nav_Send_Feedback -> openEmail()

            R.id.nav_Help -> CustomToast.toastIt(applicationContext, "Coming Soon")

        }
        replaceFragments()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    // Replace Fragments
    private fun replaceFragments() {
        if (fragment != null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.frame_Layout, fragment!!)
            ft.commit()
        }
    }

    private fun task() { // set the Task is started
        val myWorkBuilder = PeriodicWorkRequest.Builder(WorkMangerTaskManager::class.java, 12, TimeUnit.HOURS)
        val myWork = myWorkBuilder.build()
        WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork("Manages", ExistingPeriodicWorkPolicy.KEEP, myWork)
    }

    private fun callCustomDailogBackgroundTimer() {
        val ft = supportFragmentManager.beginTransaction()
        val customDialogForBackgroundTimer = CustomDialogForBackgroundTimer()
        customDialogForBackgroundTimer.show(ft, "dialog")
    }

    private fun callCustomDailogForeground() {
        val ft = supportFragmentManager.beginTransaction()
        val customDialogForChangesOptions = CustomDialogForForegroundService()
        customDialogForChangesOptions.show(ft, "dialog")
    }

    private fun share() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Link-Share")
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage) + " - git " + ratingLink)
        startActivity(Intent.createChooser(i, "Share via"))
    }

    private fun rateUs() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(ratingLink)
        startActivity(i)
    }
    private fun openMoreApp() {
        val link = "https://play.google.com/store/apps/dev?id=5309601131127361849"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(link)
        startActivity(i)
    }

    private fun openEmail() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "devsudoajay@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(intent)
        } catch (e: Exception) {
            CustomToast.toastIt(applicationContext, "There is no Email App")
        }
    }

    private fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
        return try {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            !servicesWorking()
        }
    }

    private fun servicesWorking(): Boolean {
        traceBackgroundService!!.isBackgroundWorking
        return !traceBackgroundService!!.isBackgroundServiceWorking
    }
}