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

class SwitchCameraItem(override val activity: FragmentActivity, override val owner: LifecycleOwner) : AbsIMenuItem() {

    private val recordViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    override val name: String = activity.getString(R.string.switch_camera)
    override fun provideIcon(): Int {
        return R.drawable.icon_ebox_switch_camera
    }

    override fun click() {
        recordViewModel.switchCamera()
        getIconView()?.let {
            val value = it.rotation - 180
            it.animate()?.rotation(value)?.start()
        }
    }
}