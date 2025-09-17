package com.effectsar.labcv.ebox.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.common.ebox.EBoxEffectType
import com.effectsar.labcv.common.ebox.EBoxEffectType.MATTING_CHROMA
import com.effectsar.labcv.common.utils.ToastUtils
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.volcengine.effectone.singleton.AppSingleton

/**
 *Author: gaojin.ivy
 *Time: 2025/6/3 14:46
 */

class BottomLeftButtonHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }

    private lateinit var button: View
    private lateinit var leftImage: ImageView
    private lateinit var leftTextView: TextView

    override fun initView(rootView: ViewGroup) {

        button = rootView.findViewById(R.id.eo_recorder_left_container)
        leftImage = rootView.findViewById(R.id.eo_recorder_left_image)
        leftTextView = rootView.findViewById(R.id.eo_recorder_left_text)

        val eBoxPageItem = recordUIViewModel.getLeftBottomFun()

        leftImage.setImageResource(eBoxPageItem.type.icon)
        leftTextView.text = AppSingleton.instance.getString(eBoxPageItem.type.nameResId)

        if (eBoxPageItem.type == EBoxEffectType.UNKNOWN) {
            return
        }

        button.setOnClickListener {
            recordUIViewModel.clickEboxPageItem(eBoxPageItem)
        }

        rootView.postDelayed({
            recordUIViewModel.clickEboxPageItem(eBoxPageItem)
            if (eBoxPageItem.type == MATTING_CHROMA) {
                ToastUtils.show(AppSingleton.instance.getString(R.string.matting_sticker_tips))
            }
        }, 500)
    }
}