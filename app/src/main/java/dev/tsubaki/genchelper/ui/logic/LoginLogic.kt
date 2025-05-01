package dev.tsubaki.genchelper.ui.logic

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import dev.tsubaki.genchelper.R
import dev.tsubaki.genchelper.TencentCaptchaActivity
import dev.tsubaki.genchelper.utilities.NotificationUtils
import kotlinx.serialization.Serializable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class IdentifyRequest(val username: String, val password: String)

@Serializable
data class CaptchaVerifyResponse(val randStr: String, val ticket: String)

@Serializable
data class FinalResult(val success: Boolean, val message: String)

interface AIDCallback {
    fun onSuccess(encryptedAID: String)
    fun onFailure(error: String)
}

class LoginLogic(
    private val context: Context,
    val studentID: String,
    val password: String
) {
    // 登录逻辑：
    // 用户请求 IdentifyServer, 发送账号密码 -> IdentifyServer 返回 Encrypted AID -> 带着 Encrypted AID 访问 cap_union_prehandle 进行验证码验证 -> 获取验证后返回的 randStr 和 ticket -> 带着这两个参数去请求 ValidateSignInByTencentCaptcha -> 然后获取result
    private val userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0"
    private val unencryptedAID = "192499621"

    fun startLogin() {
        // 使用回调函数获取 Encrypted AID
        getEncryptedAID(object : AIDCallback{
            override fun onSuccess(encryptedAID: String) {
                NotificationUtils.Builder(context)
                    .setTitle("AID 获取成功")
                    .setContent(encryptedAID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .show()

                val intent = Intent(null, TencentCaptchaActivity::class.java)
                context.startActivity(intent, null)
            }

            override fun onFailure(error: String) {
                NotificationUtils.Builder(context)
                    .setTitle("请求失败")
                    .setContent(error)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .show()
            }

        })
    }

    // 请求 IdentifyServer 获取 Encrypted AID
    private fun getEncryptedAID(callback: AIDCallback) {
        val client = OkHttpSingleton.instance
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

    // 单例 OkHttpClient
    object OkHttpSingleton {
        val instance: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }
}