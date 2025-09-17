package com.effectsar.labcv.ebox

import android.os.Build
import com.effectsar.labcv.common.base.EffectEBoxConfig
import com.effectsar.labcv.core.Config
import com.effectsar.labcv.ebox.blur.BgBlurUIConfig
import com.effectsar.labcv.ebox.image.ImageQualityUIConfig
import com.effectsar.labcv.ebox.makeup.StyleMakeUpUIConfig
import com.effectsar.labcv.ebox.matting.MattingUIConfig
import com.effectsar.labcv.ebox.tasks.EBoxResourceTaskProvider
import com.effectsar.labcv.ebox.utils.PreferenceHelper
import com.volcengine.ck.logkit.DebugLogger
import com.volcengine.ebox.loader.EBoxConfig
import com.volcengine.ebox.loader.EBoxRemoteRequestBuilder
import com.volcengine.ebox.loader.EBoxResourceConfig
import com.volcengine.ebox.loader.EBoxSDKManager
import com.volcengine.ebox.loader.LevelConfig
import com.volcengine.ebox.loader.ServerEnvironment
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.api.EffectOneConfigList
import com.volcengine.effectone.base.PhotoEditingMode
import com.volcengine.effectone.beauty.BeautyUIConfig
import com.volcengine.effectone.filter.FilterUIConfig
import com.volcengine.effectone.resource.api.EOResourceConfig
import com.volcengine.effectone.resource.api.EOResourceManager
import com.volcengine.effectone.resource.impl.DefaultLocalResourceLoader
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.sticker.data.EOBaseStickerUIConfig
import com.volcengine.effectone.utils.EOUtils
import java.util.Locale

/**
 *Author: gaojin.ivy
 *Time: 2025/5/28 15:26
 */

object EBoxSDKHelper {

    fun init() {
        initUIConfig()
        resourceInit()
        eboxInit()
        EffectOneSdk.run {
            isDebugMode = false
            logger = DebugLogger()
            modelPath = EOResourceManager.getModelRootPath()
            photoEditingMode = PhotoEditingMode.MODE_PICTURE
        }
    }

    private fun initUIConfig() {
        EffectOneConfigList.configure(BeautyUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
            it.defaultBeautyAction = {
                emptyList()
            }
        }
        EffectOneConfigList.configure(EOBaseStickerUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        EffectOneConfigList.configure(FilterUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        EffectOneConfigList.configure(ImageQualityUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        EffectOneConfigList.configure(MattingUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        EffectOneConfigList.configure(BgBlurUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        EffectOneConfigList.configure(StyleMakeUpUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
    }

    private fun resourceInit() {
        // 指定素材文件保存目录、素材使用语言
        val config = EOResourceConfig.Builder()
            .setResourceSavePath(EOUtils.pathUtil.internalResource())
            .setSysLanguage(Locale.getDefault().language)
            .build()
        // 素材sdk初始化
        EOResourceManager.init(config)
        EOResourceManager.taskProvider = EBoxResourceTaskProvider()
    }

    private fun eboxInit() {
        val eboxConfig = createEboxConfig()


        val eboxResourceConfig = EBoxResourceConfig.Builder()
            .setEnableOnline(BuildConfig.EBOX_ENABLE_ONLINE)
            .setNetWorker(DefaultNetWorker())
            .setAppConfig(eboxConfig)
            .setModelDir(EffectEBoxConfig.eboxModelDir().absolutePath)
            .setRequestBuilder(EBoxRemoteRequestBuilder(eboxConfig))
            .build()

        EBoxSDKManager.init(eboxResourceConfig, AppSingleton.instance)
    }

    private fun createEboxConfig(): EBoxConfig {
        //云平台配置
        val eboxConfig = EBoxConfig.generateEBoxConfigWithJson("EffectResource/ebox/config.json")
        val appId = getPreferenceValue("eo_ebox_app_id", eboxConfig.appId)
        val appSecret = getPreferenceValue("eo_ebox_app_secret", eboxConfig.appSecret)
        val appVersion = getPreferenceValue("eo_ebox_app_version", eboxConfig.appVersion)

        val newEBoxConfig = eboxConfig.copy(
            appId = appId,
            appSecret = appSecret,
            appVersion = appVersion,
        )
        //分级下发配置
        val levelConfig = LevelConfig(
            "null", Build.MANUFACTURER,
            Locale.getDefault().language, Locale.getDefault().country, ""
        )
        newEBoxConfig.levelConfig = levelConfig

        val serverEnvironment = PreferenceHelper.get("eo_ebox_server_environment")
        when (serverEnvironment) {
            "online" -> newEBoxConfig.serverEnvironment = ServerEnvironment.ONLINE
            "ppe" -> newEBoxConfig.serverEnvironment = ServerEnvironment.PPE
            "boe" -> newEBoxConfig.serverEnvironment = ServerEnvironment.BOE
        }
        val channelEnvironment = PreferenceHelper.get("eo_ebox_environment_channel")
        if (channelEnvironment.isNotEmpty()) {
            newEBoxConfig.environmentChannel = channelEnvironment
        }
        return newEBoxConfig
    }

    private fun getPreferenceValue(key: String, initValue: String): String {
        return PreferenceHelper.get(key).ifEmpty {
            initValue
        }
    }
}