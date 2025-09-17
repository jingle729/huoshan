package com.effectsar.labcv.ebox.base

import com.effectsar.labcv.common.ebox.EBoxEffectType
import com.effectsar.labcv.ebox.Panel

/**
 *Author: gaojin.ivy
 *Time: 2025/6/3 10:50
 */

data class EBoxPageItem(
    val type: EBoxEffectType,
    val panel: Panel? = null
)
