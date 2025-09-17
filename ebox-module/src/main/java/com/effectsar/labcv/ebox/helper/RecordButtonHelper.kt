package com.effectsar.labcv.ebox.helper

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.effectone.api.EORecordTakePicListener
import com.volcengine.effectone.api.RecordMode.PICTURE
import com.volcengine.effectone.widget.EORecordButton

/**
 *Author: gaojin.ivy
 *Time: 2025/6/3 14:46
 */

class RecordButtonHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    private lateinit var recordButton: EORecordButton

    override fun initView(rootView: ViewGroup) {
        recordButton = rootView.findViewById(R.id.eo_recorder_start_record)
        recordButton.changeRecordMode(PICTURE)
        recordButton.setOnTakePicListener(object : EORecordTakePicListener {
            override fun takePic() {
                recordUIViewModel.takePic()
            }
        })
    }
}