package com.sudoajay.duplication_data.duplicationData

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sudoajay.duplication_data.BuildConfig
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.customDialog.DialogInformationData
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.helperClass.DocumentHelperClass
import com.sudoajay.duplication_data.helperClass.FileSize
import com.sudoajay.duplication_data.helperClass.FileUtils
import com.sudoajay.duplication_data.intentService.DeletingTask
import com.sudoajay.duplication_data.permission.NotificationPermissionCheck
import com.sudoajay.lodinganimation.LoadingAnimation
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ShowDuplicate : AppCompatActivity() {
    private var expandableListView: ExpandableListView? = null
    private var expandableduplicatelistadapter: ExpandableDuplicateListAdapter? = null
    private var listHeaderChild = LinkedHashMap<Int, List<String>>()
    private var deleteDuplicateButton: Button? = null
    private var deleteDuplicateButton1: View? = null
    private var totalSize: Long = 0
    private var refreshImageView: ImageView? = null
    private var notificationPermissionCheck: NotificationPermissionCheck? = null
    private var nothingToShowConstraintsLayout: ConstraintLayout? = null
    var internalCheck = 0
    var externalCheck = 0
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var onClickPath: String? = null
    private var fragmentHistoryBottomSheetOpenFile: LinearLayout? = null
    private var documentHelperClass: DocumentHelperClass? = null
    var loadingAnimation: LoadingAnimation? = null
    var scanDuplicateDataWithFile: ScanDuplicateDataWithFile? = null
    var scanDuplicateDataWithDoc: ScanDuplicateDataWithDoc? = null
    private var myReceiver: MyReceiver? = null
    private var progressDone = true
    private var multiThreadingTask2: MultiThreadingTask2? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_duplicate)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        setSupportActionBar(toolbar)
        val intent = intent
        if (intent != null) {
            if (getIntent().extras!!["internalCheck"] != null) {
                internalCheck = getIntent().extras!!.getInt("internalCheck")
                externalCheck = getIntent().extras!!.getInt("externalCheck")
            }
        }
        refreshEverything()
    }

    private fun refreshEverything() {
        //  Here use of DocumentFile in android 10 not File is using anymore
        if (Build.VERSION.SDK_INT <= 28) {
            scanDuplicateDataWithFile = ScanDuplicateDataWithFile(applicationContext)
        } else {
            scanDuplicateDataWithDoc = ScanDuplicateDataWithDoc(applicationContext)
        }

        MultiThreadingTask1(this@ShowDuplicate).execute()
    }

    private class MultiThreadingTask1
    internal constructor(private val showDuplicate: ShowDuplicate) : AsyncTask<String?, String?, String?>() {

        private var internalCheck = 0
        private var externalCheck = 0
        override fun onPreExecute() {
            internalCheck = showDuplicate.internalCheck
            externalCheck = showDuplicate.externalCheck
            showDuplicate.loadingAnimation!!.start()
            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: String?): String? {
            //  Here use of DocumentFile in android 10 not File is using anymore
            if (Build.VERSION.SDK_INT <= 28) {
                showDuplicate.scanDuplicateDataWithFile!!.duplication(internalCheck, externalCheck)
            } else {
                showDuplicate.scanDuplicateDataWithDoc!!.duplication(internalCheck, externalCheck)
            }
            return null
        }

        override fun onPostExecute(s: String?) {

            showDuplicate.valueTransfer()

            showDuplicate.afterLoading()

            super.onPostExecute(s)
        }
    }

    fun afterLoading() {
        reference()
        if (listHeaderChild.isEmpty()) {
            nothingToShowConstraintsLayout!!.visibility = View.VISIBLE
        } else {
            deleteDuplicateButton!!.visibility = View.VISIBLE
            deleteDuplicateButton1!!.visibility = View.VISIBLE
        }
        //  Here use of DocumentFile in android 10 not File is using anymore
        expandableduplicatelistadapter = if (Build.VERSION.SDK_INT <= 28) {
            ExpandableDuplicateListAdapter(this@ShowDuplicate, scanDuplicateDataWithFile!!.listHeader, listHeaderChild, scanDuplicateDataWithFile!!.arrowImageResource
                    , scanDuplicateDataWithFile!!.checkBoxArray, scanDuplicateDataWithFile!!.storingSizeArray)
        } else {
            ExpandableDuplicateListAdapter(this@ShowDuplicate, scanDuplicateDataWithDoc!!.listHeader, listHeaderChild, scanDuplicateDataWithDoc!!.arrowImageResource
                    , scanDuplicateDataWithDoc!!.checkBoxArray, scanDuplicateDataWithDoc!!.storingSizeArray)
        }

        expandableListView!!.setAdapter(expandableduplicatelistadapter)


        for (i in listHeaderChild.keys.indices) {
            expandableListView!!.collapseGroup(i)
        }

        expandableListView!!.invalidate()


        // ListView Group click listener
        expandableListView!!.setOnGroupClickListener { _, _, _, _ -> false }

        //  Here use of DocumentFile in android 10 not File is using anymore
        if (Build.VERSION.SDK_INT <= 28) {
            // ListView Group expanded listener
            expandableListView!!.setOnGroupExpandListener { groupPosition -> scanDuplicateDataWithFile!!.arrowImageResource[groupPosition] = R.drawable.arrow_down_icon }
            // ListView Group collapsed listener
            expandableListView!!.setOnGroupCollapseListener { groupPosition -> scanDuplicateDataWithFile!!.arrowImageResource[groupPosition] = R.drawable.arrow_up_icon }
        } else {
            // ListView Group expanded listener
            expandableListView!!.setOnGroupExpandListener { groupPosition -> scanDuplicateDataWithDoc!!.arrowImageResource[groupPosition] = R.drawable.arrow_down_icon }
            // ListView Group collapsed listener
            expandableListView!!.setOnGroupCollapseListener { groupPosition -> scanDuplicateDataWithDoc!!.arrowImageResource[groupPosition] = R.drawable.arrow_up_icon }
        }


        // ListView on child click listener
        expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            onClickPath = listHeaderChild[groupPosition]!![childPosition]

            //  Here use of DocumentFile in android 10 not File is using anymore
            if (Build.VERSION.SDK_INT <= 28) {
                if (File(onClickPath.toString()).isDirectory) fragmentHistoryBottomSheetOpenFile!!.visibility = View.GONE
                else fragmentHistoryBottomSheetOpenFile!!.visibility = View.VISIBLE

            } else {
                if (DocumentFile.fromSingleUri(applicationContext, Uri.parse(onClickPath))!!.isDirectory) fragmentHistoryBottomSheetOpenFile!!.visibility = View.GONE
                else fragmentHistoryBottomSheetOpenFile!!.visibility = View.VISIBLE
            }
            mBottomSheetDialog!!.show()
            true
        }
        expandableListView!!.onItemLongClickListener = OnItemLongClickListener { _, _, _, _ -> false }

        setTotalSize(1)

        multiThreadingTask2 = MultiThreadingTask2(this@ShowDuplicate)
        multiThreadingTask2!!.execute()

        loadingAnimation!!.stop()

    }

    private fun reference() { // reference
        deleteDuplicateButton = findViewById(R.id.deleteDuplicateButton)
        nothingToShowConstraintsLayout = findViewById(R.id.nothingToShow_ConstraintsLayout)
        refreshImageView = findViewById(R.id.refreshImage_View)
        expandableListView = findViewById(R.id.duplicateExpandableListView)
        deleteDuplicateButton1 = findViewById(R.id.deleteDuplicateButton1)
        documentHelperClass = DocumentHelperClass(applicationContext)
        // create object
        notificationPermissionCheck = NotificationPermissionCheck(this@ShowDuplicate)


        mBottomSheetDialog = BottomSheetDialog(this@ShowDuplicate)

        val nullParent: ViewGroup? = null
        val sheetView = layoutInflater.inflate(R.layout.layout_dialog_moreoption, nullParent)
        mBottomSheetDialog!!.setContentView(sheetView)
        fragmentHistoryBottomSheetOpenFile = sheetView.findViewById(R.id.fragment_history_bottom_sheet_openFile)
        fragmentHistoryBottomSheetOpenFile!!.setOnClickListener(Onclick())
        val fragmentHistoryBottomSheetViewFolder = sheetView.findViewById<LinearLayout>(R.id.fragment_history_bottom_sheet_viewFolder)
        fragmentHistoryBottomSheetViewFolder.setOnClickListener(Onclick())
        val fragmentHistoryBottomSheetMoreInfo = sheetView.findViewById<LinearLayout>(R.id.fragment_history_bottom_sheet_moreInfo)
        fragmentHistoryBottomSheetMoreInfo.setOnClickListener(Onclick())
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.backImageView -> onBackPressed()
            R.id.shareImageView -> share()
            R.id.refreshImage_View -> if (refreshImageView != null && refreshImageView!!.rotation % 360 == 0f) {
                refreshImageView!!.animate().rotationBy(360f).duration = 1000
                multiThreadingTask2!!.cancel(true)
                refreshEverything()
            }
            R.id.deleteDuplicateButton, R.id.deleteDuplicateButton1 -> if (!notificationPermissionCheck!!.checkNotificationPermission()) {
                notificationPermissionCheck!!.customAlertDialog()
            } else {
                if (progressDone)
                    callDeleteCustomDialog()
                else {
                    CustomToast.toastIt(applicationContext, getString(R.string.alreadyProgress))
                }
            }
        }
    }

    private fun share() {
        val ratingLink = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover"
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Link-Share")
        i.putExtra(Intent.EXTRA_TEXT, R.string.shareMessage.toString() + ratingLink)
        startActivity(Intent.createChooser(i, "Share via"))
    }

    @SuppressLint("SetTextI18n")
    private fun setTotalSize(type: Int) {
        if (type == 1 && totalSize == 0L) {
            deleteDuplicateButton!!.text = "Delete ( Calc... )"
        } else {
            deleteDuplicateButton!!.text = "Delete (" + FileSize.convertIt(totalSize) + ")"
        }
        expandableListView!!.invalidate()
    }

    fun totalSizeChange(type: String, size: Long) {
        if (type == "add") {
            totalSize += size
        } else {
            totalSize -= size
        }
        setTotalSize(2)
    }


    fun valueTransfer() {
        //  Here use of DocumentFile in android 10 not File is using anymore
        if (Build.VERSION.SDK_INT <= 28) {
            listHeaderChild = scanDuplicateDataWithFile!!.listHeaderChild
            totalSize = scanDuplicateDataWithFile!!.totalSize
        } else {
            listHeaderChild = scanDuplicateDataWithDoc!!.listHeaderChild
            totalSize = scanDuplicateDataWithDoc!!.totalSize
        }
    }

    fun openWithFile(f: File) {
        var file = f
        try {
            if (file.isDirectory) file = file.listFiles()!![0]
            val myMime = MimeTypeMap.getSingleton()
            val newIntent = Intent(Intent.ACTION_VIEW)
            val mimeType = myMime.getMimeTypeFromExtension(fileExt(file.absolutePath)!!.substring(1))
            val uri = FileProvider.getUriForFile(applicationContext,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file)
            newIntent.setDataAndType(uri, mimeType)
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.startActivity(newIntent)
        } catch (e: Exception) {
            CustomToast.toastIt(applicationContext, "No handler for this type of file_icon.")
        }
    }

    fun openWithDoc(f: DocumentFile) {
        var file = f
        try {
            if (file.isDirectory) file = file.listFiles()[0]
            val myMime = MimeTypeMap.getSingleton()
            val newIntent = Intent(Intent.ACTION_VIEW)
            val mimeType = myMime.getMimeTypeFromExtension(FileUtils.replaceSdCardPath(applicationContext,
                    FileUtils.getPath(applicationContext, file.uri).toString()))!!.substring(1)
            val uri = file.uri
            newIntent.setDataAndType(uri, mimeType)
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.startActivity(newIntent)
        } catch (e: Exception) {
            CustomToast.toastIt(applicationContext, getString(R.string.noHandlerForType))
        }
    }


    private fun fileExt(u: String): String? {
        var url = u
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"))
        }
        return if (url.lastIndexOf(".") == -1) {
            null
        } else {
            var ext = url.substring(url.lastIndexOf(".") + 1)
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"))
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"))
            }
            ext.toLowerCase(Locale.getDefault())
        }
    }

    private fun callDeleteCustomDialog() {
        AlertDialog.Builder(this@ShowDuplicate)

                .setIcon(R.drawable.alert_icon)

                .setTitle(getString(R.string.sureToDelete))
                .setCancelable(true)
                .setPositiveButton(R.string.custom_dialog_yes) { _, _ ->
                    multiThreadingTask2!!.cancel(true)
                    CustomToast.toastIt(applicationContext, getString(R.string.ShowInNotification))
                    callIntentService()
                }
                .setNegativeButton(R.string.custom_dialog_no) { _, _ ->

                }
                .show()
    }
    private class MultiThreadingTask2
    internal constructor(private val showDuplicate: ShowDuplicate) : AsyncTask<String?, String?, String?>() {

        override fun doInBackground(vararg p0: String?): String? {
            val longSize: MutableList<Long> = ArrayList()
            var long: Long
            showDuplicate.totalSize = 0
            for ((k, i) in showDuplicate.listHeaderChild.values.withIndex()) {
                for (m in i) {
                    long = ExpandableDuplicateListAdapter.getFileSizeInBytes(m, showDuplicate)
                    showDuplicate.totalSize += long
                    longSize.add(long)
                }
                //  Here use of DocumentFile in android 10 not File is using anymore
                if (Build.VERSION.SDK_INT <= 28) {
                    showDuplicate.scanDuplicateDataWithFile!!.storingSizeArray[k] = ArrayList(longSize)
                } else {
                    showDuplicate.scanDuplicateDataWithDoc!!.storingSizeArray[k] = ArrayList(longSize)
                }
                longSize.clear()
                publishProgress()
            }
            return null
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            showDuplicate.expandableduplicatelistadapter!!.notifyDataSetChanged()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            showDuplicate.setTotalSize(2)
        }
    }

    private fun callIntentService() {

        progressDone = false

        val deletedList: MutableList<String> = java.util.ArrayList()
        var totalCount = 0

        for ((i, value) in listHeaderChild) {
            for (k in value.indices) {
                if (expandableduplicatelistadapter!!.checkBoxArray[i]!![k]) {
                    totalCount++
                    deletedList.add(listHeaderChild[i]!![k])
                }

            }

        }

        val intent = Intent(applicationContext, DeletingTask::class.java)
        DataHolder.instance.dataList = deletedList
        intent.putExtra("TotalCount", totalCount)
        intent.putExtra("TotalSize", totalSize)
        startService(intent)

    }

    class DataHolder {
        var dataList: MutableList<String> = java.util.ArrayList()

        companion object {
            private var dataHolder: DataHolder? = null
            val instance: DataHolder
                @Synchronized get() {
                    if (dataHolder == null) {
                        dataHolder = DataHolder()
                    }
                    return dataHolder!!
                }
        }
    }

    override fun onStart() {
        setReceiver()
        super.onStart()
    }
    private fun setReceiver() {

        myReceiver = MyReceiver(this@ShowDuplicate)
        val intentFilter = IntentFilter()
        intentFilter.addAction(actionKey)
        LocalBroadcastManager.getInstance(this@ShowDuplicate).registerReceiver(myReceiver!!, intentFilter)
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this@ShowDuplicate).unregisterReceiver(myReceiver!!)
        super.onStop()
    }


    class MyReceiver(private val showDuplicate: ShowDuplicate) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            CustomToast.toastIt(context, "Successfully Duplicate Data Deleted")
            showDuplicate.progressDone = intent.getBooleanExtra("broadcastMessage", true)
            sendBack()
        }

        private fun sendBack() {
            val intent = Intent(showDuplicate, MainActivity::class.java)
            intent.putExtra("passing", "Duplication")
            showDuplicate.startActivity(intent)
        }
    }



    inner class Onclick : View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.fragment_history_bottom_sheet_openFile ->
                    //  Here use of DocumentFile in android 10 not File is using anymore
                    if (Build.VERSION.SDK_INT <= 28) {
                        openWithFile(File(onClickPath.toString()))
                    } else {
                        openWithDoc(documentHelperClass!!.separatePath(FileUtils.replaceSdCardPath(applicationContext, FileUtils.getPath(applicationContext, Uri.parse(onClickPath)).toString())))
                    }
                R.id.fragment_history_bottom_sheet_viewFolder -> specificFolder(applicationContext, onClickPath.toString())
                R.id.fragment_history_bottom_sheet_moreInfo -> {
                    dialogInformationData()
                    mBottomSheetDialog!!.dismiss()
                }
            }
        }
    }

    fun dialogInformationData() {
        val ft = supportFragmentManager.beginTransaction()
        val informationData = DialogInformationData(this.onClickPath!!, this@ShowDuplicate)
        informationData.show(ft, "dialog")
    }

    companion object {

        const val actionKey = "any_key"

        fun specificFolder(context: Context, path: String) {
            //  Here use of DocumentFile in android 10 not File is using anymore
            val getPath: String = if (Build.VERSION.SDK_INT <= 28) {
                path.replace("/" + File(path).name, "")
            } else {
                path.replace("/" + DocumentFile.fromSingleUri(context, Uri.parse(path))!!.name, "")
            }

            val selectedUri = Uri.parse(getPath)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(selectedUri, "resource/folder")
            if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
                context.startActivity(intent)
            } else {
                CustomToast.toastIt(context, "No file explorer found")
            }
        }


    }
}