package com.effectsar.labcv.ebox.api

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 *Author: gaojin
 *Time: 2023/12/13 17:14
 */

interface IUIHelper : LifecycleObserver {
    val activity: FragmentActivity
    val owner: LifecycleOwner
    fun initView(rootView: ViewGroup)
}