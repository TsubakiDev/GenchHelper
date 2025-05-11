package dev.tsubaki.genchelper.logic.login

import android.app.Activity
import android.content.Context
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

@Serializable
data class IdentifyRequest(val username: String, val password: String)

@Serializable
data class CaptchaVerifyResponse(val randStr: String, val ticket: String)

@Serializable
data class FinalResult(val success: Boolean, val message: String)

@Composable
fun TencentCaptchaWebView(
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

    addJavascriptInterface(ResponseData(), "AndroidInterface")

    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.url?.toString()?.let { view?.loadUrl(it) }
            return true
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)

            view.postDelayed({
                val jsScript = """
                (function() {
                    try {
                        var iframe = document.getElementById('tencent_iframe_by');
                        if (!iframe) return "iframe未找到";
                    
                        var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                        return iframeDoc.documentElement.outerHTML;
                    } catch (e) {
                        return "访问错误: " + e.message;
                    }
                })();
            """.trimIndent()

                view.evaluateJavascript(jsScript) { src ->
                    src?.takeIf { it != "null" }?.removeSurrounding("\"")?.let { iframeUrl ->
                        loadUrl(iframeUrl)
                    }
                }
            }, 2000)
        }
    }

    loadUrl("https://my.gench.edu.cn/FAP5.IdentityServer/SignIn.html")
}

class ResponseData() {
    @JavascriptInterface
    fun getResponseData(data: String, context: Context) {
        (context as? Activity)?.runOnUiThread {
            NotificationUtils.Builder(context)
                .setTitle("验证码验证通过, 返回json: ")
                .setContent(data)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .show()
        }
    }
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
                    "&ua=${Base64.encode("Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0".toByteArray())}" +
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