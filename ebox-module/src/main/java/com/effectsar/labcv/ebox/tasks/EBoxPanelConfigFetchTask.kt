package com.effectsar.labcv.ebox.tasks

import android.net.Uri
import com.effectsar.labcv.ebox.Panel
import com.effectsar.labcv.ebox.SubItem
import com.volcengine.ebox.loader.EBoxErrCode
import com.volcengine.ebox.loader.EBoxSDKManager
import com.volcengine.ebox.loader.models.BizConfigItem
import com.volcengine.ebox.loader.tasks.EBoxResItemGetTask
import com.volcengine.ebox.loader.utils.EBoxLoaderUtils
import com.volcengine.ebox.loader.utils.EBoxLoaderUtils.EBOX_LOCAL
import com.volcengine.ebox.loader.utils.GsonUtils
import com.volcengine.effectone.data.EOResourceData
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.resource.data.EOResourceItem
import com.volcengine.effectone.task.Error
import com.volcengine.effectone.task.ITask
import com.volcengine.effectone.task.ITaskCallback
import com.volcengine.effectone.task.ITaskExecutor
import com.volcengine.effectone.task.TaskState
import com.volcengine.effectone.utils.EOUtils
import java.util.UUID

class EBoxPanelConfigFetchTask(override val input: String) : ITask<String, EOResourceData> {

    override var progress: Float = 0f
    override var state: TaskState = TaskState.NONE
    override var error: Error? = null
    override var taskResult: EOResourceData? = null
    override var taskCallback: ITaskCallback? = null
    private val panelKey get() = input

    override fun execute(executor: ITaskExecutor) {
        super.execute(executor)
        val panel = GsonUtils.gson.fromJson(panelKey, Panel::class.java) ?: return
        val bizKeys = panel.bizLists.map { it.key }
        val result = EBoxSDKManager.getBusinessList(bizKeys, null)

        val uiTree = panel.uiTree
        val tabs = mutableListOf<IEOResourceItem>()

        if (uiTree != null) {
            //有uiTree 根据uiTree构造列表层级
            val totalResourceItemMap = mutableMapOf<String, IEOResourceItem>()
            result.forEach {
                it.value.forEach { bizConfigItem ->
                    val item = bizConfigItem2ResourceItem(executor, bizConfigItem)
                    if (bizConfigItem.extendId.isNotEmpty()) {
                        totalResourceItemMap[bizConfigItem.extendId] = item
                    }
                }
            }
            if (result.isNotEmpty()) {
                uiTree.subItems.forEach {
                    tabs.add(subItem2EOResourceItem(it, totalResourceItemMap))
                }
            }
        } else {
            //无uiTree 根据请求创建列表层级
            result.forEach { entry ->
                val subItems = entry.value.map { bizConfigItem2ResourceItem(executor, it) }
                val eoResourceItem = EOResourceItem(
                    uniqueId = UUID.randomUUID().toString(),
                    title = panel.bizLists.firstOrNull { it.key == entry.key }?.titleDict?.getTitle() ?: "",
                    panelKey = entry.key,
                    subItems = subItems
                )
                tabs.add(eoResourceItem)
            }
        }

        if (tabs.isNotEmpty()) {
            val resourceData = EOResourceData(
                resValue = 0,
                tabs = tabs,
                resourceItem = null
            )
            onSuccess(resourceData, executor)
        } else {
            onError(EBoxErrCode.EO_ERR_PARSE, "panel config fetch failed, bizKeys: $bizKeys", executor)
        }
    }

    private fun subItem2EOResourceItem(subItem: SubItem, totalMap: Map<String, IEOResourceItem>): IEOResourceItem {
        val eoResourceItem = totalMap[subItem.uniqueId] ?: createResourceItem(subItem.uniqueId, subItem.titleDict?.getTitle() ?: "")
        eoResourceItem.subItems = subItem.subItems.map { subItem2EOResourceItem(it, totalMap) }
        return eoResourceItem
    }

    private fun bizConfigItem2ResourceItem(executor: ITaskExecutor, bizConfigItem: BizConfigItem): IEOResourceItem {
        val extraMap = mutableMapOf<String, Any>()
        extraMap["config"] = bizConfigItem.config
        var absPath = ""
        var resourceId = ""

        if (bizConfigItem.getResId() != 0L) {
            // 获取resourceItem的绝对路径
            resourceId = "${bizConfigItem.getResId()}"
            val getResItemTask = EBoxResItemGetTask(resourceId)
            val resItem = executor.execute(getResItemTask)
            absPath = resItem?.let {
                EBoxLoaderUtils.getResLocalPath(resItem)
            } ?: ""
        }

        val uniqueId = bizConfigItem.extendId.ifEmpty {
            "${bizConfigItem.id}"
        }

        return EOResourceItem(
            absPath = absPath,
            title = bizConfigItem.name,
            icon = parseIconPath(bizConfigItem.coverURL),
            panelKey = "",
            resourceId = resourceId,
            uniqueId = uniqueId,
            video = bizConfigItem.getVideoUrl(),
            extra = extraMap
        )
    }

    private fun parseIconPath(iconPath: String?): String {
        if (iconPath.isNullOrEmpty()) {
            return ""
        }
        val iconUri = Uri.parse(iconPath)
        return if (iconUri.scheme == EBOX_LOCAL) {
            "${EOUtils.pathUtil.androidAssetPrefix}${iconUri.host}${iconUri.path}"
        } else {
            iconPath
        }
    }

    private fun createResourceItem(uniqueId: String, title: String): IEOResourceItem {
        return object : IEOResourceItem {
            override var uniqueId: String = uniqueId
            override var title: String = title
            override var tips: String? = ""
            override var icon: String = ""
            override var builtInIcon: String = ""
            override var video: String = ""
            override var builtInVideo: String = ""
            override var defaultOn: Int = -1
            override var absPath: String = ""
            override var subItems: List<IEOResourceItem>? = null
            override var params: Map<String, Any>? = null
            override var extra: Map<String, Any>? = null
        }
    }
}