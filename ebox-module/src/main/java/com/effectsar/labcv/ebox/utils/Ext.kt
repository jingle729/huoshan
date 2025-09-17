package com.effectsar.labcv.ebox.utils

import android.net.Uri
import android.os.Environment
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import java.io.File

/**
 *Author: gaojin.ivy
 *Time: 2025/5/30 11:31
 */

private val externalStoragePath: String by lazy {
    Environment.getExternalStorageDirectory().path
}

fun IMediaItem.getAbsolutePath(): String {
    var absolutePath = path
    if (Uri.parse(path).scheme == "content") {
        absolutePath = externalStoragePath + File.separator + relativePath + name
    }
    return absolutePath
}