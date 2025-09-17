package com.effectsar.labcv.ebox.blur

import com.effectsar.labcv.ebox.base.EboxResourceConfig
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.utils.GsonSingleton

/**
 *Author: gaojin.ivy
 *Time: 2025/6/19 11:40
 */

fun IEOResourceItem.toBackgroundBlurItem(): BgBlurItem {
    val configStr = (extra?.get("config") as? String) ?: ""
    val config = if (configStr.isNotEmpty()) {
        GsonSingleton.gson.fromJson(configStr, EboxResourceConfig::class.java)
    } else {
        EboxResourceConfig()
    }

    config.titleDict?.getTitle()?.let {
        this.title = it
    }

    val composeNodeList = config.composerNodes.mapIndexed { index, configNode ->
        val defaultValue = configNode.floatValue()
        configNode.toComposeNode(absPath, defaultValue, index == 0)
    }

    return BgBlurItem(
        this,
        composeNodeList
    )
}