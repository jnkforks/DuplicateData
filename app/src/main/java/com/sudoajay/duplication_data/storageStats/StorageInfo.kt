package com.sudoajay.duplication_data.storageStats

import android.content.Context
import android.os.Build
import android.os.StatFs
import com.sudoajay.duplication_data.helperClass.FileSize
import com.sudoajay.duplication_data.permission.AndroidExternalStoragePermission
import com.sudoajay.duplication_data.permission.AndroidSdCardPermission
import com.sudoajay.duplication_data.sharedPreferences.SdCardPathSharedPreference
import java.io.File


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
                val externalPath = AndroidExternalStoragePermission.getExternalPath(context)
                internalAvailableSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    val stat = StatFs(externalPath)
                    val blockSize = stat.blockSizeLong
                    val availableBlocks = stat.availableBlocksLong
                    availableBlocks * blockSize
                } else {
                    val file = File(externalPath.toString())
                    file.freeSpace
                }
            } catch (ignored: Exception) {
            }
        }

    val usedInternalMemorySize: String
        get() = try {
            FileSize.convertIt(internalTotalSize - internalAvailableSize)
        } catch (ignored: Exception) {
            "0.00 GB"
        }

    val totalInternalMemorySize: String
        get() {
            val externalPath: String? = AndroidExternalStoragePermission.getExternalPath(context)
            internalTotalSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val stat = StatFs(externalPath)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                totalBlocks * blockSize
            } else {
                val file = File(externalPath.toString())
                file.totalSpace
            }
            return FileSize.convertIt(internalTotalSize)
        }

    val availableInternalPercentage: String
        get() = FileSize.getDecimal2Round((internalAvailableSize * 100).toDouble() / internalTotalSize).also { availableInternalPercent = it }

    val usedInternalPercentage: String
        get() = FileSize.getDecimal2Round((internalTotalSize - internalAvailableSize).toDouble() * 100 / internalTotalSize)

    private val availableExternalMemorySize: Unit
        get() {
            if (externalMemoryExist()) {
                try {
                    sdCardPath = sdCardPathSharedPreference.sdCardPath
                    externalAvailableSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        val stat = StatFs(sdCardPath)
                        val blockSize = stat.blockSizeLong
                        val availableBlocks = stat.availableBlocksLong
                        availableBlocks * blockSize
                    } else {
                        val file = File(sdCardPath)
                        file.freeSpace
                    }
                } catch (ignored: Exception) {
                    externalAvailableSize = 0
                }
            } else {
                externalAvailableSize = 0
            }
        }

    val usedExternalMemorySize: String
        get() = try {
            if (externalMemoryExist()) FileSize.convertIt(externalTotalSize - externalAvailableSize) else {
                "0.00 GB"
            }
        } catch (ignored: Exception) {
            "0.00 GB"
        }

    val totalExternalMemorySize: String
        get() = if (externalMemoryExist()) {
            try {
                sdCardPath = sdCardPathSharedPreference.sdCardPath
                externalTotalSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    val stat = StatFs(sdCardPath)
                    val blockSize = stat.blockSizeLong
                    val totalBlocks = stat.blockCountLong
                    totalBlocks * blockSize
                } else {
                    val file = File(sdCardPath)
                    file.totalSpace
                }
                FileSize.convertIt(externalTotalSize)
            } catch (ignored: Exception) {
                "0.0 GB"
            }
        } else {
            "0.0 GB"
        }

    val availableExternalPercentage: String
        get() = FileSize.getDecimal2Round((externalAvailableSize * 100).toDouble() / externalTotalSize)

    val usedExternalPercentage: String
        get() = FileSize.getDecimal2Round((externalTotalSize - externalAvailableSize).toDouble() * 100 / externalTotalSize)



    // get sd card path url constructor
    init {
        sdCardPath = sdCardPathSharedPreference.sdCardPath
        availableInternalMemorySize
        availableExternalMemorySize

    }
}