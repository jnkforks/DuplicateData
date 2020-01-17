package com.sudoajay.duplication_data.delete

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.duplication_data.intentService.DeletingTask
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import java.io.File
import java.io.IOException
import java.util.*

class DeleteDataUsingFile {
    private var sdCardDocumentFile: DocumentFile? = null
    private var sdCardPath: String = ""
    private var externalPath = ""
    private var throughForegroundService: Boolean
    private var deletingTask: DeletingTask? = null
    private var mContext: Context? = null
    private var deletedList: MutableList<String> = ArrayList()

    constructor(deletingTask: DeletingTask, deletedList: MutableList<String>) {

        this.deletingTask = deletingTask
        this.mContext = deletingTask.applicationContext
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
        sdCardPath = sdCardPathSharedPreference.sdCardPath

        if (sdCardPathSharedPreference.stringURI.isNotEmpty())
            sdCardDocumentFile = DocumentFile.fromTreeUri(mContext!!, Uri.parse(sdCardPathSharedPreference.stringURI))

        externalPath = AndroidExternalStoragePermission.getExternalPath(deletingTask!!.applicationContext)!!


        for (deletedPath in deletedList) {
            separateTheData(deletedPath)
            if (!throughForegroundService) deletingTask!!.updateProgress()
        }

    }

    private fun separateTheData(path: String) {
        if (path.contains(externalPath)) deleteTheDataFromInternalStorage(path)
        else if (sdCardDocumentFile != null) deleteTheDataFromExternal(path)
        
    }


    private fun deleteTheDataFromInternalStorage(path: String) {
        val file = File(path)
        if (file.isDirectory) {
            if (!file.listFiles().isNullOrEmpty()) {
                for (child in file.listFiles()!!) {
                    deleteTheDataFromInternalStorage(child.absolutePath)
                }
            }
            if (file.listFiles().isNullOrEmpty()) file.delete()
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
        try {
            val list = path.split(spilt)[1].split("/")

            for (file in list) {
                documentFile = documentFile.findFile(file)!!
            }
            deleteIt(documentFile)
        }catch (ignored :Exception){

        }

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