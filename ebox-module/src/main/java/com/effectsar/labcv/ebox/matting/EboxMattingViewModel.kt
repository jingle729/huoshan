package com.effectsar.labcv.ebox.matting

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.effectsar.labcv.ebox.image.ImageQualityUIConfig
import com.volcengine.effectone.InnerEffectOneConfigList.getConfig
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.viewmodel.BasePanelViewModel
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 *Author: gaojin.ivy
 *Time: 2025/6/17 21:25
 */

class EboxMattingViewModel(activity: FragmentActivity) : BasePanelViewModel<MattingUIConfig>(activity) {

    companion object {
        fun get(activity: FragmentActivity): EboxMattingViewModel {
            return EffectOneViewModelFactory.Companion.viewModelProvider(activity).get(EboxMattingViewModel::class.java)
        }
    }

    override val uiConfig: MattingUIConfig? = getConfig()

    private var initMattingItem: MattingItem? = null

    private val _mattingSelectedItem = MutableLiveData<MattingItem>()
    val mattingSelectedItem: LiveData<MattingItem> = _mattingSelectedItem

    fun initMattingItem(item: MattingItem) {
        initMattingItem = item
        selectMattingItem(item)
    }

    fun hasData() = initMattingItem != null

    private fun selectMattingItem(item: MattingItem) {
        _mattingSelectedItem.value = item
    }

    fun updateCustomBg(mediaItem: IMediaItem) {
        val currentMattingItem = initMattingItem ?: return
        val newItem = currentMattingItem.copy(defaultPath = "", customBg = mediaItem)
        selectMattingItem(newItem)
    }

    fun changeMattingState() {
        val currentItem = mattingSelectedItem.value ?: return
        if (currentItem.isEmpty()) {
            initMattingItem?.let { selectMattingItem(it) }
        } else {
            selectMattingItem(MattingItem())
        }
    }
}