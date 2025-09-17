package com.effectsar.labcv.ebox.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bytedance.keva.KevaBuilder
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.blur.BgBlurViewModel
import com.effectsar.labcv.ebox.helper.BottomRightButtonUIHelper
import com.effectsar.labcv.ebox.helper.BottomLeftButtonHelper
import com.effectsar.labcv.ebox.helper.GestureViewHelper
import com.effectsar.labcv.ebox.helper.MenuListUIHelper
import com.effectsar.labcv.ebox.helper.RecordButtonHelper
import com.effectsar.labcv.ebox.helper.RecordPerfViewHelper
import com.effectsar.labcv.ebox.helper.RecordRootViewHelper
import com.effectsar.labcv.ebox.helper.StickerTipsViewHelper
import com.effectsar.labcv.ebox.image.ImageQualityViewModel
import com.effectsar.labcv.ebox.makeup.StyleMakeUpViewModel
import com.effectsar.labcv.ebox.matting.EboxMattingViewModel
import com.effectsar.labcv.ebox.vm.EboxRecordEffectViewModel
import com.volcengine.effectone.beauty.BeautyViewModel
import com.volcengine.effectone.filter.FilterViewModel
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.sticker.EOBaseStickerViewModel

/**
 *Author: gaojin.ivy
 *Time: 2025/5/29 16:37
 */

class EBoxRecordFragment : Fragment() {

    //用于和EffectManager交互
    private val recordEffectViewModel by lazy { EboxRecordEffectViewModel.get(requireActivity()) }

    //美颜美型数据操作
    private val beautyViewModel by lazy { BeautyViewModel.get(requireActivity()) }

    //贴纸相关数据操作「贴纸」「直播礼物」「小游戏」「虚拟头像」等都是走这个
    private val recorderStickerViewModel by lazy { EOBaseStickerViewModel.get(requireActivity()) }

    //滤镜相关数据操作
    private val filterViewModel by lazy { FilterViewModel.get(requireActivity()) }

    //画质数据操作
    private val imageQualityViewModel by lazy { ImageQualityViewModel.get(requireActivity()) }

    //虚拟背景
    private val mattingViewModel by lazy { EboxMattingViewModel.get(requireActivity()) }

    //背景虚化
    private val bgBlurViewModel by lazy { BgBlurViewModel.get(requireActivity()) }

    //风格妆
    private val styleMakeUpViewModel by lazy { StyleMakeUpViewModel.get(requireActivity()) }

    private val uiHelperList by lazy {
        mutableListOf(
            RecordRootViewHelper(requireActivity(), viewLifecycleOwner),
            RecordButtonHelper(requireActivity(), viewLifecycleOwner),
            BottomRightButtonUIHelper(requireActivity(), viewLifecycleOwner),
            BottomLeftButtonHelper(requireActivity(), viewLifecycleOwner),
            MenuListUIHelper(requireActivity(), viewLifecycleOwner),
            RecordPerfViewHelper(requireActivity(), viewLifecycleOwner),
            StickerTipsViewHelper(requireActivity(), viewLifecycleOwner),
            GestureViewHelper(requireActivity(), viewLifecycleOwner),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KevaBuilder.getInstance().setContext(AppSingleton.instance)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ebox_record_root, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        uiHelperList.forEach {
            requireActivity().lifecycle.addObserver(it)
        }
        super.onViewCreated(view, savedInstanceState)
        uiHelperList.forEach {
            it.initView(view as ViewGroup)
        }
        initObserver()
    }

    private fun initObserver() {

        beautyViewModel.beautyChanged.observe(viewLifecycleOwner) {
            val nodeList = beautyViewModel.buildComposerList()
            recordEffectViewModel.setBeauty(nodeList)
        }

        beautyViewModel.beautyIntensityChanged.observe(viewLifecycleOwner) {
            it?.let { node ->
                recordEffectViewModel.updateBeauty(node)
            }
        }

        recorderStickerViewModel.selectedItem.observe(viewLifecycleOwner) {
            it?.let {
                val stickerPath = it.path()
                recordEffectViewModel.setSticker(stickerPath)
                if (stickerPath.isNotEmpty()) {
                    beautyViewModel.updateBeautyConflictWithSticker(true)
                } else {
                    beautyViewModel.updateBeautyConflictWithSticker(false)
                }
                beautyViewModel.notifyBeautyChanged()
            }
        }

        filterViewModel.filterSelected.observe(viewLifecycleOwner) {
            it?.let { filterItem ->
                recordEffectViewModel.setFilter(filterItem)
            }
        }

        filterViewModel.filterIntensity.observe(viewLifecycleOwner) {
            it?.let { filterIntensity ->
                recordEffectViewModel.updateFilterIntensity(filterIntensity.intensity)
            }
        }

        imageQualityViewModel.itemChanged.observe(viewLifecycleOwner) {
            it?.let { item ->
                val nodeList = imageQualityViewModel.buildComposeNodeList()
                recordEffectViewModel.setImageQuality(nodeList)
            }
        }

        imageQualityViewModel.itemIntensity.observe(viewLifecycleOwner) {
            it?.let { node ->
                recordEffectViewModel.updateBeauty(node)
            }
        }

        mattingViewModel.mattingSelectedItem.observe(viewLifecycleOwner) {
            it?.let { item ->
                recordEffectViewModel.setMattingBg(item)
            }
        }

        bgBlurViewModel.itemSelected.observe(viewLifecycleOwner) {
            it?.let { item ->
                recordEffectViewModel.setBackgroundBlur(item.composeNodeList)
            }
        }

        bgBlurViewModel.itemIntensity.observe(viewLifecycleOwner) {
            it?.let { node ->
                recordEffectViewModel.updateBeauty(node)
            }
        }

        styleMakeUpViewModel.itemSelected.observe(viewLifecycleOwner) {
            it?.let { item ->
                recordEffectViewModel.setStyleMakeup(item.composeNodeList)
            }
        }

        styleMakeUpViewModel.itemIntensity.observe(viewLifecycleOwner) {
            it?.let { node ->
                recordEffectViewModel.updateBeauty(node)
            }
        }
    }
}