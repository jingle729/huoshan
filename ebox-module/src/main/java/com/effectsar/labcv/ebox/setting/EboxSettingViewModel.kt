package com.effectsar.labcv.ebox.setting

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 *Author: gaojin.ivy
 *Time: 2025/6/17 21:25
 */

class EboxSettingViewModel(activity: FragmentActivity) : BaseViewModel(activity) {
    companion object {
        fun get(activity: FragmentActivity): EboxSettingViewModel {
            return EffectOneViewModelFactory.Companion.viewModelProvider(activity).get(EboxSettingViewModel::class.java)
        }
    }

    private val _resolution = MutableLiveData<Int>()
    val resolution: LiveData<Int> = _resolution
    fun changeResolution(resolutionId: Int) {
        _resolution.value = resolutionId
    }
}