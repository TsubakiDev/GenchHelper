package dev.tsubaki.genchelper.logic.login

import android.app.Activity
import android.content.Context
import dev.tsubaki.genchelper.R
import dev.tsubaki.genchelper.utilities.NotificationUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

fun getSignInConfig(
    context: Context
) {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    val request: Request = Request.Builder()
        .url("https://my.gench.edu.cn/FAP5.IdentityServer/api/Config/GetSignInConfig")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            (context as? Activity)?.runOnUiThread {
                NotificationUtils.Builder(context)
                    .setTitle("预处理登录配置失败")
                    .setContent(e.message.toString())
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .show()
            }
            return
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                (context as? Activity)?.runOnUiThread {
                    response.body?.use { responseBody ->
                        NotificationUtils.Builder(context)
                            .setTitle("预处理登录配置成功")
                            .setContent(responseBody.string())
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .show()
                    }
                }
            }
        }
    })
}