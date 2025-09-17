package com.effectsar.labcv.ebox.helper

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.menu.CompareItem
import com.effectsar.labcv.ebox.menu.EboxPanelItem
import com.effectsar.labcv.ebox.menu.SettingsItem
import com.effectsar.labcv.ebox.menu.SwitchCameraItem
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.effectone.menu.IMenuItem
import com.volcengine.effectone.widget.MenuToolsView

/**
 *Author: gaojin.ivy
 *Time: 2025/5/30 11:35
 */

class MenuListUIHelper(override val activity: FragmentActivity, override val owner: LifecycleOwner) : IUIHelper {

    private val recordViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    override fun initView(rootView: ViewGroup) {
        rootView.findViewById<MenuToolsView>(R.id.ebox_recorder_sidebar).apply {
            setMaxShowCount(10)
            addItems(getMenuList())
            showBarTextView()
        }
    }

    private fun getMenuList(): List<IMenuItem> {
        val menuList = mutableListOf<IMenuItem>()
        menuList.add(SettingsItem(activity, owner))
        menuList.add(SwitchCameraItem(activity, owner))
        recordViewModel.getMenuList().forEach {
            menuList.add(EboxPanelItem(activity, owner, it))
        }
        menuList.add(CompareItem(activity, owner))
        return menuList
    }
}
