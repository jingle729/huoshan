package com.effectsar.labcv.ebox.matting

import com.volcengine.effectone.api.AsyncResourceConfig
import com.volcengine.effectone.api.ResourceLoader
import com.volcengine.effectone.data.IUIConfig
import com.volcengine.effectone.resource.api.EOResourcePanelKey

class MattingUIConfig : AsyncResourceConfig(), IUIConfig {

    override val name: String = "matting"
    override val id: String = "matting"

    override var resourceLoader: ResourceLoader? = null
}