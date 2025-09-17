package com.effectsar.labcv.ebox

import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.network.INetWorker
import com.volcengine.effectone.network.RequestInfo
import com.volcengine.effectone.network.RequestType
import com.volcengine.effectone.network.ResponseBody
import com.volcengine.effectone.network.ResponseInfo
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import java.util.Collections
import java.util.concurrent.TimeUnit

class DefaultNetWorker: INetWorker {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    override fun execute(requestInfo: RequestInfo): ResponseInfo? {
        return try {
            val requestBuilder = Request.Builder()
                .url(requestInfo.domain + requestInfo.path)
            requestInfo.headerMap?.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }

            when (requestInfo.requestType) {
                RequestType.GET -> {
                    requestBuilder.method("GET", null)
                }

                RequestType.POST -> {
                    val body: FormBody.Builder = FormBody.Builder();
                    requestInfo.body?.forEach {
                        body.add(it.key, it.value)
                    }
                    requestBuilder.method("POST", body.build())
                }

                else -> {
                    LogKit.e("DefaultNetWorker", "request type is not support", null)
                }
            }
            val request = requestBuilder.build()
            LogKit.i("DefaultNetWorker", "okhttp request is $request, body is ${request.body}")

            val response: Response = okHttpClient.newCall(request).execute()
            val body = object : ResponseBody {
                override fun readString(): String? {
                    val respStr = response.body?.string()
                    LogKit.i("DefaultNetWorker", "resp is $respStr")
                    return respStr
                }

                override fun readStream(): InputStream? {
                    return response.body?.byteStream()
                }
            }
            ResponseInfo(
                response.isSuccessful, response.message,
                response.code, body
            )
        } catch (e : Exception) {
            ResponseInfo(false, "request fail:${e.message}")
        }
    }
}