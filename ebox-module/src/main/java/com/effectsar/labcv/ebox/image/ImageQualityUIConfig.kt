package com.effectsar.labcv.ebox.image

import com.volcengine.effectone.api.AsyncResourceConfig
import com.volcengine.effectone.api.ResourceLoader
import com.volcengine.effectone.data.IUIConfig
import com.volcengine.effectone.resource.api.EOResourcePanelKey

class ImageQualityUIConfig : AsyncResourceConfig(), IUIConfig {

    override val name: String = "image_quality"
    override val id: String = "image_quality"

    override var resourceLoader: ResourceLoader? = null
}