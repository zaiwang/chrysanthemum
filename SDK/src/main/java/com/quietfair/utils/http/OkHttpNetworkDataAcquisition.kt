package com.quietfair.utils.http

import okhttp3.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * 使用OkHttp网络操作
 * Created by petal on 2015/9/10.
 */
object OkHttpNetworkDataAcquisition {

    @JvmField
    val HTTP_CANCELED = -100
    @JvmField
    val HTTP_UNKNOWN = -1
    @JvmField
    val HTTP_404 = -404
    @JvmField
    val HTTP_500 = -500
    @JvmField
    val HTTP_401 = -401
    @JvmField
    val HTTP_TIME_OUT = -101
    @JvmField
    val HTTP_NO_HOST = -102
    @JvmField
    val HTTP_CONNECT_ERROR = -1001
    @JvmField
    val HTTP_SERVER_DEFINED_ERROR = -10

    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS).writeTimeout(3, TimeUnit.SECONDS).addInterceptor(RetryInterceptor()).build()
    private var headersMap: Map<String, String?>? = null
    private val log = LoggerFactory.getLogger(OkHttpNetworkDataAcquisition::class.java)

    private fun prepareRequest(url: String, params: Map<String, String>?): Request? {
        log.trace("Connect url(get)>>$url")
        val queryUrl: String
        queryUrl = if (params != null && params.isNotEmpty()) {
            log.trace("Query:$params")
            val sb = StringBuilder()
            for ((k, v) in params) {
                sb.append(k).append("=").append(URLEncoder.encode(v, "UTF-8")).append("&")
            }
            sb.deleteCharAt(sb.length - 1)
            String.format("%s?%s", url, sb.toString())
        } else {
            url
        }
        val builder = Request.Builder()
        addHttpHead(builder, headersMap)
        builder.tag(url)
        return try {
            builder.url(queryUrl).build()
        } catch (e: IllegalArgumentException) {
            null
        }

    }

    fun getData(url: String, httpResultGotListener: OnHttpResultGotListener) {
        getData(url, null, httpResultGotListener)
    }

    fun getData(url: String, params: Map<String, String>?, httpResultGotListener: OnHttpResultGotListener) {
        val request = prepareRequest(url, params)
        if (request == null) {
            httpResultGotListener.onErrorGot(url, NetworkErrorDesc(-10000001))
        } else {
            callNetwork(request, httpResultGotListener)
        }
    }

    @Throws(IOException::class)
    fun getData(url: String): String? {
        val request = prepareRequest(url, null)
        return if (request == null) {
            null
        } else {
            callNetwork(request)
        }
    }

    @Throws(IOException::class)
    fun getData(url: String, params: Map<String, String>): String? {
        val request = prepareRequest(url, params)
        return if (request == null) {
            null
        } else {
            callNetwork(request)
        }
    }

    fun postKeyValue(url: String, params: Map<String, String>?, httpResultGotListener: OnHttpResultGotListener) {
        log.trace("Connect url(post)>>$url")
        val formEncodingBuilder = FormBody.Builder()
        if (params != null && params.isNotEmpty()) {
            log.trace("Post data:$params")
            for (entry in params.entries) {
                val key = entry.key
                val `val` = entry.value
                formEncodingBuilder.add(key, `val`)
            }
        }
        val formBody = formEncodingBuilder.build()
        val builder = Request.Builder()
        addHttpHead(builder, headersMap)
        builder.tag(url)
        val request = builder.url(url).post(formBody).build()
        callNetwork(request, httpResultGotListener)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun postStringSynchronous(url: String, post: String?, mediaType: String = "application/json; charset=utf-8"): String? {
        log.trace("http connect to url>>$url\npost body:\n\t$post")
        val postData = post ?: "{}"
        val body = RequestBody.create(MediaType.parse(mediaType), postData)
        val builder = Request.Builder()
        addHttpHead(builder, headersMap)
        builder.tag(url)
        val request = builder.url(url).post(body).build()
        return callNetwork(request)
    }

    fun postString(url: String, post: String?, httpResultGotListener: OnHttpResultGotListener?) {
        postString(url, post, "application/json; charset=utf-8", httpResultGotListener)
    }

    fun postString(url: String, post: String?, mediaType: String, httpResultGotListener: OnHttpResultGotListener?) {
        log.trace("http connect to url>>$url\npost body:\n\t$post")
        val postData = post ?: "{}"
        val body = RequestBody.create(MediaType.parse(mediaType), postData)
        val builder = Request.Builder()
        addHttpHead(builder, headersMap)
        builder.tag(url)
        val request = builder.url(url).post(body).build()
        callNetwork(request, httpResultGotListener)
    }

    @Throws(IOException::class)
    fun postFile(url: String, path: String): String? {
        log.trace("http connect to url>>$url\npost file:\n\t$path")
        val file = File(path)
        val fileBody = RequestBody.create(MediaType.parse("audio/amr"), file)
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, fileBody).build()
        val builder = Request.Builder()
        addHttpHead(builder, headersMap)
        builder.tag(url)
        val request = builder.url(url).post(requestBody).build()
        return callNetwork(request)
    }

    fun postFile(url: String, path: String, httpResultGotListener: OnHttpResultGotListener) {
        log.trace("http connect to url>>$url\npost file:\n\t$path")
        val file = File(path)
        val fileBody = RequestBody.create(MediaType.parse("audio/amr"), file)
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, fileBody).build()
        val builder = Request.Builder()
        addHttpHead(builder, headersMap)
        builder.tag(url)
        val request = builder.url(url).post(requestBody).build()
        callNetwork(request, httpResultGotListener)
    }

    fun setHttpHeaders(headers: Map<String, String?>) {
        headersMap = headers

    }

    private fun callNetwork(request: Request, httpResultGotListener: OnHttpResultGotListener?) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                log.error("callNetwork onFailure", e)
                val desc = NetworkErrorDesc()
                if (e is UnknownHostException) {
                    desc.error = HTTP_NO_HOST
                } else if (e is SocketTimeoutException) {
                    desc.error = HTTP_TIME_OUT
                } else if (e is ConnectException) {
                    desc.error = HTTP_CONNECT_ERROR
                } else {
                    if ("Canceled".equals(e.message, ignoreCase = true)) {
                        desc.error = HTTP_CANCELED
                    } else {
                        desc.error = HTTP_UNKNOWN
                    }
                }
                httpResultGotListener?.onErrorGot(request.url().toString(), desc)
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code()
                log.trace("get http code:" + code + ",in thread:" + Thread.currentThread().id)
                val fullUrl = request.url().toString()
                val url: String
                url = if (fullUrl.contains("?")) {
                    fullUrl.substring(0, fullUrl.indexOf("?"))
                } else {
                    fullUrl
                }
                if (response.isSuccessful) {
                    try {
                        val result = response.body()?.string()
                        log.trace("Got data<<$result<<From $fullUrl")
                        httpResultGotListener?.onPostResult(url, result ?: "{}")
                    } catch (e: IOException) {
                        log.error("callNetwork IOException", e)
                        val desc = NetworkErrorDesc()
                        desc.error = HTTP_UNKNOWN
                        httpResultGotListener?.onErrorGot(url, desc)
                    }

                } else {
                    val desc = NetworkErrorDesc()
                    desc.error = -code
                    httpResultGotListener?.onErrorGot(url, desc)
                }
            }
        })
    }

    @Throws(IOException::class)
    private fun callNetwork(request: Request): String? {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val result = response.body()?.string()
            log.trace("Got data<<" + result + "<<From " + request.url().toString())
            return result
        } else {
            throw IOException("Unexpected code $response")
        }
    }

    private fun addHttpHead(builder: Request.Builder, heads: Map<String, String?>?) {
        if (heads != null && heads.isNotEmpty()) {
            log.trace("Set http head:$heads")
            for (entry in heads.entries) {
                val key = entry.key
                val `val` = entry.value
                builder.header(key, `val`)
            }
        }
    }

    private fun _prepareDownload(id: Int, url: String?, destFileDir: String?, _fileName: String?, force: Boolean, downloadProgress: ProcessProgress?): File? {
        var fileName = _fileName
        if (url == null || url == "") {
            log.error("url is null, return")
            downloadProgress?.onError(id, null)
            return null
        }
        if (fileName == null || fileName == "") {
            fileName = url.substring(url.lastIndexOf('/') + 1)
            log.debug("get file name $fileName from url>>$url")
        }
        if (destFileDir == null || destFileDir == "") {
            log.error("destFileDir is null, return")
            downloadProgress?.onError(id, null)
            return null
        }
        val fileDir = File(destFileDir)
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            log.error("cannot create dir, return")
            downloadProgress?.onError(id, null)
            return null
        }
        downloadProgress?.onBegin(id)
        log.debug("Begin to download file $fileName form $url to $destFileDir")
        val downloadFile = File(destFileDir, fileName)
        if (downloadFile.isFile && downloadFile.exists()) {
            if (force) {
                if (downloadFile.delete()) {
                    log.debug("file $downloadFile already exist, will override")
                } else {
                    log.error("file $downloadFile delete failed,, return")
                    downloadProgress?.onError(id, null)
                    return null
                }
            } else {
                log.warn("file $downloadFile already exist, no need to download,return")
                downloadProgress?.onEnd(id, downloadFile.absolutePath)
                return null
            }
        }
        return downloadFile
    }

    private fun _disposeDownloadResponse(response: Response, downloadFile: File?, finalFileName: String, downloadProgress: ProcessProgress?, id: Int) {
        if (response.isSuccessful) {
            var fos: FileOutputStream? = null
            var inputStream: InputStream? = null
            try {
                var fileLength = -1
                try {
                    fileLength = Integer.parseInt(response.header("Content-Length"))
                    log.debug("get file length:$fileLength")
                } catch (e: Exception) {
                    log.error("Exception", e)
                }

                inputStream = response.body()?.byteStream()
                fos = FileOutputStream(downloadFile!!)
                val buf = ByteArray(1024 * 1024)
                var downloadSize = 0
                var len: Int?
                var lastPercentage = -1
                while (true) {
                    len = inputStream?.read(buf)
                    if (len == -1 || len == null) {
                        break
                    }
                    fos.write(buf, 0, len)
                    downloadSize += len
                    if (fileLength > 0) {
                        val percentage = (downloadSize.toFloat() / fileLength * 100).toInt()
                        if (percentage != lastPercentage) {
                            log.trace("$finalFileName have download $percentage%")
                        }
                        lastPercentage = percentage
                    }
                    downloadProgress?.onProcess(id, downloadSize, fileLength)
                }
                log.debug("$finalFileName download ok")
                downloadProgress?.onEnd(id, downloadFile.absolutePath)
            } catch (e: IOException) {
                log.error("IOException", e)
                downloadProgress?.onError(id, null)
                if (downloadFile!!.delete()) {
                    log.debug("file $downloadFile clear")
                }
            } finally {
                fos?.flush()
                fos?.close()
                inputStream?.close()
            }

        } else {
            log.error("download failed")
            downloadProgress?.onError(id, null)
            if (downloadFile!!.delete()) {
                log.debug("file $downloadFile clear")
            }
        }
    }

    private fun _disposeDownloadError(e: Exception?, downloadFile: File?, downloadProgress: ProcessProgress?, id: Int) {
        log.error("onFailure", e)
        downloadProgress?.onError(id, null)
        if (downloadFile!!.delete()) {
            log.debug("file $downloadFile clear")
        }
    }

    /**
     * 下载文件 TODO 尚未完成(未定义错误类型)
     *
     * @param url         文件所在路径
     * @param destFileDir 下载文件到本地的路径
     * @param fileName    指定文件名
     * @param force       强制下载，即如果本地文件已经存在则覆盖，反之则不下载
     */
    fun downloadFile(id: Int, url: String, destFileDir: String, fileName: String, force: Boolean, downloadProgress: ProcessProgress) {
        val downloadFile = _prepareDownload(id, url, destFileDir, fileName, force, downloadProgress)
                ?: return
        val finalFileName = downloadFile.name
        val request = prepareRequest(url, null)
        if (request == null) {
            _disposeDownloadError(null, downloadFile, downloadProgress, id)
            return
        }
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _disposeDownloadError(e, downloadFile, downloadProgress, id)
            }

            override fun onResponse(call: Call, response: Response) {
                _disposeDownloadResponse(response, downloadFile, finalFileName, downloadProgress, id)
            }
        })

    }

    fun downloadFileDirect(id: Int, url: String, destFileDir: String, fileName: String, force: Boolean, downloadProgress: ProcessProgress) {
        val downloadFile = _prepareDownload(id, url, destFileDir, fileName, force, downloadProgress)
                ?: return
        val finalFileName = downloadFile.name
        try {
            val request = prepareRequest(url, null)
            if (request == null) {
                _disposeDownloadError(null, downloadFile, downloadProgress, id)
            } else {
                val response = client.newCall(request).execute()
                _disposeDownloadResponse(response, downloadFile, finalFileName, downloadProgress, id)
            }
        } catch (e: IOException) {
            _disposeDownloadError(e, downloadFile, downloadProgress, id)
        }

    }

}
