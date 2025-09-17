package com.effectsar.labcv.ebox

import com.effectsar.labcv.ebox.utils.GsonSingleton

/**
 *Author: gaojin.ivy
 *Time: 2025/5/28 17:43
 */

object ParseUtils {
    fun parse(json: String): MainPageConfig {
        return GsonSingleton.gson.fromJson(json, MainPageConfig::class.java)
    }
}