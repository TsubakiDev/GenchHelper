package dev.tsubaki.genchelper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.tsubaki.genchelper.ui.logic.AIDCallback
import dev.tsubaki.genchelper.ui.logic.GetEncryptedAIDLogic
import dev.tsubaki.genchelper.ui.logic.TencentCaptchaWebView
import dev.tsubaki.genchelper.ui.logic.verifyWithServer
import dev.tsubaki.genchelper.ui.screens.LoginScreen
import dev.tsubaki.genchelper.ui.screens.rememberLoginScreenState
import dev.tsubaki.genchelper.utilities.NotificationUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.INTERNET
            ), 0)
        }

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val loginState = rememberLoginScreenState()
                var showWebView by remember { mutableStateOf(false) }
                val (showCaptcha, setShowCaptcha) = remember { mutableStateOf(false) }
                val (encryptedAID, setEncryptedAID) = remember { mutableStateOf<String?>(null) }

                Surface {
                    LoginScreen(
                        state = loginState,
                        onLoginClick = { studentID, password ->
                            GetEncryptedAIDLogic(this).getEncryptedAID(object : AIDCallback {
                                override fun onSuccess(encryptedAID: String) {
                                    setEncryptedAID(encryptedAID)
                                    setShowCaptcha(true)
                                    NotificationUtils.Builder(context)
                                        .setTitle("AID 获取成功")
                                        .setContent(encryptedAID)
                                        .setSmallIcon(R.drawable.ic_launcher_background)
                                        .show()
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
                    )

                    if (showCaptcha && encryptedAID != null) {
                        TencentCaptchaWebView(
                            onDismiss = { showWebView = false },
                            onVerify = { ticket, randstr ->
                                NotificationUtils.Builder(context)
                                    .setTitle("验证成功")
                                    .setContent("Ticket: $ticket Random: $randstr")
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .show()
                                verifyWithServer(ticket, randstr)
                            },
                            encryptedAID = encryptedAID
                        )
                    }
                }
            }
        }
    }
}
