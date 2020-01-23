package com.sudoajay.duplication_data.mainFragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.sudoajay.duplication_data.MainActivity
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.helperClass.FileSize
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.storageStats.StorageInfo

/**
 * A simple [Fragment] subclass.
 */
@SuppressLint("SetTextI18n")
class Home : Fragment() {
    // global variable
    private var mainActivity: MainActivity? = null
    private var layout: View? = null
    private var circularProgressBarInternal: CircularProgressBar? = null
    private var circularProgressBarExternal: CircularProgressBar? = null
    private var textViewInternal2: TextView? = null
    private var textViewInternal3: TextView? = null
    private var textViewUsedSpaceSizeInternal: TextView? = null
    private var textViewInternal4: TextView? = null
    private var textViewInternal5: TextView? = null
    private var textViewUsedSpaceSizeExternal: TextView? = null
    private var textViewExternal2: TextView? = null
    private var textViewExternal4: TextView? = null
    private var textViewExternal5: TextView? = null
    private var textViewExternal3: TextView? = null
    private var storageInfo: StorageInfo? = null
    private var externalStoragePermission: AndroidExternalStoragePermission? = null
    private var sdCardPermission: AndroidSdCardPermission? = null


    fun createInstance(mainActivity: MainActivity?): Home {
        this.mainActivity = mainActivity
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.main_fragment_home, container, false)
        // Reference and Create Object
        reference()
        //Set Storage Stats
        setInternalStorageStats()
        setExternalStorageStats()

        // custom progress bar
        val animationDuration = 4000
        circularProgressBarInternal!!.setProgressWithAnimation(java.lang.Float.valueOf(storageInfo!!.usedInternalPercentage), animationDuration) // Default duration = 1500ms
        circularProgressBarExternal!!.setProgressWithAnimation(java.lang.Float.valueOf(storageInfo!!.usedExternalPercentage), animationDuration)
        return layout
    }

    // Reference and Create Object
    private fun reference() {
        textViewUsedSpaceSizeInternal = layout!!.findViewById(R.id.textViewUsedSpaceSizeInternal)
        textViewInternal2 = layout!!.findViewById(R.id.textViewInternal2)
        textViewInternal3 = layout!!.findViewById(R.id.textViewInternal3)
        textViewInternal4 = layout!!.findViewById(R.id.textViewInternal4)
        textViewInternal5 = layout!!.findViewById(R.id.textViewInternal5)
        circularProgressBarInternal = layout!!.findViewById(R.id.circularProgressBarInternal)
        textViewExternal4 = layout!!.findViewById(R.id.textViewExternal4)
        textViewUsedSpaceSizeExternal = layout!!.findViewById(R.id.textViewUsedSpaceSizeExternal)
        textViewExternal2 = layout!!.findViewById(R.id.textViewExternal2)
        textViewExternal5 = layout!!.findViewById(R.id.textViewExternal5)
        textViewExternal3 = layout!!.findViewById(R.id.textViewExternal3)
        circularProgressBarExternal = layout!!.findViewById(R.id.circularProgressBarExternal)
        //Sd Card shared Preference
        externalStoragePermission = AndroidExternalStoragePermission(context!!, mainActivity)
        sdCardPermission = AndroidSdCardPermission(context!!, mainActivity)
        // create object
        storageInfo = StorageInfo(context!!)
    }

    // on click listener
    fun onClick(v: View) {
        when (v.id) {
            R.id.cardViewInternal3 -> if (externalStoragePermission!!.isExternalStorageWritable) {
                if (circularProgressBarInternal!!.color == ContextCompat.getColor(mainActivity!!, R.color.used_progressBarColor)) {
                    circularProgressBarInternal!!.backgroundColor = ContextCompat.getColor(mainActivity!!, R.color.used_progressBarColor)
                    circularProgressBarInternal!!.color = ContextCompat.getColor(mainActivity!!, R.color.free_progressBarColor)
                    circularProgressBarInternal!!.setProgressWithAnimation(java.lang.Float.valueOf(storageInfo!!.availableInternalPercent)
                            , 0) // Default duration = 1500ms
                    textViewInternal2!!.text = FileSize.convertIt(storageInfo!!.internalAvailableSize)
                    textViewInternal3!!.text = resources.getString(R.string.text_Free)
                } else {
                    circularProgressBarInternal!!.backgroundColor = ContextCompat.getColor(mainActivity!!, R.color.free_progressBarColor)
                    circularProgressBarInternal!!.color = ContextCompat.getColor(mainActivity!!, R.color.used_progressBarColor)
                    circularProgressBarInternal!!.setProgressWithAnimation(java.lang.Float.valueOf(storageInfo!!.usedInternalPercentage)
                            , 0) // Default duration = 1500ms
                    textViewInternal2!!.text = FileSize.convertIt(storageInfo!!.internalTotalSize -
                            storageInfo!!.internalAvailableSize)
                    textViewInternal3!!.text = resources.getString(R.string.text_Used)
                }
            } else {
                externalStoragePermission!!.callThread()
            }
            R.id.cardViewExternal3 ->
                if ((externalStoragePermission!!.isExternalStorageWritable || Build.VERSION.SDK_INT >= 29)
                        && sdCardPermission!!.isSdStorageWritable) {

                if (circularProgressBarExternal!!.color == ContextCompat.getColor(mainActivity!!, R.color.used_progressBarColor)) {
                    circularProgressBarExternal!!.backgroundColor = ContextCompat.getColor(mainActivity!!, R.color.used_progressBarColor)
                    circularProgressBarExternal!!.color = ContextCompat.getColor(mainActivity!!, R.color.free_progressBarColor)
                    circularProgressBarExternal!!.setProgressWithAnimation(java.lang.Float.valueOf(storageInfo!!.availableExternalPercentage)
                            , 0) // Default duration = 1500ms
                    textViewExternal3!!.text = resources.getString(R.string.text_Free)
                    textViewExternal2!!.text = FileSize.convertIt(storageInfo!!.externalAvailableSize)
                } else {
                    circularProgressBarExternal!!.backgroundColor = ContextCompat.getColor(mainActivity!!, R.color.free_progressBarColor)
                    circularProgressBarExternal!!.color = ContextCompat.getColor(mainActivity!!, R.color.used_progressBarColor)
                    circularProgressBarExternal!!.setProgressWithAnimation(java.lang.Float.valueOf(storageInfo!!.usedExternalPercentage)
                            , 0) // Default duration = 1500ms
                    textViewExternal2!!.text = FileSize.convertIt(storageInfo!!.externalTotalSize -
                            storageInfo!!.externalAvailableSize)
                    textViewExternal3!!.text = resources.getString(R.string.text_Used)
                }
            } else if (!externalStoragePermission!!.isExternalStorageWritable && Build.VERSION.SDK_INT <= 28) {
                externalStoragePermission!!.callThread()
            } else {
                sdCardPermission!!.callThread()
            }
        }
    }


    private fun setInternalStorageStats() {
        val totalInternal = storageInfo!!.totalInternalMemorySize
        val availableInternal = storageInfo!!.usedInternalMemorySize
        val availableInternalPercent = storageInfo!!.usedInternalPercentage
        val usedInternalPercent = storageInfo!!.availableInternalPercentage
        // set text to used spaced size
        textViewUsedSpaceSizeInternal!!.text = "$availableInternal / $totalInternal"
        // set text to internal
        textViewInternal2!!.text = availableInternal
        // set Text to used Percentage
        textViewInternal4!!.text = "$availableInternalPercent % Used"
        textViewInternal5!!.text = "$usedInternalPercent % Free"
    }


    private fun setExternalStorageStats() {
        val totalExternal = storageInfo!!.totalExternalMemorySize
        val availableExternal = storageInfo!!.usedExternalMemorySize
        val availableExternalPercent = storageInfo!!.usedExternalPercentage
        val usedExternalPercent = storageInfo!!.availableExternalPercentage
        // set text to used spaced size
        textViewUsedSpaceSizeExternal!!.text = "$availableExternal / $totalExternal"
        // set text to internal
        textViewExternal2!!.text = availableExternal
        // set Text to used Percentage
        textViewExternal4!!.text = "$availableExternalPercent % Used"
        textViewExternal5!!.text = "$usedExternalPercent % Free"
    }
}