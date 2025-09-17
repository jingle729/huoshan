package com.effectsar.labcv.ebox.tasks

import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.resource.data.EOResourceItem
import com.volcengine.effectone.resource.tasks.DefaultEOTaskProvider
import com.volcengine.effectone.resource.tasks.EOFetchPanelConfigTask
import com.volcengine.effectone.resource.tasks.EONormalResourceCheckTask
import com.volcengine.effectone.resource.tasks.EONormalResourceFetchTask
import com.volcengine.effectone.task.ITask

class EBoxResourceTaskProvider : DefaultEOTaskProvider() {
    override fun <I, R, T : ITask<I, R>> newInstance(tClazz: Class<T>, input: I): ITask<I, R> {
        return when (tClazz) {
            //下载单个素材
            EONormalResourceFetchTask::class.java -> {
                EBoxResourceFetchTaskWrapper(input as EOResourceItem) as T
            }

            //获取素材列表
            EOFetchPanelConfigTask::class.java -> {
                EBoxPanelConfigFetchTask(input as String) as T
            }

            EONormalResourceCheckTask::class.java -> {
                EBoxResourceCheckTaskWrapper(input as IEOResourceItem) as T
            }

            else -> super.newInstance(tClazz, input)
        }
    }
}