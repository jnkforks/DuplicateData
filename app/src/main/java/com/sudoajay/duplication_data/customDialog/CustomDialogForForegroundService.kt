package com.sudoajay.duplication_data.customDialog

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.foregroundService.Foreground
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService

class CustomDialogForForegroundService  // blank constructor
    : DialogFragment(), OnItemSelectedListener {
    private var traceBackgroundService: TraceBackgroundService? = null
    private var activity: Activity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootview = inflater.inflate(R.layout.setting_layout_design_foreground, container, false)
        activity = getActivity()
        traceBackgroundService = TraceBackgroundService(this.context!!)
        // setup and instalizition for getSharedPreferences
// configuration or setup the sharedPreferences
        val switchForeground = rootview.findViewById<Switch>(R.id.switchForeground)
        // check if background Service is working or Not
        if (!traceBackgroundService!!.isForegroundServiceWorking) switchForeground.isChecked = false
        // globally variable
        val cancelButton = rootview.findViewById<Button>(R.id.cancelButton)
        val okButton = rootview.findViewById<Button>(R.id.ok_Button)
        val backImageViewChange = rootview.findViewById<ImageView>(R.id.back_Image_View_Change)
        backImageViewChange.setOnClickListener { dismiss() }
        okButton.setOnClickListener {
            if (!traceBackgroundService!!.isBackgroundServiceWorking) {
                if (switchForeground.isChecked) { // shared Preference changes
                    if (!isServiceRunningInForeground(activity, Foreground::class.java)) { // push foreground service
                        val startIntent = Intent(this@CustomDialogForForegroundService.context, Foreground::class.java)
                        startIntent.putExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"
                                , "Start_Foreground")
                        getActivity()!!.startService(startIntent)
                    }
                    traceBackgroundService!!.isForegroundServiceWorking = true
                    // dismiss or close the dialog
                    dismiss()
                } else {
                    if (isServiceRunningInForeground(activity, Foreground::class.java)) {
                        val theme: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            R.style.AppTheme
                        } else {
                            android.R.style.Theme_Dialog
                        }
                        AlertDialog.Builder(ContextThemeWrapper(activity, theme))
                                .setTitle(resources.getString(R.string.custom_Dialog_Box_Heading))
                                .setMessage(resources.getString(R.string.custom_Dialog_Box_Text)) // Specifying a listener allows you to take an action before dismissing the dialog.
// The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes) { _, _ ->
                                    // Continue with delete operation
// shared Preference changes
// push foreground service
                                    val stopIntent = Intent(activity, Foreground::class.java)
                                    stopIntent.putExtra("com.sudoajay.whatapp_media_mover_to_sdcard.ForegroundDialog"
                                            , "Stop_Foreground")
                                    activity!!.startService(stopIntent)
                                    traceBackgroundService!!.isForegroundServiceWorking = false
                                    dismiss()
                                } // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                    } else {
                        dismiss()
                    }
                }
            } else {
                dismiss()
            }
        }
        cancelButton.setOnClickListener { dismiss() }
        this.isCancelable = false
        return rootview
    }

    override fun onStart() { // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart()
        forceWrapContent(this.view)
    }

    private fun forceWrapContent(v: View?) { // Start with the provided view
        var current = v
        val dm = DisplayMetrics()
        getActivity()!!.windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        // Travel up the tree until fail, modifying the LayoutParams
        do { // Get the parent
            val parent = current!!.parent
            // Check if the parent exists
            if (parent != null) { // Get the view
                current = try {
                    parent as View
                } catch (e: ClassCastException) { // This will happen when at the top view, it cannot be cast to a View
                    break
                }
                // Modify the layout
                current.layoutParams.width = width - 10 * width / 100
                //                current.getLayoutParams().height = height - ((70 * height) / 100);
            }
        } while (current!!.parent != null)
        // Request a layout to be re-done
        current!!.requestLayout()
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun isServiceRunningInForeground(context: Context?, serviceClass: Class<*>): Boolean {
        return try {
            if (traceBackgroundService!!.isForegroundServiceWorking) {
                val manager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                    if (serviceClass.name == service.service.className) {
                        if (service.foreground) {
                            return true
                        }
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