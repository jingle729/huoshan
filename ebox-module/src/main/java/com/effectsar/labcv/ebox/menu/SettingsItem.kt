package com.effectsar.labcv.ebox.menu

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.image.ImageQualityPanel
import com.effectsar.labcv.ebox.matting.MattingPanel
import com.effectsar.labcv.ebox.setting.EboxSettingPanel
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.effectone.api.EOVisibilityListener
import com.volcengine.effectone.menu.AbsIMenuItem

/**
 *Author: gaojin
 *Time: 2023/11/13 10:42
 */

class SettingsItem(override val activity: FragmentActivity, override val owner: LifecycleOwner) : AbsIMenuItem() {

    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    override val name: String = activity.getString(R.string.settings)
    override fun provideIcon(): Int {
        return R.drawable.icon_ebox_settings
    }

    override fun click() {
        EboxSettingPanel().apply {
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