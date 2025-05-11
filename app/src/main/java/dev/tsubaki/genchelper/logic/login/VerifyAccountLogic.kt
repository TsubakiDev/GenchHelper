package dev.tsubaki.genchelper.logic.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class LoginResult(val success: Boolean, val message: String)

fun verifyWithServer(
    studentID: String,
    password: String,
    ticket: String,
    randstr: String
): LoginResult {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            TODO()
        } catch (e: Exception) {
            // 处理异常
        }
    }

    return LoginResult(false, "not implemented")
}