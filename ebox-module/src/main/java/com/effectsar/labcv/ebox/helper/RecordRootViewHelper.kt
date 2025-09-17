package com.effectsar.labcv.ebox.helper

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.common.ebox.EBoxEffectType
import com.effectsar.labcv.common.ebox.EBoxEffectType.BACKGROUND_BLUR
import com.effectsar.labcv.common.ebox.EBoxEffectType.BEAUTY
import com.effectsar.labcv.common.ebox.EBoxEffectType.FILTER
import com.effectsar.labcv.common.ebox.EBoxEffectType.IMAGE_QUALITY
import com.effectsar.labcv.common.ebox.EBoxEffectType.MATTING
import com.effectsar.labcv.common.ebox.EBoxEffectType.MATTING_CHROMA
import com.effectsar.labcv.common.ebox.EBoxEffectType.STYLE_MAKEUP
import com.effectsar.labcv.common.ebox.EBoxEffectType.UNKNOWN
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.base.EBoxPageItem
import com.effectsar.labcv.ebox.blur.BgBlurPanel
import com.effectsar.labcv.ebox.image.ImageQualityPanel
import com.effectsar.labcv.ebox.makeup.StyleMakeUpPanel
import com.effectsar.labcv.ebox.matting.MattingPanel
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.ebox.loader.utils.GsonUtils
import com.volcengine.effectone.api.EOVisibilityListener
import com.volcengine.effectone.beauty.BeautyPanel
import com.volcengine.effectone.filter.FilterPanel
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.sticker.EOBaseStickerPanel

/**
 *Author: gaojin.ivy
 *Time: 2025/6/3 14:46
 */

class RecordRootViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordViewModel by lazy { EboxRecordUIViewModel.get(activity) }
    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    private lateinit var rootContainer: View

    override fun initView(rootView: ViewGroup) {
        rootContainer = rootView.findViewById(R.id.ebox_record_root_content)
        recordViewModel.rootViewVisible.observe(owner) {
            it?.let { show ->
                if (show) {
                    rootContainer.visibility = View.VISIBLE
                } else {
                    rootContainer.visibility = View.GONE
                }
            }
        }

        rootView.findViewById<View>(R.id.ebox_recorder_back).setOnClickListener {
            activity.finish()
        }

        rootView.findViewById<TextView>(R.id.ebox_recorder_title).apply {
            text = recordViewModel.getPageTitle()
        }


        recordUIViewModel.clickEboxPageItem.observe(owner) {
            it?.let { eBoxPageItem ->
                when (eBoxPageItem.type) {
                    BEAUTY -> {
                        showBeautyPanel(eBoxPageItem)
                    }

                    FILTER -> {
                        showFilterPanel(eBoxPageItem)
                    }

                    IMAGE_QUALITY -> {
                        showImageQualityPanel(eBoxPageItem)
                    }

                    EBoxEffectType.STICKER,
                    EBoxEffectType.AMAZING_STICKER,
                    EBoxEffectType.GIFT_STICKER,
                    EBoxEffectType.GAME_STICKER,
                    EBoxEffectType.AVATAR_DRIVE_STICKER,
                    EBoxEffectType.CINE_MOVE_STICKER -> {
                        showStickerPanel(eBoxPageItem)
                    }

                    STYLE_MAKEUP -> {
                        showStyleMakeUpPanel(eBoxPageItem)
                    }


                    MATTING, MATTING_CHROMA -> {
                        showMattingPanel(eBoxPageItem)
                    }

                    BACKGROUND_BLUR -> {
                        showBackgroundBlurPanel(eBoxPageItem)
                    }

                    UNKNOWN -> {
                        Toast.makeText(AppSingleton.instance, "eboxPageItem UNKNOWN", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showBeautyPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        BeautyPanel().apply {
            setHasSticker(false)
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, BeautyPanel.TAG)
    }

    private fun showStickerPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        EOBaseStickerPanel().apply {
            defaultSelectSticker(false)
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, EOBaseStickerPanel.TAG)
    }

    private fun showFilterPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        FilterPanel().apply {
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, FilterPanel.TAG)
    }

    private fun showImageQualityPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        ImageQualityPanel().apply {
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, ImageQualityPanel.TAG)
    }

    private fun showMattingPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        MattingPanel().apply {
            setTitleResId(eBoxPageItem.type.nameResId)
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, ImageQualityPanel.TAG)
    }

    private fun showBackgroundBlurPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        BgBlurPanel().apply {
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, ImageQualityPanel.TAG)
    }

    private fun showStyleMakeUpPanel(eBoxPageItem: EBoxPageItem) {
        val panel = eBoxPageItem.panel ?: return
        val requestKey = GsonUtils.gson.toJson(panel)
        StyleMakeUpPanel().apply {
            setCustomRequestKey(requestKey)
            setVisibilityListener(object : EOVisibilityListener {
                override fun onDialogShow() {
                    recordUIViewModel.showOrHideRootView(false)
                }

                override fun onDialogDismiss() {
                    recordUIViewModel.showOrHideRootView(true)
                }
            })
        }.show(activity.supportFragmentManager, ImageQualityPanel.TAG)
    }
}