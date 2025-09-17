package com.effectsar.labcv.ebox.base

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.volcengine.effectone.base.ConfigComposeNode
import com.volcengine.effectone.base.TitleDict

/**
 *Author: gaojin.ivy
 *Time: 2025/6/19 11:47
 */

@Keep
data class EboxResourceConfig(
    @SerializedName("titleDict")
    val titleDict: TitleDict? = null,

    @SerializedName("composerNodes")
    val composerNodes: List<ConfigComposeNode> = emptyList(),
)
