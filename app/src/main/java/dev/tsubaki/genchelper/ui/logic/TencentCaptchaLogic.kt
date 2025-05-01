package dev.tsubaki.genchelper.ui.logic

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0"

@Composable
fun TencentCaptchaWebView(
    onDismiss: () -> Unit,
    encryptedAID: String,
    onVerify: (String, String) -> Unit
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            setupWebView(encryptedAID, onVerify)
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

@OptIn(ExperimentalEncodingApi::class)
private fun WebView.setupWebView(
    encryptedAID: String,
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

    loadUrl("https://turing.captcha.qcloud.com/cap_union_prehandle?" +
            "aid=192499621" +
            "&protocol=https" +
            "&accver=1" +
            "&showtype=popup" +
            "&ua=${Base64.encode(userAgent.encodeToByteArray())}" +
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
            "&callback=_aq_146009" +
            "&sess="
    )
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