package com.effectsar.labcv.ebox.helper

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.vm.EboxPerfViewModel
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel

/**
 *Author: gaojin.ivy
 *Time: 2025/6/18 11:19
 */

class RecordPerfViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }
    private val perfViewModel by lazy { EboxPerfViewModel.get(activity) }

    private var perfRootView: View? = null
    private var fpsValueView: TextView? = null
    private var renderValueView: TextView? = null
    private var resolutionValueView: TextView? = null

    @SuppressLint("SetTextI18n")
    override fun initView(rootView: ViewGroup) {

        perfRootView = rootView.findViewById(R.id.ebox_record_root_performance)
        fpsValueView = rootView.findViewById(R.id.ebox_perf_fps_value)
        renderValueView = rootView.findViewById(R.id.ebox_perf_rending_value)
        resolutionValueView = rootView.findViewById(R.id.ebox_perf_resolution_value)

        recordUIViewModel.showPerf.observe(owner) {
            it?.let { showPerf ->
                if (showPerf) {
                    perfRootView?.visibility = View.VISIBLE
                } else {
                    perfRootView?.visibility = View.GONE
                }
            }
        }

        perfViewModel.fpsValue.observe(owner) {
            fpsValueView?.text = "$it"
        }

        perfViewModel.drawFrameCostTime.observe(owner) {
            renderValueView?.text = "$it ms"
        }

        perfViewModel.resolution.observe(owner) {
            it?.let { size ->
                if (size.width != 0 && size.height != 0) {
                    resolutionValueView?.text = "${size.height}*${size.width}"
                }
            }
        }
    }
}