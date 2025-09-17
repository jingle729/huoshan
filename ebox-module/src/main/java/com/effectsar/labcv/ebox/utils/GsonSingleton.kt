package com.effectsar.labcv.ebox.utils

import com.google.gson.Gson

/**
 *Author: gaojin.ivy
 *Time: 2025/5/28 17:41
 */

object GsonSingleton {
    val gson by lazy { Gson() }
}