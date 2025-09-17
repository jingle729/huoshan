package com.effectsar.labcv.ebox.makeup

import com.volcengine.effectone.api.AsyncResourceConfig
import com.volcengine.effectone.api.ResourceLoader
import com.volcengine.effectone.data.IUIConfig

class StyleMakeUpUIConfig : AsyncResourceConfig(), IUIConfig {

    override val name: String = "style_makeup"
    override val id: String = "style_makeup"

    override var resourceLoader: ResourceLoader? = null
}