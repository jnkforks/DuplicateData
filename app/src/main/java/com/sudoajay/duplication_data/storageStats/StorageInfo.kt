package com.sudoajay.duplication_data.storageStats

import android.content.Context
import android.os.Build
import android.os.StatFs
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import java.io.File
import java.text.DecimalFormat

class StorageInfo(val context: Context) {
    private var sdCardPath: String = ""
    var availableInternalPercent: String = ""
    var internalAvailableSize: Long = 0
    var internalTotalSize: Long = 0
    var externalAvailableSize: Long = 0
    var externalTotalSize: Long = 0
    private var androidSdCardPermission: AndroidSdCardPermission = AndroidSdCardPermission(context)
    private val sdCardPathSharedPreference = SdCardPathSharedPreference(context)


    private fun externalMemoryExist(): Boolean {
        return if (Build.VERSION.SDK_INT <= 22) File(sdCardPath).exists()
        else androidSdCardPermission.isSdStorageWritable

    }

    private val availableInternalMemorySize: Unit
        get() {
            try {

                val path = AndroidExternalStoragePermission.getExternalPath(context)
                val stat = StatFs(path)
                val blockSize = stat.blockSizeLong
                val availableBlocks = stat.availableBlocksLong
                internalAvailableSize = availableBlocks * blockSize
            } catch (ignored: Exception) {
            }
        }

    val usedInternalMemorySize: String
        get() = try {
            convertIt(internalTotalSize - internalAvailableSize)
        } catch (ignored: Exception) {
            "0.00 GB"
        }

    val totalInternalMemorySize: String
        get() {
            val path: String? = AndroidExternalStoragePermission.getExternalPath(context)
            val stat = StatFs(path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            internalTotalSize = totalBlocks * blockSize
            return convertIt(internalTotalSize)
        }

    val availableInternalPercentage: String
        get() = getDecimal2Round((internalAvailableSize * 100).toDouble() / internalTotalSize).also { availableInternalPercent = it }

    val usedInternalPercentage: String
        get() = getDecimal2Round((internalTotalSize - internalAvailableSize).toDouble() * 100 / internalTotalSize)

    private val availableExternalMemorySize: Unit
        get() {
            externalAvailableSize = if (externalMemoryExist()) {
                try {
                    sdCardPath = sdCardPathSharedPreference.sdCardPath.toString()
                    val stat = StatFs(sdCardPath)
                    val blockSize = stat.blockSizeLong
                    val availableBlocks = stat.availableBlocksLong
                    availableBlocks * blockSize
                } catch (ignored: Exception) {
                    0
                } as Long
            } else {
                0
            }
        }

    val usedExternalMemorySize: String
        get() = try {
            if (externalMemoryExist()) convertIt(externalTotalSize - externalAvailableSize) else {
                "0.00 GB"
            }
        } catch (ignored: Exception) {
            "0.00 GB"
        }

    val totalExternalMemorySize: String
        get() = if (externalMemoryExist()) {
            try {
                sdCardPath = sdCardPathSharedPreference.sdCardPath.toString()
                val stat = StatFs(sdCardPath)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                externalTotalSize = totalBlocks * blockSize
                convertIt(externalTotalSize)
            } catch (ignored: Exception) {
                "0.0 GB"
            }
        } else {
            "0.0 GB"
        }

    val availableExternalPercentage: String
        get() = getDecimal2Round((externalAvailableSize * 100).toDouble() / externalTotalSize)

    val usedExternalPercentage: String
        get() = getDecimal2Round((externalTotalSize - externalAvailableSize).toDouble() * 100 / externalTotalSize)

    companion object {
        @JvmStatic
        fun convertIt(size: Long): String {
            return try {
                when {
                    size > 1024 * 1024 * 1024 -> { // GB
                        getDecimal2Round(size.toDouble() / (1024 * 1024 * 1024)) + " GB"
                    }
                    size > 1024 * 1024 -> { // MB
                        getDecimal2Round(size.toDouble() / (1024 * 1024)) + " MB"
                    }
                    else -> { // KB
                        getDecimal2Round(size.toDouble() / 1024) + " KB"
                    }
                }
            } catch (e: Exception) {
                ""
            }
        }


        private fun getDecimal2Round(time: Double): String {
            val df = DecimalFormat("#.#")
            return java.lang.Double.valueOf(df.format(time)).toString()
        }
    }

    // get sd card path url constructor
    init {
        sdCardPath = sdCardPathSharedPreference.sdCardPath.toString()

        availableInternalMemorySize
        availableExternalMemorySize

    }
}