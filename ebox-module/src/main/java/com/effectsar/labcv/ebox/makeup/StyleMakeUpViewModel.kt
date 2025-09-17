package com.effectsar.labcv.ebox.makeup

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

class StyleMakeUpViewModel(activity: FragmentActivity) : BasePanelViewModel<StyleMakeUpUIConfig>(activity) {

    companion object {
        fun get(activity: FragmentActivity): StyleMakeUpViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(StyleMakeUpViewModel::class.java)
        }
    }

    override val uiConfig: StyleMakeUpUIConfig? = getConfig()

    val itemList = mutableListOf<StyleMakeUpItem>()

    private val _itemSelected = MutableLiveData<StyleMakeUpItem>()
    val itemSelected: LiveData<StyleMakeUpItem> = _itemSelected
    fun selectItem(item: StyleMakeUpItem) {
        _itemSelected.value = item
    }

    private val _currentVisibleItem = MutableLiveData<StyleMakeUpItem?>()
    val currentVisibleItem: LiveData<StyleMakeUpItem?> = _currentVisibleItem
    fun setCurrentVisibleItem(item: StyleMakeUpItem?) {
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