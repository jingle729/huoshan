package com.effectsar.platform.struct

data class DeferredResult(
    val result: Boolean,
    val exception: Exception? = null,
    val platformError: PlatformError? = null
)