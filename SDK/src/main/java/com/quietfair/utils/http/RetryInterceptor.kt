package com.quietfair.utils.http

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Created by petal on 2017/6/20.
 * 设置请求失败重试
 */

class RetryInterceptor : Interceptor {
    private val maxRetry = 5
    private var retryNum = 1
    private val log = LoggerFactory.getLogger(RetryInterceptor::class.java)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        while (retryNum <= maxRetry) {
            try {
                val response = chain.proceed(request)
                if (response.isSuccessful) {
                    retryNum = 1
                    return response
                }
            } catch (e: Exception) {
                log.error("intercept error", e)
            }
            log.warn("request failed, retry $retryNum times")
            Thread.sleep(3000)
            retryNum++
        }
        retryNum = 1
        throw IOException("reached max retry times!")
    }
}
