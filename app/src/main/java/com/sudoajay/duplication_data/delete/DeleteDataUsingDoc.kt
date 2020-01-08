package com.sudoajay.duplication_data.delete

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.duplication_data.duplicationData.ShowDuplicate
import com.sudoajay.duplication_data.helperClass.FileUtils
import com.sudoajay.duplication_data.sharedPreferences.ExternalPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import java.util.*

class DeleteDataUsingDoc {
    private var sdCardDocumentFile: DocumentFile? = null
    private var externalDocumentFile: DocumentFile? = null
    private var sdCardPath: String = ""
    private var externalPath: String = ""
    private var throughForegroundService: Boolean
    private var showDuplicate: ShowDuplicate? = null
    private var mContext: Context? = null
    private var deletedList: MutableList<String> = ArrayList()

    constructor(showDuplicate: ShowDuplicate?, deletedList: MutableList<String>
    ) {
        this.deletedList = deletedList
        this.showDuplicate = showDuplicate
        this.mContext = showDuplicate!!.applicationContext

        throughForegroundService = false
        getThePath()
    }
    constructor(context: Context?, deletedList: MutableList<String>) {
        this.deletedList = deletedList
        this.mContext = context

        throughForegroundService = true
        getThePath()
    }

    private fun getThePath() {

        val sdCardPathSharedPreference = SdCardPathSharedPreference(mContext!!)
        val externalPathSharedPreference = ExternalPathSharedPreference(mContext!!)

        sdCardPath = sdCardPathSharedPreference.sdCardPath!!
        sdCardDocumentFile = DocumentFile.fromTreeUri(mContext!!, Uri.parse(sdCardPathSharedPreference.stringURI))

        externalPath = externalPathSharedPreference.externalPath!!
        externalDocumentFile = DocumentFile.fromTreeUri(mContext!!, Uri.parse(externalPathSharedPreference.stringURI))


        for (deletedPath in deletedList) {
            separateTheData(deletedPath)
            if (!throughForegroundService) showDuplicate!!.javaThreading!!.updateProgress()
        }
    }

    private fun separateTheData(uriPath: String) {
        val path = FileUtils.replaceSdCardPath(mContext, FileUtils.getPath(mContext, Uri.parse(uriPath)))
        if (path.contains(externalPath)) {
            deleteTheData(path, 1)
        } else {
            deleteTheData(path, 2)
        }
    }


    private fun deleteTheData(path: String, type: Int) {
        var documentFile: DocumentFile

        val spilt: String
        if (type == 1) {
            spilt = externalPath
            documentFile = externalDocumentFile!!
        } else {
            spilt = sdCardPath
            documentFile = sdCardDocumentFile!!
        }
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