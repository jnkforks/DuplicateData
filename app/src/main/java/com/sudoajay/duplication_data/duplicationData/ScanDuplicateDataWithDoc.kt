package com.sudoajay.duplication_data.duplicationData

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.View
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.sharedPreferences.ExternalPathSharedPreference
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

class ScanDuplicateDataWithDoc(private var context: Context?) {
    private val getAllData: MutableList<DocumentFile> = LinkedList()
    private val listPath: MutableList<String> = ArrayList()

    private var externalPath: String? = null
    private var externalUri: String? = null
    private var sdCardDir: String? = null
    private var sdCardUri: String? = null
    private val rejectedFolder = ArrayList<String>()
    private val apkFile = ArrayList<String>()
    private val emptyFolder = ArrayList<String>()
    private val logFolder = ArrayList<String>()
    private var messageDigest: MessageDigest? = null


    val listHeader: MutableList<String> = ArrayList()
    val listHeaderChild = LinkedHashMap<Int, List<String>>()
    val storingSizeArray = LinkedHashMap<Int, List<Long>>()
    val checkBoxArray = LinkedHashMap<Int, List<Boolean>>()
    var separateList = ArrayList<String>()
    val arrowImageResource: MutableList<Int> = ArrayList()

    private val longSize: MutableList<Long> = ArrayList()
    private val setsBoolean: MutableList<Boolean> = ArrayList()
    var totalSize: Long = 0
    private var countDuplicate = 0
    private var countAll = 0


    fun duplication(internal_Visible: Int, external_Visible: Int) {

        val sdCardPathSharedPreference = SdCardPathSharedPreference(context!!)
        val externalPathSharedPreference = ExternalPathSharedPreference(context!!)

        val duplicateList: MutableMap<String, MutableList<String>> = HashMap()

        if (internal_Visible == View.VISIBLE) {
            externalPath = externalPathSharedPreference.externalPath
            externalUri = externalPathSharedPreference.stringURI


            whatsAppUnnecessaryData(Uri.parse(externalUri))
            val external = DocumentFile.fromTreeUri(context!!, Uri.parse(externalUri))
            rejectedFolder.add(external!!.findFile("Android")!!.findFile("data")!!.uri.toString())

            if (external.exists()) getAllPath(external)
        }
        if (external_Visible == View.VISIBLE) {
            sdCardDir = sdCardPathSharedPreference.sdCardPath
            sdCardUri = sdCardPathSharedPreference.stringURI

            whatsAppUnnecessaryData(Uri.parse(sdCardUri))
            val sdCard = DocumentFile.fromTreeUri(context!!, Uri.parse(sdCardUri))
            rejectedFolder.add(sdCard!!.findFile("Android")!!.findFile("data")!!.uri.toString())

            if (sdCard.exists()) getAllPath(sdCard)

        }
        setTheData(1)


        // check for length in file_icon
        duplicatedFilesUsingLength()
        // check for mime type
        duplicateFileType()
        // check for hash using "SHA-512"
        duplicatedFilesUsingHashTable(duplicateList)
        for (duplicate in duplicateList.values) {
            if (duplicate.size > 1) {
                listPath.addAll(duplicate)
                setTheData(2)
            }
        }

        apkFile()
        cacheData(internal_Visible, external_Visible)
        emptyFolder()

    }

    private fun setTheData(type: Int) {
        if (listPath.isNotEmpty()) {
            var long: Long

            for (i in listPath) {
                long = -1
                totalSize += long
                longSize.add(long)
                if (setsBoolean.isEmpty() && type == 2) {
                    setsBoolean.add(false)
                } else {
                    setsBoolean.add(true)
                }

            }

            separateList = ArrayList(listOf("WhatsApp (Cache)", "Duplicate", "Apk", "App (Memory)",
                    "Obsolete Folder"))
            val heading: String
            when (type) {
                2 -> {
                    countDuplicate++
                    heading = "${separateList[1]} ($countDuplicate)"
                    listHeader.add(heading)
                    listHeaderChild[countAll] = ArrayList(listPath)
                }
                1, 3, 4, 5 -> {
                    heading = separateList[type - 1]
                    listHeader.add(heading)
                    listHeaderChild[countAll] = ArrayList(listPath)
                }

            }

            checkBoxArray[countAll] = ArrayList(setsBoolean)
            arrowImageResource.add(R.drawable.arrow_up_icon)
            storingSizeArray[countAll] = ArrayList(longSize)
            countAll++
            listPath.clear()
            longSize.clear()
            setsBoolean.clear()
        }

    }

    private fun whatsAppUnnecessaryData(uri: Uri) {
        val folderName = ArrayList(listOf(".Shared", ".Trash", "cache", ".Thumbs", "Backups", "Databases"))
        if (DocumentFile.fromTreeUri(context!!, uri)?.findFile("WhatsApp") != null) {
            val whatsAppDoc = DocumentFile.fromTreeUri(context!!, uri)?.findFile("WhatsApp")
            for (j in 0..5) {
                if (whatsAppDoc!!.findFile(folderName[j]) == null) continue
                val folderDoc = whatsAppDoc.findFile(folderName[j])

                if (folderDoc!!.exists()) {
                    if (j != 5) {
                        saveToArray(folderDoc)
                    } else {
                        if (folderDoc.listFiles().size > 1) {
                            var files: MutableList<DocumentFile> = java.util.ArrayList(listOf(*folderDoc.listFiles()))
                            files = convertIntoLastModified(files)
                            if (files.size > 1) {
                                for (i in files.size - 1 downTo 1) {
                                    saveToArray(files[i])
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun saveToArray(folderDoc: DocumentFile) {
        val getPath = folderDoc.uri.toString()
        listPath.add(getPath)
        rejectedFolder.add(getPath)
    }

    private fun convertIntoLastModified(files: MutableList<DocumentFile>): MutableList<DocumentFile> {
        var tempFile: DocumentFile
        for (i in files.indices) {
            for (j in i until files.size - 1) {
                val date1 = Date(files[i].lastModified())
                val date2 = Date(files[j + 1].lastModified())
                if (date1 < date2) {
                    tempFile = files[i]
                    files[i] = files[j + 1]
                    files[j + 1] = tempFile
                }
            }
        }
        return files
    }


    private fun duplicatedFilesUsingLength() {
        val getAllDataLength = ArrayList<Long>()
        for (data in getAllData) {
            getAllDataLength.add(data.length())
        }
        for (i in getAllDataLength.size - 1 downTo 0) {
            for (j in getAllDataLength.indices) {
                if (i != j) {
                    if (getAllDataLength[i] == getAllDataLength[j]) break
                    if (j == getAllDataLength.size - 1) {
                        getAllDataLength.removeAt(i)
                        getAllData.removeAt(i)
                    }
                }
            }
        }

    }

    private fun duplicateFileType() {
        val getAllDataType = ArrayList<String?>()
        for (data in getAllData) {
            getAllDataType.add(getMimeType(data.uri))
        }
        for (i in getAllDataType.size - 1 downTo 0) {
            for (j in getAllDataType.indices) {
                if (i != j) {
                    if (getAllDataType[i] == null || getAllDataType[i] == getAllDataType[j]) break
                    if (j == getAllDataType.size - 1) {
                        getAllDataType.removeAt(i)
                        getAllData.removeAt(i)
                    }
                }
            }
        }
    }

    private fun getMimeType(uri: Uri): String? {
        val mimeType: String?
        mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = context!!.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase(Locale.getDefault()))
        }
        return mimeType
    }

    private fun duplicatedFilesUsingHashTable(lists: MutableMap<String, MutableList<String>>) {
        for (child in getAllData) {
            try {
                val fileData = ByteArray(child.length().toInt())
                val uniqueFileHash = BigInteger(1, messageDigest!!.digest(fileData)).toString(16)
                var list = lists[uniqueFileHash]
                if (list == null) {
                    list = LinkedList()
                    lists[uniqueFileHash] = list
                }
                list.add(child.uri.toString())
            } catch (ignored: IOException) {
            }
        }
    }

    private fun getAllPath(directory: DocumentFile) {
        try {
            var getName: String
            var getExt: String
            var getChildUri: String
            for (child in directory.listFiles()) {
                getChildUri = child.uri.toString()
                if (child.isDirectory) {
                    if (!isRejectedFolder(getChildUri)) {
                        if (child.listFiles().isNullOrEmpty()) {
                            emptyFolder.add(getChildUri)
                        } else {
                            when (checkForLogCache(child.name.toString())) {
                                1 -> logFolder.add(getChildUri)
                                2 -> logFolder.add(getChildUri)
                                else -> getAllPath(child)
                            }
                        }
                    }
                } else {
                    getName = child.name!!
                    getExt = getExtension(getName)
                    if (getExt == "apk") apkFile.add(getChildUri)
                    else if (getName != ".nomedia") getAllData.add(child)
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun isRejectedFolder(path: String): Boolean {
        for (i in rejectedFolder.size - 1 downTo 0) {
            if (path == rejectedFolder[i]) {
                rejectedFolder.removeAt(i)
                return true
            }
        }
        return false
    }

    private fun cacheData(internal_Visible: Int, external_Visible: Int) {
        val savePath = ArrayList<String>()
        if (internal_Visible == View.VISIBLE) {
            savePath.add(externalUri!!)
        }
        if (external_Visible == View.VISIBLE) {
            savePath.add(sdCardUri!!)
        }
        for (path in savePath) {
            val mainPath = DocumentFile.fromTreeUri(context!!, Uri.parse(path))
            val androidDataFolder = mainPath!!.findFile("Android")!!.findFile("data")
            if (androidDataFolder!!.exists()) {
                val filesList = androidDataFolder.listFiles()
                for (folder in filesList) {
                    moreCacheData(folder)
                }
            }
        }
        setTheData(4)
    }

    private fun moreCacheData(folder: DocumentFile) {
        for (file in folder.listFiles()) {
            if (file.isDirectory) {
                val fileUri = file.uri.toString()
                if (file.listFiles().isEmpty()) {
                    emptyFolder.add(fileUri)
                    continue
                } else {
                    when (checkForLogCache(file.name.toString())) {
                        1 -> logFolder.add(fileUri)
                        2 -> listPath.add(fileUri)
                        else -> moreCacheData(file)
                    }
                }
            }
        }
    }

    private fun checkForLogCache(name: String): Int {
        val logCacheNameArray = listOf("Log", "log", "cache", "Cache")

        logCacheNameArray.forEachIndexed { i, v ->
            if (i <= 1) {
                if (name.contains(v)) return 1
            } else {
                if (name.contains(v)) return 2
            }
        }
        return 0
    }

    private fun emptyFolder() {
        listPath.addAll(logFolder)
        listPath.addAll(emptyFolder)
        setTheData(5)
    }

    private fun apkFile() {
        listPath.addAll(apkFile)
        setTheData(3)
    }

    private fun getExtension(path: String): String {
        val i = path.lastIndexOf('.')
        var extension = ""
        if (i > 0) {
            extension = path.substring(i + 1)
        }
        return extension
    }

    init {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512")
        } catch (e: NoSuchAlgorithmException) {
            CustomToast.toastIt(context!!, "cannot initialize SHA-512 hash function")
        }
    }

}