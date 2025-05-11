package dev.tsubaki.genchelper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dev.tsubaki.genchelper.logic.startLogin
import dev.tsubaki.genchelper.ui.screens.LoginScreen
import dev.tsubaki.genchelper.ui.screens.rememberLoginScreenState

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
                val loginState = rememberLoginScreenState()

                Surface {
                    LoginScreen(
                        state = loginState,
                        onLoginClick = { studentID, password ->
                            startLogin(studentID, password, this, loginState)
                        }
                    )
                }
            }
        }
    }
}
