package com.effectsar.labcv.ebox.image

import com.volcengine.effectone.base.ComposerNode
import com.volcengine.effectone.data.IEOResourceItem

/**
 *Author: gaojin.ivy
 *Time: 2025/6/15 15:08
 */

data class ImageQualityItem(
    val resource: IEOResourceItem?,
    val composeNodeList: List<ComposerNode>,
    var selected: Boolean = false,
    var open: Boolean = false,
) {

    companion object {
        const val STATE_REMOTE = 1
        const val STATE_DOWNLOADING = 2
        const val STATE_DOWNLOADED = 3
    }

    var state = STATE_REMOTE

    fun name() = resource?.title ?: ""

    fun path() = resource?.absPath ?: ""

    fun isClear(): Boolean {
        return resource == null
    }
}