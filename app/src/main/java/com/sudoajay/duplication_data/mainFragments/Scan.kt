package com.sudoajay.duplication_data.mainFragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate
import com.sudoajay.duplication_data.helperClass.FileSize
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.ExternalPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import com.sudoajay.duplication_data.storageStats.StorageInfo

/**
 * A simple [Fragment] subclass.
 */
class Scan : Fragment() {
    // global variable
    private var mainNavigation: MainActivity? = null
    private var layout: View? = null
    private var internalCheck: ImageView? = null
    private var externalCheck: ImageView? = null
    private var androidExternalStoragePermission: AndroidExternalStoragePermission? = null
    private var androidSdCardPermission: AndroidSdCardPermission? = null
    private var externalSharedPreferences: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null
    private var storageInfo: StorageInfo? = null
    private var fileSizeText: Button? = null
    private var totalSizeLong: Long = 0
    private var customToastLayout: View? = null
    private var customToast: Toast? = null

    fun createInstance(main_navigation: MainActivity?): Scan {
        this.mainNavigation = main_navigation
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.main_fragment_scan, container, false)
        val inflaters = layoutInflater
        customToastLayout = inflaters.inflate(R.layout.activity_custom_toast,
                layout!!.findViewById<View>(R.id.toastcustom) as? ViewGroup)
        // Reference and Create Object
        reference()
        // check if internal and sd card is write able
        isWritable()
        return layout
    }

    // Reference and Create Object
    private fun reference() {
        internalCheck = layout?.findViewById(R.id.internal_Check)
        externalCheck = layout?.findViewById(R.id.external_Check)
        fileSizeText = layout?.findViewById(R.id.file_Size_Text)
        // create object
        androidExternalStoragePermission = AndroidExternalStoragePermission(context!!, activity)
        androidSdCardPermission = AndroidSdCardPermission(context!!, activity)
        externalSharedPreferences = ExternalPathSharedPreference(context!!)
        sdCardPathSharedPreference = SdCardPathSharedPreference(context!!)
        storageInfo = StorageInfo(context!!)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.scan_Button, R.id.scan_Button1 ->  // if nothing check
                if (internalCheck!!.visibility == View.GONE &&
                        externalCheck!!.visibility == View.GONE)
                    customToastForSelect()
                else {
                    try {
                        sendForward()
                    } catch (ignored: Exception) {
                    }
                }
            R.id.internal_Image_View, R.id.internal_Text_View, R.id.internal_Check -> {
                // call this method to get size of data
                storageInfo!!.totalInternalMemorySize
                if (internalCheck!!.visibility == View.GONE) {
                    if (androidExternalStoragePermission!!.isExternalStorageWritable) {
                        internalCheck!!.visibility = View.VISIBLE
                        totalSizeLong += storageInfo!!.internalTotalSize - storageInfo!!.internalAvailableSize
                    } else {
                        androidExternalStoragePermission!!.callThread()
                    }
                } else {

                    internalCheck!!.visibility = View.GONE
                    totalSizeLong -= storageInfo!!.internalTotalSize - storageInfo!!.internalAvailableSize
                }
            }
            R.id.external_Image_View, R.id.external_Text_View, R.id.external_Check -> {
                // call this method to get size of data
                androidSdCardPermission = AndroidSdCardPermission(context!!, activity)
                storageInfo!!.totalExternalMemorySize
                if (externalCheck!!.visibility == View.GONE) {
                    if ((androidExternalStoragePermission!!.isExternalStorageWritable || Build.VERSION.SDK_INT >= 29) && androidSdCardPermission!!.isSdStorageWritable) {
                        externalCheck!!.visibility = View.VISIBLE
                        totalSizeLong += storageInfo!!.externalTotalSize - storageInfo!!.externalAvailableSize
                    } else if (!androidExternalStoragePermission!!.isExternalStorageWritable && Build.VERSION.SDK_INT <= 28) {
                        androidExternalStoragePermission!!.callThread()
                    } else {
                        androidSdCardPermission!!.callThread()
                    }

                } else {

                    externalCheck!!.visibility = View.GONE
                    totalSizeLong -= storageInfo!!.externalTotalSize - storageInfo!!.externalAvailableSize
                }
            }
        }
        fileSizeText!!.text = resources.getString(R.string.file_Size_Text, FileSize.convertIt(totalSizeLong))
    }


    private fun isWritable() {
        totalSizeLong = 0
        if (!androidExternalStoragePermission!!.isExternalStorageWritable) {
            internalCheck!!.visibility = View.GONE
        } else {
            storageInfo!!.totalInternalMemorySize
            totalSizeLong += storageInfo!!.internalTotalSize - storageInfo!!.internalAvailableSize
        }
        if (!androidSdCardPermission!!.isSdStorageWritable) {
            externalCheck!!.visibility = View.GONE
        } else {
            storageInfo!!.totalExternalMemorySize
            totalSizeLong += storageInfo!!.externalTotalSize - storageInfo!!.externalAvailableSize
        }
        fileSizeText!!.text = resources.getString(R.string.file_Size_Text, FileSize.convertIt(totalSizeLong))
    }

    private fun sendForward() {
        val intent = Intent(mainNavigation, ShowDuplicate::class.java)
        intent.putExtra("internalCheck", internalCheck!!.visibility)
        intent.putExtra("externalCheck", externalCheck!!.visibility)
        startActivity(intent)
    }

    private fun customToastForSelect() {
        val toastTextView = customToastLayout!!.findViewById<TextView>(R.id.text)
        if (customToast == null || customToast!!.view.windowVisibility != View.VISIBLE) {
            customToast = Toast(context)
            customToast!!.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            customToast!!.duration = Toast.LENGTH_LONG
            customToast!!.view = customToastLayout
            toastTextView.text = getString(R.string.selectStorageText)
            customToast!!.show()
        } else {
            customToast!!.cancel()
        }

    }
}