package com.effectsar.labcv.ebox.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.effectsar.labcv.common.ebox.EBoxEffectType
import com.effectsar.labcv.common.ebox.EBoxEffectType.UNKNOWN
import com.effectsar.labcv.ebox.PageDetail
import com.effectsar.labcv.ebox.Panel
import com.effectsar.labcv.ebox.base.EBoxPageItem
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 *Author: gaojin.ivy
 *Time: 2025/5/29 20:44
 */
class EboxRecordUIViewModel(activity: FragmentActivity) : BaseViewModel(activity) {
    companion object {
        fun get(activity: FragmentActivity): EboxRecordUIViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(EboxRecordUIViewModel::class.java)
        }
    }

    private val _rootViewVisible = MutableLiveData<Boolean>()
    val rootViewVisible: LiveData<Boolean> = _rootViewVisible
    fun showOrHideRootView(show: Boolean) {
        _rootViewVisible.value = show
    }

    private val _albumViewVisible = MutableLiveData<Boolean>()
    val albumViewVisible: LiveData<Boolean> = _albumViewVisible
    fun showOrHideAlbumView(show: Boolean) {
        _albumViewVisible.value = show
    }

    private val _switchCamera = MutableLiveData<Unit>()
    val switchCamera: LiveData<Unit> = _switchCamera
    fun switchCamera() {
        _switchCamera.value = Unit
    }

    private val _takePic = MutableLiveData<Unit>()
    val takePic: LiveData<Unit> = _takePic
    fun takePic() {
        _takePic.value = Unit
    }

    private val _startAlbum = MutableLiveData<Unit>()
    val startAlbum: LiveData<Unit> = _startAlbum
    fun startAlbum() {
        _startAlbum.value = Unit
    }

    private val _closeEffect = MutableLiveData<Boolean>()
    val closeEffect: LiveData<Boolean> = _closeEffect
    fun closeEffect() {
        _closeEffect.value = true
    }

    fun openEffect() {
        _closeEffect.value = false
    }

    private val _clickEboxPageItem = MutableLiveData<EBoxPageItem>()
    val clickEboxPageItem: LiveData<EBoxPageItem> = _clickEboxPageItem
    fun clickEboxPageItem(eboxPanelItem: EBoxPageItem) {
        _clickEboxPageItem.value = eboxPanelItem
    }

    private val _showPerf = MutableLiveData<Boolean>().apply {
        value = false
    }
    val showPerf: LiveData<Boolean> = _showPerf
    fun showPerfView(show: Boolean) {
        _showPerf.value = show
    }

    private var pageDetail: PageDetail? = null
    private var leftBottomPageItem = EBoxPageItem(UNKNOWN)

    fun setPageDetail(pageDetail: PageDetail?) {
        this.pageDetail = pageDetail
        val detail = pageDetail ?: return
        val isPkg = detail.id.startsWith("pkg_")
        leftBottomPageItem = if (isPkg) {
            //套餐的处理逻辑
            getPkgPanel(detail.panels)
        } else {
            //原子能力处理逻辑
            //取第一个Panel
            firstEBoxPageItem(detail.panels)
        }
    }

    /**
     * 获取页面左下角的能力
     */
    fun getLeftBottomFun(): EBoxPageItem {
        return leftBottomPageItem
    }

    fun getMenuList(): List<EBoxPageItem> {
        val detail = pageDetail ?: return emptyList()
        val leftBottomPanelKey = leftBottomPageItem.panel?.key ?: ""
        val menuList = mutableListOf<EBoxPageItem>()
        detail.panels.forEach { panel ->
            if (!panel.key.startsWith(leftBottomPanelKey)) {
                val eBoxEffectType = EBoxEffectType.parsePanelKeyToEffectType(panel.key)
                menuList.add(EBoxPageItem(eBoxEffectType, panel))
            }
        }
        return menuList
    }

    /**
     * 左下角优先展示美颜美型, 没有话，按顺序读取
     */
    private fun getPkgPanel(panels: List<Panel>): EBoxPageItem {
        val beautyPanel = panels.firstOrNull { it.key.startsWith(EBoxEffectType.BEAUTY.key) }
        return if (beautyPanel == null) {
            firstEBoxPageItem(panels)
        } else {
            EBoxPageItem(EBoxEffectType.BEAUTY, beautyPanel)
        }
    }

    private fun firstEBoxPageItem(panels: List<Panel>): EBoxPageItem {
        val panel = panels.firstOrNull()
        val eBoxEffectType = EBoxEffectType.parsePanelKeyToEffectType(panel?.key ?: "")
        return EBoxPageItem(eBoxEffectType, panel)
    }

    fun getPageTitle() = pageDetail?.title ?: ""
}
