package dev.tsubaki.genchelper.logic

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dev.tsubaki.genchelper.R
import dev.tsubaki.genchelper.utilities.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0"

@Serializable
data class IdentifyRequest(val username: String, val password: String)

@Serializable
data class CaptchaVerifyResponse(val randStr: String, val ticket: String)

@Serializable
data class FinalResult(val success: Boolean, val message: String)

@Composable
fun TencentCaptchaWebView(
    onDismiss: () -> Unit,
    encryptedAID: String,
    onVerify: (String, String) -> Unit
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            setupWebView(onVerify)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}

private fun WebView.setupWebView(
    onVerify: (String, String) -> Unit
) {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
    }

    addJavascriptInterface(object {
        @JavascriptInterface
        fun onVerify(ticket: String, randstr: String) {
            Handler(Looper.getMainLooper()).post {
                onVerify(ticket, randstr)
            }
        }
    }, "AndroidInterface")

    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.url?.toString()?.let { view?.loadUrl(it) }
            return true
        }
    }

    loadUrl("")
}

@OptIn(ExperimentalEncodingApi::class)
fun preHandleCaptcha(
    context: Context,
    encryptedAID: String
) {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    val request: Request = Request.Builder()
        .url(
            "https://turing.captcha.qcloud.com/cap_union_prehandle?" +
                    "aid=192499621" +
                    "&protocol=https" +
                    "&accver=1" +
                    "&showtype=popup" +
                    "&ua=${Base64.encode(userAgent.toByteArray())}" +
                    "&noheader=1" +
                    "&fb=1" +
                    "&aged=0" +
                    "&enableAged=0" +
                    "&enableDarkMode=0" +
                    "&grayscale=1" +
                    "&clientype=2" +
                    "&aidEncrypted=${encryptedAID}" +
                    "&cap_cd=" +
                    "&uid=" +
                    "&lang=en" +
                    "&entry_url=https://my.gench.edu.cn/FAP5.IdentityServer/SignIn.html" +
                    "&elder_captcha=0" +
                    "&js=/tcaptcha-frame.c055d939.js" +
                    "&login_appid=" +
                    "&wb=1" +
                    "&subsid=1" +
                    "&callback=_aq_143146" +
                    "&sess="
        )
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            (context as? Activity)?.runOnUiThread {
                NotificationUtils.Builder(context)
                    .setTitle("PreHandle 验证码失败")
                    .setContent(e.message.toString())
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            (context as? Activity)?.runOnUiThread {
                NotificationUtils.Builder(context).apply {
                    setTitle("PreHandle 验证码${if (response.isSuccessful) "成功" else "失败"}")
                    setContent(response.takeIf { it.isSuccessful }?.let { call.toString() }
                        ?: response.message)
                    setSmallIcon(R.drawable.ic_launcher_background)
                }.show()
            }
        }
    })
}

fun verifyWithServer(ticket: String, randstr: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // 验证逻辑

        } catch (e: Exception) {
            // 处理异常
        }
    }
}