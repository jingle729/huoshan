package com.effectsar.labcv.ebox.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.volcengine.effectone.sticker.EOBaseStickerViewModel
import com.volcengine.effectone.widget.AutoDismissTextView

/**
 *Author: gaojin
 *Time: 2023/11/22 18:50
 */

class StickerTipsViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val stickerViewModel by lazy { EOBaseStickerViewModel.get(activity) }

    private lateinit var stickerTipView: AutoDismissTextView

    override fun initView(rootView: ViewGroup) {
        stickerTipView = rootView.findViewById(R.id.ebox_recorder_sticker_tips)

        stickerViewModel.selectedItem.observe(owner) {
            it?.let { item ->
                if (item.tips().isNotEmpty()) {
                    stickerTipView.visibility = View.VISIBLE
                    stickerTipView.showTextWithBlink(item.tips(), 1000L, 4)
                } else {
                    stickerTipView.visibility = View.GONE
                }
            }
        }
    }
}