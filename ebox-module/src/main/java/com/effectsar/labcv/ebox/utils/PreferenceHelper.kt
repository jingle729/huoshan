package com.effectsar.labcv.ebox.utils

import android.preference.PreferenceManager
import com.volcengine.effectone.singleton.AppSingleton

/**
 *Author: gaojin.ivy
 *Time: 2025/3/18 19:40
 */

object PreferenceHelper {

    private val sp by lazy {
        PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance)
    }

    fun get(key: String): String {
        return sp.getString(key, null) ?: ""
    }
}