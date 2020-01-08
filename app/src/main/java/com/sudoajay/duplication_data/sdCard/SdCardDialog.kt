package com.sudoajay.duplication_data.sdCard

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission

/**
 * Created by sudoajay on 4/15/18.
 */
class SdCardDialog : DialogFragment {
    private var android_sdCard_permission: AndroidSdCardPermission? = null

    constructor() {}
    @SuppressLint("ValidFragment")
    constructor(android_sdCard_permission: AndroidSdCardPermission?) {
        this.android_sdCard_permission = android_sdCard_permission
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            val rootview = inflater.inflate(R.layout.activity_custom_dialog_sd_select, container, false)
            val continueButton = rootview.findViewById<Button>(R.id.continue_Button)
            val seeMoreButton = rootview.findViewById<Button>(R.id.see_More_button)
            continueButton.setOnClickListener {
                android_sdCard_permission!!.storageAccessFrameWork()
                dismiss()
            }
            seeMoreButton.setOnClickListener {
                try {
                    val url = "https://developer.android.com/guide/topics/providers/document-provider.html"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                } catch (ignored: Exception) {
                }
            }
            isCancelable = false
            rootview
        } catch (e: Exception) {
            null
        }
    }

    override fun onStart() { // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}