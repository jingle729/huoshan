package com.effectsar.labcv.ebox.vm

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.GLSurfaceView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.effectsar.labcv.common.imgsrc.video.SimplePlayer
import com.effectsar.labcv.common.imgsrc.video.SimplePlayer.IPlayStateListener
import com.effectsar.labcv.common.imgsrc.video.VideoSourceImpl
import com.effectsar.labcv.common.model.CaptureResult
import com.effectsar.labcv.common.utils.BitmapUtils
import com.effectsar.labcv.core.effect.EffectManager
import com.effectsar.labcv.core.util.ImageUtil
import com.effectsar.labcv.core.util.LogUtils
import com.effectsar.labcv.ebox.matting.MattingItem
import com.effectsar.labcv.ebox.utils.getAbsolutePath
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants
import com.volcengine.ck.album.utils.AlbumUtils
import com.volcengine.effectone.base.ComposerNode
import com.volcengine.effectone.filter.EOFilterItem
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory
import java.nio.ByteBuffer

/**
 *Author: gaojin.ivy
 *Time: 2025/6/5 20:08
 */

class EboxRecordEffectViewModel(activity: FragmentActivity) : BaseViewModel(activity) {
    companion object {
        fun get(activity: FragmentActivity): EboxRecordEffectViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(EboxRecordEffectViewModel::class.java)
        }

        private const val BG_KEY = "BCCustomBackground"
    }

    private val beautyNodeList = mutableListOf<ComposerNode>()
    private var imageQualityNodeList = mutableListOf<ComposerNode>()
    private var backgroundBlurNodeList = mutableListOf<ComposerNode>()
    private var styleMakeupNodeList = mutableListOf<ComposerNode>()

    private var currentSticker: String = ""
    private var currentFilter: EOFilterItem? = null
    private var currentMattingItem: MattingItem? = null

    private var effectManager: EffectManager? = null

    @SuppressLint("StaticFieldLeak")
    private var surfaceView: GLSurfaceView? = null
    private var maxTextureSize: Int = 3000
    private var imageUtil: ImageUtil? = null

    fun injectEffectManager(manager: EffectManager) {
        effectManager = manager
    }

    fun injectSurfaceView(surfaceView: GLSurfaceView) {
        this.surfaceView = surfaceView
    }

    fun injectMaxTextureSize(size: Int) {
        maxTextureSize = size
    }

    fun initEffect() {
        runEffectSafety {
            setTotalBeauty()
            if (currentSticker.isNotEmpty()) {
                effectManager?.setStickerAbs(currentSticker)
            }
            currentFilter?.let {
                if (it.isClear()) {
                    effectManager?.run {
                        setFilterAbs("")
                        updateFilterIntensity(0F)
                    }
                } else {
                    effectManager?.run {
                        setFilterAbs(it.path())
                        updateFilterIntensity(it.intensity)
                    }
                }
            }
            currentMattingItem?.let {
                if (it.isEmpty()) {
                    effectManager?.setStickerAbs("")
                } else {
                    if (it.customBg != null) {
                        setCustomMattingBg(it.path(), it.customBg)
                    } else if (it.defaultPath.isNotEmpty()) {
                        setDefaultMattingBg(it.path(), it.defaultPath)
                    }
                }
            }
        }
    }

    /**
     * 设置美颜美型数据，每次都是全量Set
     */
    fun setBeauty(nodeList: List<ComposerNode>) {
        runEffectSafety {
            val hasEyelash = nodeList.any { it.key == "Internal_Makeup_Eyelash" }
            if (hasEyelash) {
                effectManager?.setSyncLoadResource(true)
            }
            beautyNodeList.run {
                forEach { oldNode ->
                    val filterNode = nodeList.firstOrNull { it == oldNode }
                    if (filterNode == null) {
                        effectManager?.updateComposerNodeIntensity(oldNode.node, oldNode.key, 0F)
                    }
                }
                clear()
                addAll(nodeList)
            }
            setTotalBeauty()
            if (hasEyelash) {
                //美颜美型-美妆-睫毛 这个道具比较特殊，set完毕以后需要立即更新一下强度
                //但是立即更新又不好使，需要先设置为同步加载，再更新强度
                nodeList.forEach {
                    if (it.key == "Internal_Makeup_Eyelash") {
                        updateBeauty(it)
                    }
                }
                effectManager?.setSyncLoadResource(false)
            }
        }
    }

    /**
     * 更新美颜美型强度
     */
    fun updateBeauty(node: ComposerNode) {
        runEffectSafety {
            effectManager?.updateComposerNodeIntensity(node.node, node.key, node.value)
        }
    }

    /**
     * 设置贴纸
     */
    fun setSticker(stickerPath: String) {
        currentSticker = stickerPath
        runEffectSafety { effectManager?.setStickerAbs(stickerPath) }
    }

    /**
     * 设置滤镜
     */
    fun setFilter(filterItem: EOFilterItem) {
        runEffectSafety {
            if (filterItem.isClear()) {
                currentFilter = null
                effectManager?.run {
                    setFilterAbs("")
                    updateFilterIntensity(0F)
                }
            } else {
                currentFilter = filterItem
                effectManager?.run {
                    setFilterAbs(filterItem.path())
                    updateFilterIntensity(filterItem.intensity)
                }
            }
        }
    }

    /**
     * 更新滤镜强度
     */
    fun updateFilterIntensity(value: Float) {
        runEffectSafety { effectManager?.updateFilterIntensity(value) }
    }

    fun setImageQuality(nodeList: List<ComposerNode>) {
        runEffectSafety {
            imageQualityNodeList.run {
                forEach { oldNode ->
                    val filterNode = nodeList.firstOrNull { it == oldNode }
                    if (filterNode == null) {
                        effectManager?.updateComposerNodeIntensity(oldNode.node, oldNode.key, 0F)
                    }
                }
                clear()
                addAll(nodeList)
            }
            setTotalBeauty()
        }
    }

    fun setStyleMakeup(nodeList: List<ComposerNode>) {
        runEffectSafety {
            styleMakeupNodeList.run {
                forEach { oldNode ->
                    val filterNode = nodeList.firstOrNull { it == oldNode }
                    if (filterNode == null) {
                        effectManager?.updateComposerNodeIntensity(oldNode.node, oldNode.key, 0F)
                    }
                }
                clear()
                addAll(nodeList)
            }
            setTotalBeauty()
        }
    }

    fun setBackgroundBlur(nodeList: List<ComposerNode>) {
        runEffectSafety {
            backgroundBlurNodeList.run {
                forEach { oldNode ->
                    val filterNode = nodeList.firstOrNull { it == oldNode }
                    if (filterNode == null) {
                        effectManager?.updateComposerNodeIntensity(oldNode.node, oldNode.key, 0F)
                    }
                }
                clear()
                addAll(nodeList)
            }
            setTotalBeauty()
        }
    }

    /**
     * 设置虚拟背景
     * 选图的场景涉及到EffectManager的销毁和重建，走initEffect方法
     *
     */
    fun setMattingBg(item: MattingItem) {
        currentMattingItem = item
        runEffectSafety {
            if (item.isEmpty()) {
                videoSource?.close()
                effectManager?.setStickerAbs("")
                effectManager?.removeRenderCache(BG_KEY)
            } else {
                if (item.defaultPath.isNotEmpty()) {
                    setDefaultMattingBg(item.path(), item.defaultPath)
                }
            }
        }
    }

    fun onTouchEvent(
        eventCode: EffectsSDKEffectConstants.TouchEventCode?,
        x: Float,
        y: Float,
        force: Float,
        majorRadius: Float,
        pointerId: Int,
        pointerCount: Int
    ) {
        runEffectSafety {
            effectManager?.processTouch(eventCode, x, y, force, majorRadius, pointerId, pointerCount)
        }
    }

    fun onGestureEvent(
        eventCode: EffectsSDKEffectConstants.GestureEventCode?,
        x: Float,
        y: Float,
        dx: Float,
        dy: Float,
        factor: Float
    ) {
        runEffectSafety {
            effectManager?.processGesture(eventCode, x, y, dx, dy, factor)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        imageUtil = ImageUtil()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        runEffectSafety {
            videoSource?.close()
            videoSource = null
            imageUtil?.release()
            imageUtil = null
        }
    }

    private var videoSource: VideoSourceImpl? = null

    private fun setCustomMattingBg(effectPath: String, customBg: IMediaItem) {
        effectManager?.setStickerAbs(effectPath)
        if (effectPath.isEmpty()) {
            return
        }
        if (AlbumUtils.isImage(customBg)) {
            val captureResult = decodeByteBuffer(customBg.getAbsolutePath())
            if (captureResult == null) {
                LogUtils.e("decodeByteBuffer return null!!")
                return
            }
            val result: Boolean = effectManager?.setRenderCacheTexture(
                BG_KEY,
                captureResult.byteBuffer,
                captureResult.width,
                captureResult.height,
                4 * captureResult.width,
                EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0
            ) == true
            if (!result) {
                LogUtils.e("setRenderCacheTexture fail!!")
            }
        }
        if (AlbumUtils.isVideo(customBg)) {
            if (videoSource != null) {
                videoSource!!.close()
                videoSource = null
            }
            videoSource = VideoSourceImpl(null, object : IPlayStateListener {
                override fun videoAspect(width: Int, height: Int, videoRotation: Int) {
                }

                override fun frameRate(rate: Int) {
                }

                override fun onVideoEnd() {
                }
            }, object : SimplePlayer.IAudioDataListener {
                override fun onAudioFormatExtracted(format: MediaFormat?) {
                }

                override fun onAudioData(buffer: ByteBuffer?, bufferInfo: MediaCodec.BufferInfo?) {
                }

                override fun onNoAudioAvaliable() {
                }
            })
            videoSource!!.setSimplerAudioOn(false)

            videoSource!!.open(customBg.getAbsolutePath(), SurfaceTexture.OnFrameAvailableListener { surfaceTexture: SurfaceTexture? ->
                runEffectSafety {
                    val source = videoSource ?: return@runEffectSafety
                    if (!source.isReady) return@runEffectSafety
                    source.update()
                    val texture = source.getTexture()
                    val rotation = source.orientation
                    val width = if (rotation % 180 == 0) source.width else source.height
                    val height = if (rotation % 180 == 0) source.height else source.width

                    imageUtil?.let {
                        val curTexture: Int = it.transferTextureToTexture(
                            texture,
                            EffectsSDKEffectConstants.TextureFormat.Texture_Oes, EffectsSDKEffectConstants.TextureFormat.Texure2D,
                            width, height, ImageUtil.Transition().rotate(rotation.toFloat())
                        )
                        effectManager?.setRenderCacheTexture(BG_KEY, curTexture, width, height)
                    }
                }
            })
        }
    }

    private fun setDefaultMattingBg(effectPath: String, mattingPath: String) {
        effectManager?.setStickerAbs(effectPath)
        if (effectPath.isEmpty()) {
            return
        }
        effectManager?.setRenderCacheTexture(BG_KEY, mattingPath)
    }

    private fun setTotalBeauty() {
        val totalList = mutableListOf<ComposerNode>().apply {
            addAll(beautyNodeList)
            addAll(imageQualityNodeList)
            addAll(backgroundBlurNodeList)
            addAll(styleMakeupNodeList)
        }
        val nodes = totalList.map { it.getPathNodeValue() }
        effectManager?.setComposeNodes(nodes.toTypedArray())
    }

    private fun runEffectSafety(action: () -> Unit) {
        surfaceView?.let {
            it.queueEvent { action.invoke() }
        } ?: run {
            action.invoke()
        }
    }

    private fun decodeByteBuffer(path: String?): CaptureResult? {
        val bitmap = BitmapUtils.decodeBitmapFromFile(path, maxTextureSize, maxTextureSize)
        if (bitmap == null) return null
        return CaptureResult(BitmapUtils.bitmap2ByteBuffer(bitmap), bitmap.getWidth(), bitmap.getHeight())
    }
}