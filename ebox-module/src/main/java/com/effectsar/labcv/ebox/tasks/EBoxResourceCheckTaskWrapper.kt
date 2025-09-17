package com.effectsar.labcv.ebox.tasks

import com.volcengine.ebox.loader.EBoxSDKManager
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.resource.data.EOResourceItem
import com.volcengine.effectone.task.Error
import com.volcengine.effectone.task.ITask
import com.volcengine.effectone.task.ITaskCallback
import com.volcengine.effectone.task.ITaskExecutor
import com.volcengine.effectone.task.TaskState

class EBoxResourceCheckTaskWrapper(override val input: IEOResourceItem) : ITask<IEOResourceItem, Boolean> {
    override var progress: Float = 0f
    override var state: TaskState = TaskState.NONE
    override var error: Error? = null
    override var taskResult: Boolean? = null
    override var taskCallback: ITaskCallback? = null

    override fun execute(executor: ITaskExecutor) {
        super.execute(executor)
        val resId = (input as EOResourceItem).resourceId!!
        val res = EBoxSDKManager.checkResourceReady(resId)
        onSuccess(res, executor)
    }
}