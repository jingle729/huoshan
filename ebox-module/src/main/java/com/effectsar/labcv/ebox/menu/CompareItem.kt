package com.effectsar.labcv.ebox.menu

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.effectone.menu.AbsIMenuItem

/**
 *Author: gaojin
 *Time: 2023/11/13 10:42
 */

class CompareItem(override val activity: FragmentActivity, override val owner: LifecycleOwner) : AbsIMenuItem() {

    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    override val name: String = activity.getString(R.string.setting_compare)
    override fun provideIcon(): Int {
        return R.drawable.icon_ebox_compare
    }

    override fun click() {

    }

    override fun onTouchDown() {
        super.onTouchDown()
        recordUIViewModel.closeEffect()
    }

    override fun onTouchUp() {
        super.onTouchUp()
        recordUIViewModel.openEffect()
    }
}