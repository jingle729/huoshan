package com.effectsar.labcv.ebox.image

import android.util.Log
import com.effectsar.labcv.ebox.base.EboxResourceConfig
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.utils.GsonSingleton

/**
 *Author: gaojin.ivy
 *Time: 2025/6/15 15:09
 */

fun IEOResourceItem.toImageQualityItem(): ImageQualityItem {
    val configStr = (extra?.get("config") as? String) ?: ""
    val config = if (configStr.isNotEmpty()) {
        GsonSingleton.gson.fromJson(configStr, EboxResourceConfig::class.java)
    } else {
        EboxResourceConfig()
    }

    val composeNodeList = config.composerNodes.mapIndexed { index, configNode ->
        val defaultValue = configNode.floatValue()
        configNode.toComposeNode(absPath, defaultValue, index == 0)
    }

    config.titleDict?.getTitle()?.let {
        this.title = it
    }

    return ImageQualityItem(
        this,
        composeNodeList
    )
}