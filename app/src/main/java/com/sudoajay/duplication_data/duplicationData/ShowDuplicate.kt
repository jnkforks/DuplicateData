package com.sudoajay.duplication_data.duplicationData

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sudoajay.duplication_data.BuildConfig
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.customDialog.DialogInformationData
import com.sudoajay.duplication_data.delete.DeleteDataUsingDoc
import com.sudoajay.duplication_data.delete.DeleteDataUsingFile
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.helperClass.DocumentHelperClass
import com.sudoajay.duplication_data.helperClass.FileUtils
import com.sudoajay.duplication_data.notification.NotifyNotification
import com.sudoajay.duplication_data.permission.NotificationPermissionCheck
import com.sudoajay.lodinganimation.LoadingAnimation
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("ObsoleteSdkInt")
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
    var javaThreading: JavaThreading? = null
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

        //  Its supports till android 9 & api 28
        if (Build.VERSION.SDK_INT <= getString(R.string.apiLevel).toInt()) {
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
            //             Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= showDuplicate.getString(R.string.apiLevel).toInt()) {
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
        // Its supports till android 9 & api 28
        expandableduplicatelistadapter = if (Build.VERSION.SDK_INT <= getString(R.string.apiLevel).toInt()) {
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

        // Its supports till android 9 & api 28
        if (Build.VERSION.SDK_INT <= getString(R.string.apiLevel).toInt()) {
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

//            Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= getString(R.string.apiLevel).toInt()) {
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

    @SuppressLint("InflateParams")
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


        val sheetView = layoutInflater.inflate(R.layout.layout_dialog_moreoption, null)
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
//                valueTransfer()
            }
            R.id.deleteDuplicateButton, R.id.deleteDuplicateButton1 -> if (!notificationPermissionCheck!!.checkNotificationPermission()) {
                notificationPermissionCheck!!.customAertDialog()
            } else {
                callDeleteCustomDialog()
            }
        }
    }

    private fun share() {
        val ratingLink = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover"
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Link-Share")
        i.putExtra(Intent.EXTRA_TEXT, R.string.share_info.toString() + ratingLink)
        startActivity(Intent.createChooser(i, "Share via"))
    }

    @SuppressLint("SetTextI18n")
    private fun setTotalSize(type: Int) {
        if (type == 1 && totalSize == 0L) {
            deleteDuplicateButton!!.text = "Delete ( Calc... )"
        } else {
            deleteDuplicateButton!!.text = "Delete (" + convertIt(totalSize) + ")"
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
        //            Its supports till android 9 & api 28
        if (Build.VERSION.SDK_INT <= getString(R.string.apiLevel).toInt()) {
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
            val mimeType = myMime.getMimeTypeFromExtension(fileExt
            (FileUtils.replaceSdCardPath(applicationContext, FileUtils.getPath(applicationContext, file.uri)))!!.substring(1))
            val uri = file.uri
            newIntent.setDataAndType(uri, mimeType)
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.startActivity(newIntent)
        } catch (e: Exception) {
            CustomToast.toastIt(applicationContext, "No handler for this type of file_icon.")
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
            ext.toLowerCase()
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
                    thread()
                }

                .setNegativeButton(R.string.custom_dialog_no) { _, _ ->

                }
                .show()
    }


    private fun sendBack() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("passing", "Duplication")
        startActivity(intent)
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
                //            Its supports till android 9 & api 28
                if (Build.VERSION.SDK_INT <= showDuplicate.getString(R.string.apiLevel).toInt()) {
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

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            showDuplicate.setTotalSize(2)
        }
    }

    private fun thread() {
        Handler().postDelayed({
            run {
                javaThreading = JavaThreading(this@ShowDuplicate)
                javaThreading!!.run()

            }
        }, 200)

    }

    class JavaThreading
    internal constructor(private val showDuplicate: ShowDuplicate) : Runnable {
        private var progress = 0
        private var totalCount = 0
        private var contentView: RemoteViews? = null
        private var notification: Notification? = null
        private var deletedList: MutableList<String> = ArrayList()

        override fun run() {

            for ((i, value) in showDuplicate.listHeaderChild) {
                for (k in value.indices) {
                    if (showDuplicate.expandableduplicatelistadapter!!.checkBoxArray[i]!![k]) {
                        totalCount++
                        deletedList.add(showDuplicate.listHeaderChild[i]!![k])
                    }

                }

            }
            notification()
            //             Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= showDuplicate.getString(R.string.apiLevel).toInt()) {
                DeleteDataUsingFile(showDuplicate, deletedList)
            } else {
                DeleteDataUsingDoc(showDuplicate, deletedList)
            }
            callThread()
        }

        fun updateProgress() {
            progress++
            contentView!!.setTextViewText(R.id.size_Title, "$progress/$totalCount")
            contentView!!.setTextViewText(R.id.percent_Text, (progress * 100 / totalCount).toString() + "%")
            contentView!!.setTextViewText(R.id.time_Tittle, getCurrentTime())
            contentView!!.setProgressBar(R.id.progressBar, totalCount, progress, false)
            notificationManager!!.notify(1, notification)
        }

        private fun callThread() {
            val handler = Handler()
            handler.postDelayed({
                notificationManager!!.cancel(1)
                val notifyNotification = NotifyNotification(showDuplicate.applicationContext)
                notifyNotification.notify("You Have Saved " + convertIt(showDuplicate.totalSize) + " Of Data ", showDuplicate.getString(R.string.delete_Done_title))
                CustomToast.toastIt(showDuplicate.applicationContext, "Successfully Duplicate Data Deleted")
                showDuplicate.sendBack()
            }, 2000)
        }

        fun notification() {
            val id = showDuplicate.getString(R.string.duplicate_Id) // default_channel_id
            val title = showDuplicate.getString(R.string.duplicate_title) // Default Channel
            val mBuilder: NotificationCompat.Builder
            val closeButton = Intent()
            closeButton.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            contentView = RemoteViews(showDuplicate.packageName, R.layout.activity_custom_notification)
            contentView!!.setImageViewResource(R.id.image, R.mipmap.ic_launcher)
            contentView!!.setTextViewText(R.id.title, "Deletion...")
            contentView!!.setTextViewText(R.id.time_Tittle, getCurrentTime())
            contentView!!.setProgressBar(R.id.progressBar, 100, 0, false)
            contentView!!.setTextViewText(R.id.size_Title, "0/$totalCount")
            contentView!!.setTextViewText(R.id.percent_Text, "00%")
            if (notificationManager == null) {
                notificationManager = showDuplicate.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW
                assert(notificationManager != null)
                var mChannel = notificationManager!!.getNotificationChannel(id)
                if (mChannel == null) {
                    mChannel = NotificationChannel(id, title, importance)
                    notificationManager!!.createNotificationChannel(mChannel)
                }
            }
            mBuilder = NotificationCompat.Builder(showDuplicate.applicationContext, id)
                    .setSmallIcon(R.mipmap.ic_launcher) // required
                    .setContent(contentView)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setLights(Color.parseColor("#075e54"), 3000, 3000)
            mBuilder.setContentIntent(
                    PendingIntent.getActivity(
                            showDuplicate.applicationContext,
                            0,
                            closeButton,
                            PendingIntent.FLAG_UPDATE_CURRENT))
            notification = mBuilder.build()
            notification!!.flags = notification!!.flags or Notification.FLAG_AUTO_CANCEL
            notificationManager!!.notify(1, notification)
        }

        private fun getCurrentTime(): String {
            val calendar = Calendar.getInstance()
            val hours = calendar[Calendar.HOUR_OF_DAY]
            val minutes = calendar[Calendar.MINUTE]
            return if (hours < 12) {
                "$hours:$minutes AM"
            } else {
                (hours - 12).toString() + ":" + minutes + " PM"
            }
        }

    }

    inner class Onclick : View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.fragment_history_bottom_sheet_openFile ->
                    if (Build.VERSION.SDK_INT <= getString(R.string.apiLevel).toInt()) {
                        openWithFile(File(onClickPath.toString()))
                    } else {
                        openWithDoc(documentHelperClass!!.separatePath(FileUtils.replaceSdCardPath(applicationContext, FileUtils.getPath(applicationContext, Uri.parse(onClickPath)))))
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

        fun specificFolder(context: Context, path: String) {

            //            Its supports till android 9 & api 28
            val getPath: String = if (Build.VERSION.SDK_INT <= context.getString(R.string.apiLevel).toInt()) {
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

        private var notificationManager: NotificationManager? = null
        @JvmStatic
        fun convertIt(size: Long): String {
            return when {
                size > 1024 * 1024 * 1024 -> { // GB
                    convertToDecimal(size.toFloat() / (1024 * 1024 * 1024)) + " GB"
                }
                size > 1024 * 1024 -> { // MB
                    convertToDecimal(size.toFloat() / (1024 * 1024)) + " MB"
                }
                else -> { // KB
                    convertToDecimal(size.toFloat() / 1024) + " KB"
                }
            }
        }

        private fun convertToDecimal(value: Float): String {
            val size = value.toString() + ""
            return if (value >= 1000) {
                size.substring(0, 4)
            } else if (value >= 100) {
                size.substring(0, 3)
            } else {
                if (size.length == 2 || size.length == 3) {
                    size.substring(0, 1)
                } else size.substring(0, 4)
            }
        }
    }
}