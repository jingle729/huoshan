package com.effectsar.labcv.ebox.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.effectsar.labcv.common.base.BaseGLActivity
import com.effectsar.labcv.common.config.ImageSourceConfig.ImageSourceType
import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl
import com.effectsar.labcv.common.model.ProcessInput
import com.effectsar.labcv.common.model.ProcessOutput
import com.effectsar.labcv.core.effect.EffectManager
import com.effectsar.labcv.core.effect.EffectResourceHelper
import com.effectsar.labcv.core.license.EffectLicenseHelper
import com.effectsar.labcv.core.util.LogUtils
import com.effectsar.labcv.core.util.timer_record.LogTimerRecord
import com.effectsar.labcv.ebox.PageDetail
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.base.EboxResourceFinder
import com.effectsar.labcv.ebox.setting.EboxSettingPanel
import com.effectsar.labcv.ebox.vm.EboxPerfViewModel
import com.effectsar.labcv.ebox.vm.EboxRecordEffectViewModel
import com.effectsar.labcv.ebox.vm.EboxRecordUIViewModel
import com.effectsar.labcv.ebox.setting.EboxSettingViewModel
import com.effectsar.labcv.ebox.vm.ResolutionSize
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.EffectsSDKResultCode
import com.volcengine.ebox.loader.EBoxSDKManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *Author: gaojin.ivy
 *Time: 2025/5/29 16:34
 */

class EBoxRecordActivity : BaseGLActivity() {

    private val eBoxRecordFragment by lazy {
        EBoxRecordFragment().apply {
            arguments = intent.extras
        }
    }

    private lateinit var effectManager: EffectManager
    private val recordUIViewModel by lazy { EboxRecordUIViewModel.get(this) }
    private val recordEffectViewModel by lazy { EboxRecordEffectViewModel.get(this) }
    private val settingViewModel by lazy { EboxSettingViewModel.get(this) }
    private val perfViewModel by lazy { EboxPerfViewModel.get(this) }

    private var closeEffect = false
    private val handler = Handler(Looper.getMainLooper())
    private var isPolling = false
    private val delayMillis = 1000L
    private val pollingRunnable = object : Runnable {
        override fun run() {
            if (isPolling) {
                doPollingTask()
                handler.postDelayed(this, delayMillis)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pageDetail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(PageDetail.PAGE_DETAIL_KEY, PageDetail::class.java)
        } else {
            intent?.getParcelableExtra(PageDetail.PAGE_DETAIL_KEY)
        }

        recordUIViewModel.setPageDetail(pageDetail)

        val type = mImageSourceConfig.type
        if (type == ImageSourceType.TYPE_IMAGE || type == ImageSourceType.TYPE_VIDEO) {
            recordUIViewModel.showOrHideAlbumView(false)
        } else {
            recordUIViewModel.showOrHideAlbumView(true)
        }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fl_container, eBoxRecordFragment, "EBoxRecordFragment")
            .commitNowAllowingStateLoss()

        initObserver()

        /** {zh}
         * EffectManager是RenderManger的封装类，
         * 与当前GL上下文强关联，因此GL上下文发生变化之前，需要调用各自的销毁函数，但对象本身不销毁，再次使用时会根据内部变量的状态，重新关联当前上下文初始化
         */
        /** {en}
         * EffectManager is the encapsulated class of RenderManger,
         * is strongly associated with the current GL context, so before the GL context changes, you need to call the respective destruction function, but the object itself is not destroyed, and when you use it again, it will be based on the state of the internal variable, Reassociate the current context initialization
         */
        effectManager = EffectManager(this, EffectResourceHelper(this), EffectLicenseHelper.getInstance(this))

        recordEffectViewModel.injectEffectManager(effectManager)
        recordEffectViewModel.injectSurfaceView(mSurfaceView)

        effectManager.addMessageListener { i, i1, i2, s ->
            if (i == EffectManager.BEF_MSG_TYPE_LICENSE_CHECK && i1 != 0) {
                // 鉴权失败，arg1 对应离线鉴权or在线鉴权，arg2对应鉴权错误码，args返回license路径或buffer长度
                Log.e("cvtest", "Message Type: BEF_MSG_TYPE_LICENSE_CHECK, arg1: $i1,arg2: $i2,arg3: $s")
            } else if (i == EffectManager.BEF_MSG_TYPE_MODEL_MISS) {
                // arg3为模型路径，素材加载过程中出现了模型找不到的问题
                Log.e("cvtest", "Message Type: BEF_MSG_TYPE_MODEL_MISS, arg3: $s")
            } else if (i == EffectManager.RENDER_MSG_TYPE_RESOURCE) {
                // 存在feature加载失败，arg3中保存对应feature的名称
                Log.e("cvtest", "Message Type: RENDER_MSG_TYPE_RESOURCE, arg1: $i1,arg2: $i2,arg3: $s")
            } else if (i == EffectManager.BEF_MSG_TYPE_EFFECT_INIT) {
                // EffectManager初始化失败，arg1对应埋点序号，arg3对应错误提示信息
                Log.e("cvtest", "Message Type: BEF_MSG_TYPE_EFFECT_INIT, arg1: $i1,arg3: $s")
            }
        }
    }

    override fun onSurfaceCreated(gl10: GL10?, eglConfig: EGLConfig?) {
        super.onSurfaceCreated(gl10, eglConfig)
        recordEffectViewModel.injectMaxTextureSize(mMaxTextureSize)
        val ret = effectManager.init(EboxResourceFinder(EBoxSDKManager.resourceConfig.modelDir))
        if (ret == EffectsSDKResultCode.BEF_RESULT_SUC) {
            recordEffectViewModel.initEffect()
        } else {
            LogUtils.e("mEffectManager.init() fail!! error code =$ret")
        }
    }

    override fun processImpl(input: ProcessInput): ProcessOutput {
        LogTimerRecord.RECORD("totalProcess")
        var dstTexture = mImageUtil.prepareTexture(input.width, input.height)
        if (closeEffect) {
            dstTexture = input.texture
            effectManager.cleanPipeline()
        } else {
            effectManager.setCameraPosition(mImageSourceProvider.isFront)
            val timestamp = System.nanoTime()
            val ret: Boolean = effectManager.process(input.texture, dstTexture, input.width, input.height, input.getSensorRotation(), timestamp)
            if (!ret) {
                dstTexture = input.texture
            }
        }
        LogTimerRecord.STOP("totalProcess")
        output.setTexture(dstTexture)
        output.setWidth(input.width)
        output.setHeight(input.height)
        return output
    }

    private fun initObserver() {
        recordUIViewModel.switchCamera.observe(this) {
            if (mImageSourceProvider is CameraSourceImpl) {
                val cameraId = 1 - (mImageSourceProvider as CameraSourceImpl).cameraID
                mImageSourceConfig.media = cameraId.toString()
                (mImageSourceProvider as CameraSourceImpl).changeCamera(cameraId, null)

            }
        }

        recordUIViewModel.takePic.observe(this) {
            mNeedCapture = true
        }

        recordUIViewModel.closeEffect.observe(this) {
            it?.let { value ->
                closeEffect = value
            }
        }

        recordUIViewModel.startAlbum.observe(this) {
            startChoosePic()
        }

        settingViewModel.resolution.observe(this) {
            it?.let { resolution ->
                when (resolution) {
                    EboxSettingPanel.TAB_720P_ID -> {
                        changeResolution(1280, 720)
                    }

                    EboxSettingPanel.TAB_1080P_ID -> {
                        changeResolution(1920, 1080)
                    }
                }
            }
        }
    }

    private fun changeResolution(width: Int, height: Int) {
        (mImageSourceProvider as? CameraSourceImpl)?.let {
            mImageSourceConfig.requestWidth = width
            mImageSourceConfig.requestHeight = height
            it.setPreferSize(width, height)
            it.changeCamera(it.cameraID, null)
        }
    }

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    var frameCount = 0L
    var size = ResolutionSize()
    override fun onDrawFrame(gl10: GL10?) {
        val start = System.currentTimeMillis()
        super.onDrawFrame(gl10)
        val cost = System.currentTimeMillis() - start
        frameCount++
        if (frameCount % 10 == 0L) {
            runOnUiThread {
                perfViewModel.drawFrameCostTime(cost)
                size.width = mTextureWidth
                size.height = mTextureHeight
                perfViewModel.resolution(size)
            }
        }
    }

    override fun onPause() {
        mSurfaceView.queueEvent {
            effectManager.destroy()
        }
        super.onPause()
        stopPolling()
    }

    private fun startPolling() {
        isPolling = true
        handler.post(pollingRunnable)
    }

    private fun stopPolling() {
        isPolling = false
        handler.removeCallbacks(pollingRunnable)
    }

    private fun doPollingTask() {
        val fps = mFrameRator?.frameRate ?: 0
        perfViewModel.fpsValue(fps)
    }
}