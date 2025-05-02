package dev.tsubaki.genchelper.logic.login

import android.app.Activity
import android.content.Context
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

interface AIDCallback {
    fun onSuccess(encryptedAID: String)
    fun onFailure(error: String)
}

class GetEncryptedAIDLogic(
    private val context: Context
) {
    // 请求 IdentifyServer 获取 Encrypted AID
    fun getEncryptedAID(callback: AIDCallback) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        val request: Request = Request.Builder()
            .url("https://my.gench.edu.cn/FAP5.IdentityServer/api/Authentication/GetAIDEncrypted")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    (context as? Activity)?.runOnUiThread {
                        callback.onFailure("服务器返回错误")
                    }
                    return
                }

                response.body?.use { responseBody ->
                    val aid = responseBody.string()
                    (context as? Activity)?.runOnUiThread {
                        callback.onSuccess(aid)
                    } ?: run {
                        callback.onFailure("返回为空")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                (context as? Activity)?.runOnUiThread {
                    e.message?.let { callback.onFailure(it) }
                }
            }
        })
    }
}