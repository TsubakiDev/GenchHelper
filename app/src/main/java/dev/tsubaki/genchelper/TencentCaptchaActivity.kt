package dev.tsubaki.genchelper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.tsubaki.genchelper.utilities.NotificationUtils

class TencentCaptchaActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TencentCaptchaWebScreen()
            }
        }
    }

    @Composable
    private fun TencentCaptchaWebScreen() {
        var receivedData by remember { mutableStateOf<String?>(null) }

        Column(Modifier.fillMaxSize()) {
            TencentCaptchaWebView(
                modifier = Modifier.weight(1f),
                onDataReceived = { data ->
                    receivedData = data

                }
            )

            receivedData?.let { data ->
                Text(
                    text = "验证码结果: $data",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TencentCaptchaWebView(
    modifier: Modifier = Modifier,
    onDataReceived: (String) -> Unit // 接收 JS 回调的数据
) {
    val context = LocalContext.current

    // 创建 WebView 实例（带状态记忆）
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                javaScriptEnabled = true
            }
            webViewClient = CaptchaWebViewClient()
            addJavascriptInterface(CaptchaJsBridge(context, onDataReceived), "jsBridge")
        }
    }

    // 处理 WebView 生命周期
    DisposableEffect(webView) {
        webView.loadUrl("https://uring.captcha.qcloud.com/cap_union_prehandle")
        onDispose { webView.destroy() }
    }

    // 嵌入到 Compose UI
    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

// 自定义 WebViewClient（处理腾讯验证码特定逻辑）
private class CaptchaWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        view?.loadUrl(url)
        return true
    }
}

// 自定义 JS 接口
private class CaptchaJsBridge(
    private val context: Context,
    private val onDataReceived: (String) -> Unit
) {
    @JavascriptInterface
    fun getData(data: String) {
        (context as? Activity)?.runOnUiThread {
            onDataReceived(data) // 将数据传递给外部处理
            // 显示通知
            NotificationUtils.Builder(context)
                .setTitle("验证码数据")
                .setContent(data)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .show()
        }
    }
}