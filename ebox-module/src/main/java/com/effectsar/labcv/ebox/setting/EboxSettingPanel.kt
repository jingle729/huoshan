package com.effectsar.labcv.ebox.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.effectsar.labcv.ebox.setting.EboxSettingViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.volcengine.effectone.ui.BaseBottomSheetDialogFragment

/**
 *Author: gaojin.ivy
 *Time: 2025/6/17 19:46
 */

class EboxSettingPanel : BaseBottomSheetDialogFragment() {

    companion object {
        const val TAG = "EboxSettingPanel"
        const val TAB_1080P_ID = 10001
        const val TAB_720P_ID = 10002
    }

    private var resolutionTab: TabLayout? = null
    private var perfSwitch: SwitchCompat? = null

    private val settingViewModel by lazy { EboxSettingViewModel.get(requireActivity()) }
    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(requireActivity()) }

    override fun getFragmentTag() = TAG

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ebox_layout_setting_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resolutionTab = view.findViewById<TabLayout>(R.id.ebox_setting_resolution_tab)
        perfSwitch = view.findViewById<SwitchCompat>(R.id.perf_switch)

        resolutionTab?.run {
            val tab1080P = newTab().setText("1920*1080").setId(TAB_1080P_ID)
            val tab720P = newTab().setText("1280*720").setId(TAB_720P_ID)
            addTab(tab1080P)
            addTab(tab720P)

            val resolution = settingViewModel.resolution.value
            when (resolution) {
                TAB_720P_ID -> {
                    selectTab(tab720P)
                }

                TAB_1080P_ID -> {
                    selectTab(tab1080P)
                }

                else -> {
                    selectTab(tab720P)
                }
            }

            addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    settingViewModel.changeResolution(tab.id)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })
        }

        perfSwitch?.run {
            isChecked = recordUIViewModel.showPerf.value == true
            setOnCheckedChangeListener { _, isChecked ->
                recordUIViewModel.showPerfView(isChecked)
            }
        }
    }
}