package com.quietfair.utils.http

/**
 * 获取网络数据回调
 * Created by petal on 2015/7/9.
 */
interface OnHttpResultGotListener {
    fun onErrorGot(tag: String, desc: NetworkErrorDesc)
    fun onPostResult(tag: String, result: String)
}
