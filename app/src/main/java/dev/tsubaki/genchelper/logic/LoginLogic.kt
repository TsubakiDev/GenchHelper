package dev.tsubaki.genchelper.logic

import android.app.Activity
import android.content.Context
import dev.tsubaki.genchelper.R
import dev.tsubaki.genchelper.logic.login.AIDCallback
import dev.tsubaki.genchelper.logic.login.GetEncryptedAIDLogic
import dev.tsubaki.genchelper.ui.screens.LoginScreenState
import dev.tsubaki.genchelper.utilities.NotificationUtils

// 登录逻辑：
// 用户请求 IdentifyServer, 发送账号密码 ->
// IdentifyServer 返回 Encrypted AID ->
// 带着 Encrypted AID 访问 cap_union_prehandle 进行验证码验证 ->
// 获取验证后返回的 randStr 和 ticket ->
// 带着这两个参数去请求 ValidateSignInByTencentCaptcha ->
// 获取result
fun startLogin(
    studentID: String,
    password: String,
    context: Context,
    state: LoginScreenState
) {
    GetEncryptedAIDLogic(context).getEncryptedAID(object : AIDCallback {
        override fun onSuccess(encryptedAID: String) {
            state.isLoading = false
            NotificationUtils.Builder(context)
                .setTitle("AID 获取成功")
                .setContent(encryptedAID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .show()
            (context as? Activity)?.runOnUiThread {
                state.showCaptcha = true
            }
        }

        override fun onFailure(error: String) {
            state.isLoading = false
            NotificationUtils.Builder(context)
                .setTitle("请求失败")
                .setContent(error)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .show()
        }
    })
}