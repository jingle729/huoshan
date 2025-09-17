package com.effectsar.labcv.ebox.helper

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.effectsar.labcv.common.gesture.GestureManager
import com.effectsar.labcv.common.gesture.GestureManager.OnTouchListener
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.vm.EboxRecordEffectViewModel
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants
import com.volcengine.effectone.singleton.AppSingleton

/**
 *Author: gaojin
 *Time: 2023/11/22 18:50
 */

class GestureViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordEffectViewModel by lazy { EboxRecordEffectViewModel.get(activity) }

    private val touchListener by lazy {
        object : OnTouchListener {
            override fun onTouchEvent(
                eventCode: EffectsSDKEffectConstants.TouchEventCode?,
                x: Float,
                y: Float,
                force: Float,
                majorRadius: Float,
                pointerId: Int,
                pointerCount: Int
            ) {
                recordEffectViewModel.onTouchEvent(eventCode, x, y, force, majorRadius, pointerId, pointerCount)
            }

            override fun onGestureEvent(
                eventCode: EffectsSDKEffectConstants.GestureEventCode?,
                x: Float,
                y: Float,
                dx: Float,
                dy: Float,
                factor: Float
            ) {
                recordEffectViewModel.onGestureEvent(eventCode, x, y, dx, dy, factor)
            }
        }
    }

    private val gestureManager by lazy {
        GestureManager(AppSingleton.instance, touchListener)
    }

    override fun initView(rootView: ViewGroup) {
        //rootView 和 「R.id.glview」对应的View区域相同
        rootView.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                gestureManager.onTouchEvent(event)
                return true
            }
        })
    }
}