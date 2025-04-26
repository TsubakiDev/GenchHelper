package dev.tsubaki.genchelper.ui.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

@Serializable
data class IdentifyRequest(val username: String, val password: String)

@Serializable
data class IdentifyResponse(val encryptedAID: String)

@Serializable
data class CaptchaVerifyResponse(val randStr: String, val ticket: String)

@Serializable
data class FinalResult(val success: Boolean, val message: String)

class LoginLogic(
    studentID: String,
    password: String
) {
    // 登录逻辑：
    // 用户请求 IdentifyServer, 发送账号密码 -> IdentifyServer 返回 Encrypted AID -> 带着 Encrypted AID 访问 cap_union_prehandle 进行验证码验证 (ua 和 unencrypted aid 都是不变的) -> 获取验证后返回的 randStr 和 ticket -> 带着这两个参数去请求 ValidateSignInByTencentCaptcha -> 然后获取result
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private var userAgent = "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzEzNS4wLjAuMCBTYWZhcmkvNTM3LjM2"
    private var unencryptedAID = "192499621"
}