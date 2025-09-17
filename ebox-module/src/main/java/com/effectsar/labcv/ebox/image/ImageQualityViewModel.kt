package com.effectsar.labcv.ebox.image

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

class ImageQualityViewModel(activity: FragmentActivity) : BasePanelViewModel<ImageQualityUIConfig>(activity) {

    companion object {
        fun get(activity: FragmentActivity): ImageQualityViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(ImageQualityViewModel::class.java)
        }
    }

    override val uiConfig: ImageQualityUIConfig? = getConfig()

    val imageQualityItemList = mutableListOf<ImageQualityItem>()

    private val _itemChanged = MutableLiveData<Unit>()
    val itemChanged: LiveData<Unit> = _itemChanged
    fun itemChanged() {
        _itemChanged.value = Unit
    }

    private val _currentVisibleItem = MutableLiveData<ImageQualityItem?>()
    val currentVisibleItem: LiveData<ImageQualityItem?> = _currentVisibleItem
    fun setCurrentVisibleItem(item: ImageQualityItem?) {
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

    fun buildComposeNodeList(): List<ComposerNode> {
        if (imageQualityItemList.isEmpty()) {
            return emptyList()
        }
        val clearItem = imageQualityItemList.first()
        if (clearItem.selected) {
            return emptyList()
        }
        val nodeList = mutableListOf<ComposerNode>()
        imageQualityItemList.forEach {
            if (it.open) {
                nodeList.addAll(it.composeNodeList)
            }
        }
        return nodeList
    }
}