package com.effectsar.labcv.ebox.tasks

import com.volcengine.ebox.loader.EBoxErrCode
import com.volcengine.ebox.loader.EBoxSDKManager
import com.volcengine.ebox.loader.utils.EBoxLoaderUtils
import com.volcengine.effectone.resource.data.EOResourceItem
import com.volcengine.effectone.task.Error
import com.volcengine.effectone.task.ITask
import com.volcengine.effectone.task.ITaskCallback
import com.volcengine.effectone.task.ITaskExecutor
import com.volcengine.effectone.task.TaskState

class EBoxResourceFetchTaskWrapper(override val input: EOResourceItem) : ITask<EOResourceItem, EOResourceItem> {
    override var progress: Float = 0f
    override var state: TaskState = TaskState.NONE
    override var error: Error? = null
    override var taskResult: EOResourceItem? = null
    override var taskCallback: ITaskCallback? = null
    private val originEOResourceItem get() = input
    override fun execute(executor: ITaskExecutor) {
        super.execute(executor)
        val resId = input.resourceId ?: return onError(EBoxErrCode.RESOURCE_RES_ID_INVALID, "item id is null", executor)
        val eboxResItem = EBoxSDKManager.getResourceItem(resId)
        return if (eboxResItem != null) {
            onSuccess(originEOResourceItem.apply {
                md5 = eboxResItem.md5
                absPath = EBoxLoaderUtils.getResLocalPath(eboxResItem)
            }, executor)
        } else {
            onError(EBoxErrCode.EO_ERR_PARSE, "item: ${input.title} fetch failed, isOnline: ${input.online}", executor)
        }
    }
}