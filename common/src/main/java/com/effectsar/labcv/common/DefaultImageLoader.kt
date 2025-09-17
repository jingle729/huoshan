package com.effectsar.labcv.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.image.ImageLoader
import com.volcengine.effectone.image.ImageOption
import com.volcengine.effectone.image.ImageSource
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils

class DefaultImageLoader : ImageLoader {
    companion object {
        private const val TAG = "ImageLoader"
    }

    private fun buildRequest(option: ImageOption?, source: ImageSource<*>, context: Context): RequestBuilder<out Any>? {
        val sourceValue = source.getValue() ?: return null

        var requestBuilder = Glide.with(context)
            .`as`(option?.transcodeType ?: Drawable::class.java)
            .load(sourceValue)

        option?.let {
            requestBuilder = requestBuilder.apply(it.toGlideRequestOption())
        }
        bindListener(requestBuilder, option)
        return requestBuilder
    }

    override fun <T> loadImageView(
        imageView: ImageView,
        imageSource: ImageSource<T>,
        option: ImageOption?
    ) {
        buildRequest(option, imageSource, imageView.context)?.into(imageView)
    }


    override fun <T> loadBitmapSync(
        context: Context,
        imageSource: ImageSource<T>,
        option: ImageOption
    ): Bitmap? {
        option.transcodeType = Bitmap::class.java
        return buildRequest(option, imageSource, AppSingleton.instance)
            ?.submit(option.width, option.height)
            ?.get() as? Bitmap
    }


    @SuppressLint("DiscouragedApi")
    private fun setLocalImage(imageView: ImageView, builtInIcon: String, option: ImageOption?) {
        val assetsIcon = "${EOUtils.pathUtil.androidAssetPrefix}Resource_icons/$builtInIcon"
        LogKit.d(TAG, "assets icon is $assetsIcon")
        loadImageView(imageView, assetsIcon, option)
    }

    private fun getIconName(builtInIcon: String): String {
        val lastDotIndex = builtInIcon.lastIndexOf(".")
        return if (lastDotIndex != -1) {
            builtInIcon.substring(0, lastDotIndex)
        } else {
            ""
        }
    }


    private fun <TranscodeType> bindListener(requestBuilder: RequestBuilder<TranscodeType>, option: ImageOption?) {
        option?.listener?.let {
            requestBuilder.listener(object : RequestListener<TranscodeType> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<TranscodeType>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return it.onLoadFailed(e)
                }

                override fun onResourceReady(
                    resource: TranscodeType,
                    model: Any?,
                    target: Target<TranscodeType>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return it.onResourceReady(resource)
                }
            })
        }
    }
}

fun ImageOption.toGlideRequestOption(): RequestOptions {
    var requestOptions = RequestOptions()
    if (skipDiskCached) {
        requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
    }
    requestOptions = requestOptions.skipMemoryCache(false)
    if (this.width != 0 && this.height != 0) {
        requestOptions = requestOptions.override(this.width, this.height)
    }
    if (this.placeHolder != 0) {
        requestOptions = requestOptions.placeholder(this.placeHolder)
    }
    when (this.scaleType) {
        ImageView.ScaleType.CENTER_CROP -> {
            requestOptions = requestOptions.centerCrop()
        }

        ImageView.ScaleType.CENTER_INSIDE -> {
            requestOptions = requestOptions.centerInside()
        }

        ImageView.ScaleType.FIT_CENTER -> {
            requestOptions = requestOptions.fitCenter()
        }

        else -> {}
    }
    when (this.format) {
        Bitmap.Config.ARGB_8888 -> {
            requestOptions = requestOptions.format(DecodeFormat.PREFER_ARGB_8888)
        }

        Bitmap.Config.RGB_565 -> {
            requestOptions = requestOptions.format(DecodeFormat.PREFER_RGB_565)
        }

        else -> {
            //do nothing
        }
    }
    return requestOptions
}