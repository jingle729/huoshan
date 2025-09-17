package com.effectsar.labcv.ebox.blur

import com.volcengine.effectone.base.ComposerNode
import com.volcengine.effectone.data.IEOResourceItem

/**
 *Author: gaojin.ivy
 *Time: 2025/6/19 11:27
 */

data class BgBlurItem(
    val resource: IEOResourceItem?,
    val composeNodeList: List<ComposerNode>
) {

    companion object {
        const val STATE_REMOTE = 1
        const val STATE_DOWNLOADING = 2
        const val STATE_DOWNLOADED = 3
    }

    var selected = false
    var state = STATE_REMOTE

    fun name() = resource?.title ?: ""

    fun path() = resource?.absPath ?: ""

    fun isClear(): Boolean {
        return resource == null
    }
}
