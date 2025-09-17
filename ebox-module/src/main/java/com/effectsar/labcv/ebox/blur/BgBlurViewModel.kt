package com.effectsar.labcv.ebox.blur

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.volcengine.effectone.InnerEffectOneConfigList.getConfig
import com.volcengine.effectone.base.ComposerNode
import com.volcengine.effectone.viewmodel.BasePanelViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 *Author: gaojin
 *Time: 2023/11/2 14:32
 */

class BgBlurViewModel(activity: FragmentActivity) : BasePanelViewModel<BgBlurUIConfig>(activity) {

    companion object {
        fun get(activity: FragmentActivity): BgBlurViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(BgBlurViewModel::class.java)
        }
    }

    override val uiConfig: BgBlurUIConfig? = getConfig()

    val itemList = mutableListOf<BgBlurItem>()

    private val _itemSelected = MutableLiveData<BgBlurItem>()
    val itemSelected: LiveData<BgBlurItem> = _itemSelected
    fun selectItem(item: BgBlurItem) {
        _itemSelected.value = item
    }

    private val _currentVisibleItem = MutableLiveData<BgBlurItem?>()
    val currentVisibleItem: LiveData<BgBlurItem?> = _currentVisibleItem
    fun setCurrentVisibleItem(item: BgBlurItem?) {
        _currentVisibleItem.value = item
    }

    private val _itemIntensity = MutableLiveData<ComposerNode>()
    val itemIntensity: LiveData<ComposerNode> = _itemIntensity
    fun updateIntensity(node: ComposerNode) {
        _itemIntensity.value = node
    }

    private val _currentVisibleComposerNode = MutableLiveData<ComposerNode?>()
    val currentVisibleComposerNode: LiveData<ComposerNode?> = _currentVisibleComposerNode
    fun setCurrentVisibleComposerNode(node: ComposerNode?) {
        _currentVisibleComposerNode.value = node
    }
}