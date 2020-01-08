package com.sudoajay.duplication_data.mainFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.ExternalPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import com.sudoajay.duplication_data.storageStats.StorageInfo
import com.sudoajay.duplication_data.storageStats.StorageInfo.Companion.convertIt
import java.io.File

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
                        externalCheck!!.visibility == View.GONE) CustomToast.toastIt(context, getString(R.string.selectStorageText)) else {
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
                storageInfo!!.totalExternalMemorySize
                androidSdCardPermission = AndroidSdCardPermission(context!!, activity)
                if (externalCheck!!.visibility == View.GONE) {
                    if (androidSdCardPermission!!.isSdStorageWritable) {
                        externalCheck!!.visibility = View.VISIBLE
                        totalSizeLong += storageInfo!!.externalTotalSize - storageInfo!!.externalAvailableSize
                    } else {
                        androidSdCardPermission!!.callThread()
                    }
                } else {
                    externalCheck!!.visibility = View.GONE
                    totalSizeLong -= storageInfo!!.externalTotalSize - storageInfo!!.externalAvailableSize
                }
            }
        }
        fileSizeText!!.text = resources.getString(R.string.file_Size_Text, convertIt(totalSizeLong))
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
        fileSizeText!!.text = resources.getString(R.string.file_Size_Text, convertIt(totalSizeLong))
    }

    private fun sendForward() {
        val intent = Intent(mainNavigation, ShowDuplicate::class.java)
        intent.putExtra("internalCheck", internalCheck!!.visibility)
        intent.putExtra("externalCheck", externalCheck!!.visibility)
        startActivity(intent)
    }
}