package com.effectsar.labcv.ebox.matting

import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.volcengine.effectone.data.IEOResourceItem

/**
 *Author: gaojin.ivy
 *Time: 2025/6/18 17:37
 */

data class MattingItem(
    val defaultPath: String = "",
    val customBg: IMediaItem? = null,
    val resource: IEOResourceItem? = null,
) {
    fun path() = resource?.absPath ?: ""

    fun isEmpty() = resource == null
}
