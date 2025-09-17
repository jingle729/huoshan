package com.effectsar.labcv.ebox.menu

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.base.EBoxPageItem
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.effectone.menu.AbsIMenuItem

/**
 *Author: gaojin
 *Time: 2023/11/13 10:42
 */

class EboxPanelItem(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner,
    private val eBoxPageItem: EBoxPageItem,
) : AbsIMenuItem() {

    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    override val name: String = activity.getString(eBoxPageItem.type.nameResId)
    override fun provideIcon(): Int {
        return eBoxPageItem.type.icon
    }

    override fun click() {
        recordUIViewModel.clickEboxPageItem(eBoxPageItem)
    }
}