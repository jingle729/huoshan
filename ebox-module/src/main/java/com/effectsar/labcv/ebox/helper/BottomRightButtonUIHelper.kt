package com.effectsar.labcv.ebox.helper

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.api.IUIHelper
import com.effectsar.labcv.ebox.utils.getAbsolutePath
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.effectsar.labcv.ebox.vm.FirstImageViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.image.ImageListener
import com.volcengine.effectone.image.ImageOption.Builder
import com.volcengine.effectone.singleton.AppSingleton

/**
 *Author: gaojin.ivy
 *Time: 2025/5/30 11:35
 */

class BottomRightButtonUIHelper(override val activity: FragmentActivity, override val owner: LifecycleOwner) : IUIHelper {

    private lateinit var albumImage: ShapeableImageView
    private lateinit var albumViewContainer: View
    private val firstImageViewModel by lazy { FirstImageViewModel.get(activity) }
    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(activity) }


    override fun initView(rootView: ViewGroup) {
        albumImage = rootView.findViewById(R.id.eo_recorder_album_image)
        albumViewContainer = rootView.findViewById(R.id.eo_recorder_right_container)

        albumImage.setOnClickListener {
            recordUIViewModel.startAlbum()
        }

        firstImageViewModel.firstMaterialItem.observe(owner) {
            it?.let { item ->
                EffectOneSdk.imageLoader.loadImageView(
                    albumImage,
                    (item as IMediaItem).getAbsolutePath(),
                    Builder()
                        .width(albumImage.measuredWidth)
                        .width(albumImage.measuredHeight)
                        .listener(object : ImageListener {
                            override fun onLoadFailed(e: Exception?): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Any?): Boolean {
                                albumImage.strokeColor = ContextCompat.getColorStateList(AppSingleton.instance, R.color.colorWhite)
                                return false
                            }
                        })
                        .transcodeType(Bitmap::class.java)
                        .build()
                )
            }
        }

        recordUIViewModel.albumViewVisible.observe(owner) {
            it?.let { show ->
                if (show) {
                    albumViewContainer.visibility = View.VISIBLE
                } else {
                    albumViewContainer.visibility = View.GONE
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        //设置相册Icon默认图片
        firstImageViewModel.queryFirstMedia(owner)
    }
}
