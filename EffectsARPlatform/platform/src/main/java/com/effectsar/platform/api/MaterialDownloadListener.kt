package com.effectsar.platform.api

import com.effectsar.platform.struct.Material
import com.effectsar.platform.struct.PlatformError

interface MaterialDownloadListener {
    fun onSuccess(material: Material, path: String)
    fun onProgress(material: Material, process: Int)
    fun onFailed(material: Material, e: Exception, platformError: PlatformError)
}