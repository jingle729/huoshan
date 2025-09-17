package com.effectsar.labcv.ebox.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 *Author: gaojin.ivy
 *Time: 2025/6/18 12:14
 */

class EboxPerfViewModel(activity: FragmentActivity) : BaseViewModel(activity) {
    companion object {
        fun get(activity: FragmentActivity): EboxPerfViewModel {
            return EffectOneViewModelFactory.Companion.viewModelProvider(activity).get(EboxPerfViewModel::class.java)
        }
    }

    private val _fpsValue = MutableLiveData<Int>()
    val fpsValue: LiveData<Int> = _fpsValue
    fun fpsValue(fps: Int) {
        _fpsValue.value = fps
    }

    private val _drawFrameCostTime = MutableLiveData<Long>()
    val drawFrameCostTime: LiveData<Long> = _drawFrameCostTime
    fun drawFrameCostTime(time: Long) {
        _drawFrameCostTime.value = time
    }

    private val _resolution = MutableLiveData<ResolutionSize>()
    val resolution: LiveData<ResolutionSize> = _resolution
    fun resolution(size: ResolutionSize) {
        _resolution.value = size
    }
}

class ResolutionSize {
    var width = 0
    var height = 0
}