package dev.tsubaki.genchelper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import dev.tsubaki.genchelper.ui.screens.LoginScreen
import dev.tsubaki.genchelper.ui.screens.rememberLoginScreenState
import dev.tsubaki.genchelper.utilities.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        setContent {
            MaterialTheme {
                val loginState = rememberLoginScreenState()

                LoginScreen(
                    state = loginState,
                    onLoginClick = { studentID, password ->
                        // LoginLogic(studentID, password)
                        NotificationHelper.Builder(this)
                            .setTitle("MainActivity#onCreate: Test Notify")
                            .setContent("You clicked LoginScreen#onLoginClick!")
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .show()

                        NotificationHelper.Builder(this)
                            .setTitle("MainActivity#onCreate: Test Progress")
                            .setContent("Hello Progress bar here")
                            .setProgress(Int.MIN_VALUE, Int.MAX_VALUE)
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .show()
                    }
                )
            }
        }
    }
}