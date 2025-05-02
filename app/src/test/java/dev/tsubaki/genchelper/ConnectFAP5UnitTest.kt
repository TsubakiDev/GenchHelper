package dev.tsubaki.genchelper

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tsubaki.genchelper.logic.login.AIDCallback
import dev.tsubaki.genchelper.logic.login.GetEncryptedAIDLogic
import org.junit.Test

class ConnectFAP5UnitTest {
    @Composable
    @Test
    fun connect_server() {
        val context = LocalContext.current

        GetEncryptedAIDLogic(context).getEncryptedAID(object : AIDCallback {
            override fun onSuccess(encryptedAID: String) {
                println("succ: $encryptedAID")
            }

            override fun onFailure(error: String) {
                println("err: $error")
            }
        })
    }
}