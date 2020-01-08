package com.sudoajay.duplication_data.duplicationData

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.helperClass.DocumentHelperClass
import com.sudoajay.duplication_data.helperClass.FileUtils
import java.io.File


@Suppress("NAME_SHADOWING", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "ObsoleteSdkInt")
class ExpandableDuplicateListAdapter internal constructor(private val showDuplicate: ShowDuplicate, private val listHeader: MutableList<String>, private val listHeaderChild: LinkedHashMap<Int, List<String>>, private val arrowImageResource: List<Int>,
                                                          val checkBoxArray: MutableMap<Int, List<Boolean>>, private var storingSizeArray: MutableMap<Int, List<Long>>) : BaseExpandableListAdapter() {
    private val context: Context = showDuplicate.applicationContext

    override fun getGroupCount(): Int {
        return listHeader.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return listHeaderChild[groupPosition]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return listHeader[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listHeaderChild[groupPosition]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {

        var convertView = convertView
        val headerTitle = getGroup(groupPosition) as String
        if (convertView == null) {
            val inflateInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflateInflater.inflate(R.layout.activity_duplication_list_view, null)
        }


        val arrowImageView = convertView!!.findViewById<ImageView>(R.id.arrow_Image_View)
        val groupHeadingTextView = convertView.findViewById<TextView>(R.id.group_Heading_Text_View)
        val countTextView = convertView.findViewById<TextView>(R.id.count_Text_View)
        val groupSizeTextView = convertView.findViewById<TextView>(R.id.group_Size_Text_View)

        countTextView!!.text = "" + getChildrenCount(groupPosition)
        groupHeadingTextView!!.text = headerTitle
        arrowImageView!!.setImageResource(arrowImageResource[groupPosition])


        var dataSize: Long = 0

        for (i in storingSizeArray[groupPosition]!!.indices) {
            dataSize += storingSizeArray[groupPosition]!![i]
        }
        if (dataSize == -1L) {
            groupSizeTextView!!.text = "( Calc...  )"
        } else {
            groupSizeTextView!!.text = "(" + convertIt(dataSize) + ")"
        }

        return convertView
    }

    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val headerTitle = getGroup(groupPosition) as String
        val headerChildren = getChild(groupPosition, childPosition) as String

        if (convertView == null) {
            val inflateInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflateInflater.inflate(R.layout.activity_duplication_under_list_view, null)
        }
        var getName: String?

        val nameTextView = convertView!!.findViewById<TextView>(R.id.nameTextView)
        val coverImageView = convertView.findViewById<ImageView>(R.id.coverImageView)
        val pathTextView = convertView.findViewById<TextView>(R.id.pathTextView)
        val checkBoxView = convertView.findViewById<CheckBox>(R.id.checkBoxView)
        var changeValue: MutableList<Boolean>

        //      Its supports till android 9 & api 28
        if (Build.VERSION.SDK_INT <= showDuplicate.getString(R.string.apiLevel).toInt()) {
            val file = File(headerChildren)
            pathTextView.text = headerChildren

            when (headerTitle) {
                showDuplicate.scanDuplicateDataWithFile!!.separateList[3] -> {
                    getName = headerChildren.split("/Android/data/")[1]
                    getName = getName.split("/")[0]
                    nameTextView.text = getApplicationName(getName)
                    appIcon(getName, coverImageView)
                }
                showDuplicate.scanDuplicateDataWithFile!!.separateList[2] -> nameTextView.text = getImageFromApk(file.absolutePath, coverImageView)

                else -> {
                    nameTextView.text = file.name
                    getExtension(headerChildren, coverImageView)
                }
            }
        } else {
            val fileUri = Uri.parse(headerChildren)
            val file = DocumentFile.fromSingleUri(context, fileUri)

            pathTextView.text = FileUtils.replaceSdCardPath(context, FileUtils.getPath(context, fileUri))


            when (headerTitle) {
                showDuplicate.scanDuplicateDataWithDoc!!.separateList[3] -> {
                    getName = FileUtils.replaceSdCardPath(context, FileUtils.getPath(context, fileUri)).split("/Android/data/")[1]
                    getName = getName.split("/")[0]
                    nameTextView.text = getApplicationName(getName)
                    appIcon(getName, coverImageView)
                }
                showDuplicate.scanDuplicateDataWithDoc!!.separateList[2] -> nameTextView.text = getImageFromApk(FileUtils.replaceSdCardPath(context, FileUtils.getPath(context, fileUri)), coverImageView)

                else -> {
                    nameTextView.text = file!!.name
                    getExtension(headerChildren, coverImageView)
                }
            }

        }

        checkBoxView.setOnClickListener {
            if (!checkBoxView.isChecked) {
                checkBoxView.isChecked = false

                changeValue = checkBoxArray[groupPosition] as MutableList<Boolean>
                changeValue[childPosition] = false
                checkBoxArray[groupPosition] = changeValue

                showDuplicate.totalSizeChange("sub", storingSizeArray[groupPosition]!![childPosition])

            } else {
                checkBoxView.isChecked = true

                changeValue = checkBoxArray[groupPosition] as MutableList<Boolean>
                changeValue[childPosition] = true
                checkBoxArray[groupPosition] = changeValue

                showDuplicate.totalSizeChange("add", storingSizeArray[groupPosition]!![childPosition])

            }
        }
        checkBoxView.isChecked = checkBoxArray[groupPosition]!![childPosition]

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    private fun convertIt(size: Long): String {
        return when {
            size > 1024 * 1024 * 1024 -> { // GB
                convertToDecimal(size.toFloat() / (1024 * 1024 * 1024)) + " GB"
            }
            size > 1024 * 1024 -> { // MB
                convertToDecimal(size.toFloat() / (1024 * 1024)) + " MB"
            }
            else -> { // KB
                convertToDecimal(size.toFloat() / 1024) + " KB"
            }
        }
    }

    private fun convertToDecimal(value: Float): String {
        val size = value.toString() + ""
        return if (value >= 1000) {
            size.substring(0, 4)
        } else if (value >= 100) {
            size.substring(0, 3)
        } else {
            if (size.length == 2 || size.length == 3) {
                size.substring(0, 1)
            } else size.substring(0, 4)
        }
    }

    private fun getApplicationName(name: String): String {
        val pm = context.packageManager
        val ai: ApplicationInfo?
        ai = try {
            pm.getApplicationInfo(name, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String
    }


    private fun getExtension(path: String, imageView: ImageView) {
        //      Its supports till android 9 & api 28
        if (Build.VERSION.SDK_INT <= showDuplicate.getString(R.string.apiLevel).toInt()) {
            checkForExtensionFile(path, imageView)
        } else {
            checkForExtensionDoc(path, imageView)
        }

    }

    private fun checkForExtensionFile(path: String, imageView: ImageView) {
        val i = path.lastIndexOf('.')
        var extension = ""
        if (i > 0) {
            extension = path.substring(i + 1)
        }
        val file = File(path)
        if (file.isDirectory) imageView.setImageResource(R.drawable.folder_icon)
        else when (extension) {
            "jpg", "mp4", "jpeg", "png", "gif", "webp" ->  // Images || Videos
                Glide.with(context)
                        .asBitmap()
                        .load(Uri.fromFile(file))
                        .into(imageView)
            "mp3", "m4a", "amr", "aac" ->  // Audiio
                getAudioAlbumImageContentUri(imageView, path)
            "pptx", "pdf", "docx", "txt" -> imageView.setImageResource(R.drawable.document_icon)
            "opus" -> imageView.setImageResource(R.drawable.voice_icon)
            else -> imageView.setImageResource(R.drawable.file_icon)
        }
    }

    private fun checkForExtensionDoc(uri: String, imageView: ImageView) {
        val path = FileUtils.replaceSdCardPath(context, FileUtils.getPath(context, Uri.parse(uri)))

        val i = path.lastIndexOf('.')
        var extension = ""
        if (i > 0) {
            extension = path.substring(i + 1)
        }
        val file = DocumentFile.fromSingleUri(context, Uri.parse(uri))
        if (file!!.isDirectory) imageView.setImageResource(R.drawable.folder_icon)
        else {
            when (extension) {
                "jpg", "mp4", "jpeg", "png", "gif", "webp" ->  // Images || Videos
                    Glide.with(context)
                            .asBitmap()
                            .load(file.uri)
                            .into(imageView)
                "mp3", "m4a", "amr", "aac" ->  // Audio
                    getAudioAlbumImageContentUri(imageView, uri)
                "pptx", "pdf", "docx", "txt" -> imageView.setImageResource(R.drawable.document_icon)
                "opus" -> imageView.setImageResource(R.drawable.voice_icon)
                else -> imageView.setImageResource(R.drawable.file_icon)
            }
        }

    }

    private fun appIcon(path: String, imageView: ImageView) {
        try {
            val appIcon = showDuplicate.packageManager.getApplicationIcon(path)
            imageView.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            imageView.setImageResource(R.drawable.file_icon)
        }
    }

    private fun getImageFromApk(APKFilePath: String, imageView: ImageView): String {
        val pm = context.packageManager
        val pi = pm.getPackageArchiveInfo(APKFilePath, 0)!!
        pi.applicationInfo.sourceDir = APKFilePath
        pi.applicationInfo.publicSourceDir = APKFilePath
        //
        val apkIcon = pi.applicationInfo.loadIcon(pm)
        val appName = pi.applicationInfo.loadLabel(pm) as String
        imageView.setImageDrawable(apkIcon)
        return appName
    }

    private fun getAudioAlbumImageContentUri(imageView: ImageView, uri: String) {
        try {
            val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.DATA + "=? "
            val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID)
            val cursor = context.contentResolver.query(
                    audioUri,
                    projection,
                    selection, arrayOf(uri), null)
            if (cursor != null && cursor.moveToFirst()) {
                val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                if (getCover(albumId) != null) {
                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val imgUri = ContentUris.withAppendedId(sArtworkUri,
                            albumId)
                    Glide.with(context)
                            .load(imgUri)
                            .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.audio_icon)
                }
                cursor.close()
            }
        } catch (e: Exception) {
        }
    }

    private fun getCover(album_id: Long): Bitmap? {
        val artwork: Bitmap
        var resizedBitmap: Bitmap? = null
        try {
            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
            val uri = ContentUris.withAppendedId(sArtworkUri, album_id)
            val res = context.contentResolver
            val `in` = res.openInputStream(uri)
            artwork = BitmapFactory.decodeStream(`in`)
            val width = artwork.width
            val height = artwork.height
            val scaleWidth = 500.toFloat() / width
            val scaleHeight = 500.toFloat() / height
            // CREATE A MATRIX FOR THE MANIPULATION
            val matrix = Matrix()
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight)
            // "RECREATE" THE NEW BITMAP
            resizedBitmap = Bitmap.createBitmap(
                    artwork, 0, 0, width, height, matrix, false)
        } catch (ignored: Exception) {
        }
        return resizedBitmap
    }

    companion object {
        @JvmStatic
        fun getFileSizeInBytes(fileName: String?, context: Context): Long {

            val documentHelperClass = DocumentHelperClass(context)
            Log.e("GotSomething", fileName)
            //      Its supports till android 9 & api 28
            return if (Build.VERSION.SDK_INT <= context.getString(R.string.apiLevel).toInt()) {
                getFileSizeInBytesFile(File(fileName))
            } else {
                val documentFile = documentHelperClass.separatePath(FileUtils.replaceSdCardPath(context, FileUtils.getPath(context, Uri.parse(fileName))))

                getFileSizeInBytesDoc(documentFile)

            }

        }

        private fun getFileSizeInBytesFile(f: File): Long {

            var ret: Long = 0
            if (f.exists()) {
                if (f.isFile) {
                    return f.length()
                } else if (f.isDirectory) {
                    val contents = f.listFiles()
                    for (content in contents) {
                        if (content.isFile) {
                            ret += content.length()
                        } else if (content.isDirectory) ret += getFileSizeInBytesFile(content)
                    }
                }
            }
            return ret
        }

        private fun getFileSizeInBytesDoc(fileName: DocumentFile): Long {

            var ret: Long = 0
            if (fileName.exists()) {
                if (fileName.isFile) {
                    return fileName.length()
                } else if (fileName.isDirectory) {
                    val contents = fileName.listFiles()
                    for (content in contents) {
                        if (content.isFile) {
                            ret += content.length()
                        } else if (content.isDirectory) ret += getFileSizeInBytesDoc(content)
                    }
                }
            }
            return ret
        }
    }

}