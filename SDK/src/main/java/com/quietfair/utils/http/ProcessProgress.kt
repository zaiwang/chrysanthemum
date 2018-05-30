package com.quietfair.utils.http

/**
 * 上传下载等处理进度
 * Created by petal on 2015/9/16.
 */
interface ProcessProgress {
    fun onBegin(id: Int)
    fun onProcess(id: Int, progress: Int, total: Int)
    fun onEnd(id: Int, downloadFile: String)
    fun onError(id: Int, desc: NetworkErrorDesc?)
}
