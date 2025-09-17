package com.effectsar.labcv.ebox.blur

import com.volcengine.effectone.api.AsyncResourceConfig
import com.volcengine.effectone.api.ResourceLoader
import com.volcengine.effectone.data.IUIConfig

class BgBlurUIConfig : AsyncResourceConfig(), IUIConfig {

    override val name: String = "background_blur"
    override val id: String = "background_blur"

    override var resourceLoader: ResourceLoader? = null
}