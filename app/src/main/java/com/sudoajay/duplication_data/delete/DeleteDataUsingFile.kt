package com.sudoajay.duplication_data.delete

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import java.io.File
import java.io.IOException
import java.util.*

class DeleteDataUsingFile {
    private var sdCardDocumentFile: DocumentFile? = null
    private var sdCardPath: String = ""
    private var externalPath = ""
    private var throughForegroundService: Boolean
    private var showDuplicate: ShowDuplicate? = null
    private var mContext: Context? = null
    private var deletedList: MutableList<String> = ArrayList()

    constructor(showDuplicate: ShowDuplicate?, deletedList: MutableList<String>) {

        this.showDuplicate = showDuplicate
        this.mContext = showDuplicate!!.applicationContext
        this.deletedList = deletedList
        throughForegroundService = false
        getThePath()
    }

    constructor(context: Context?, deletedList: MutableList<String>) {

        this.mContext = context
        this.deletedList = deletedList
        throughForegroundService = true
        getThePath()
    }

    private fun getThePath() {

        val sdCardPathSharedPreference = SdCardPathSharedPreference(mContext!!)
        sdCardPath = sdCardPathSharedPreference.sdCardPath!!
        sdCardDocumentFile = DocumentFile.fromTreeUri(mContext!!, Uri.parse(sdCardPathSharedPreference.stringURI))

        externalPath = AndroidExternalStoragePermission.getExternalPath(showDuplicate!!.applicationContext)!!


        for (deletedPath in deletedList) {
            separateTheData(deletedPath)
            if (!throughForegroundService) showDuplicate!!.javaThreading!!.updateProgress()
        }

    }

    private fun separateTheData(path: String) {
        when {
            path.contains(externalPath) -> {
                deleteTheDataFromInternalStorage(path)
            }
            else -> {
                deleteTheDataFromExternal(path)
            }
        }
    }


    private fun deleteTheDataFromInternalStorage(path: String) {
        val file = File(path)
        if (file.isDirectory) {
            for (child in file.listFiles()!!) {
                deleteTheDataFromInternalStorage(child.absolutePath)
            }
            if (file.listFiles()!!.isEmpty()) file.delete()
        } else {
            file.delete()
            if (file.exists()) {
                try {
                    file.canonicalFile.delete()
                } catch (ignored: IOException) {
                }
            }
        }
    }

    private fun deleteTheDataFromExternal(path: String) {
        val spilt: String = sdCardPath
        var documentFile: DocumentFile = sdCardDocumentFile!!

        val list = path.split(spilt)[1].split("/")
        for (file in list) {
            documentFile = documentFile.findFile(file)!!
        }
        deleteIt(documentFile)
    }

    private fun deleteIt(documentFile: DocumentFile) {
        if (documentFile.isDirectory) {
            for (file in documentFile.listFiles()) {
                deleteIt(file)
            }
            if (documentFile.listFiles().isEmpty())
                documentFile.delete()

        } else {
            documentFile.delete()
        }
    }


}