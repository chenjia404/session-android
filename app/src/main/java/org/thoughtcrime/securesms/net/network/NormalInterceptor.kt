package org.thoughtcrime.securesms.net.network

import okhttp3.*
import org.thoughtcrime.securesms.util.Logger
import java.io.IOException
import java.nio.charset.Charset

class NormalInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        Logger.d("--------------Start----------------")
        Logger.d(request.toString())
        val method = request.method
        if ("POST" == method) {
            val sb = StringBuilder()
            if (request.body is FormBody) {
                val body = request.body as FormBody?
                for (i in 0 until body!!.size) {
                    sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                }
                sb.delete(sb.length - 1, sb.length)
                Logger.d("RequestParams:{$sb}")
            } else if (request.body is RequestBody) {
                val requestBody = request.body
            }
        }
        Logger.d("--------------End--------------")
        val response: Response = chain.proceed(request)
        decrypt(response)
        return response
    }

    companion object {
        @Throws(IOException::class)
        fun decrypt(response: Response): Response {
            var response = response
            if (response.isSuccessful) {
                //the response data
                val body = response.body
                val source = body!!.source()
                // Buffer the entire body
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer()
                var charset = Charset.defaultCharset()
                val contentType = body.contentType()
                if (contentType != null) {
                    charset = contentType.charset(charset)
                }
                val result = buffer.clone().readString(charset!!)
                Logger.d("result = $result")
                val responseBody = ResponseBody.create(contentType, result)
                response = response.newBuilder().body(responseBody).build()
            }
            return response
        }
    }
}