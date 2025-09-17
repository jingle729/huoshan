package com.effectsar.labcv.ebox.base

import android.annotation.SuppressLint
import com.effectsar.labcv.effectsdk.ReflectResourceFinder
import com.volcengine.ck.logkit.LogKit
import java.io.File

class EboxResourceFinder(
    private val modelDir: String,
) : ReflectResourceFinder(modelDir) {


    @SuppressLint("[ByDesign6.4]UnsafeFile")
    private val modelFile = File(modelDir)

    companion object {
        private const val TAG = "EboxResourceFinder"
    }

    override fun findResource(modelName: String?): String? {
        if (modelName.isNullOrEmpty()) {
            return null
        }

        LogKit.i(TAG, "relative model path from sticker is: $modelName")

        val modelValidName = modelName.split("/").lastOrNull()?.split(".")?.firstOrNull()
        if (modelFile.exists() && modelFile.isDirectory && modelValidName != null) {
            val matchedModelFile = modelFile.listFiles { file ->
                file.name.contains(modelValidName, ignoreCase = true)
            }?.firstOrNull()

            if (matchedModelFile != null) {
                return "file://${modelDir}/${matchedModelFile.name}"
            }
        }
        LogKit.e(TAG, "model file: $modelValidName not found", null)
        return null
    }
}