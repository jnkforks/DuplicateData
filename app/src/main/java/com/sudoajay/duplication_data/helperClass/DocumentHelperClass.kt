package com.sudoajay.duplication_data.helperClass

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.sharedPreferences.ExternalPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference

class DocumentHelperClass(private var context: Context) {
    private var externalPathSharedPreference: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null

    fun separatePath(path: String): DocumentFile {
        val externalPath: String = AndroidExternalStoragePermission.getExternalPath(context).toString()
        var spilt: String
        val documentFile: DocumentFile
        if (path.contains(externalPath)) {
            documentFile = DocumentFile.fromTreeUri(context, Uri.parse(externalPathSharedPreference?.stringURI))!!
            spilt = externalPath
        } else {
            documentFile = DocumentFile.fromTreeUri(context, Uri.parse(sdCardPathSharedPreference?.stringURI))!!
            spilt = sdCardPathSharedPreference!!.sdCardPath.toString()
        }
        spilt = path.split(spilt)[1]
        return dugPath(spilt, documentFile)
    }

    private fun dugPath(path: String, document: DocumentFile): DocumentFile {
        var documentFile = document
        val list = path.split("/")
        for (file in list) {
            documentFile = documentFile.findFile(file)!!
        }
        return documentFile
    }

    init {
        externalPathSharedPreference = ExternalPathSharedPreference(context)
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
    }
}