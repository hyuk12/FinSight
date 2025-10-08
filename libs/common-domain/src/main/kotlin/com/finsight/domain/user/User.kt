package com.finsight.domain.user

import java.time.LocalDateTime
import javax.management.monitor.StringMonitor

data class User (
    val id: String,
    val email: String,
    val name: String,
    val provider: AuthProvider,
    val providerId: String,
    val profileImageUrl: String? = null,
    val codefConnectedId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
enum class AuthProvider {
    GOOGLE,
    KAKAO,
    NAVER,
}

data class CodefRegistrationRequest(
    val organization: String,           // "0004" (신한은행 등)
    val loginType: String = "1",        // 1: 인증서, 0: ID/PW
    val userName: String,
    val identity: String,               // 주민등록번호 or 사업자번호
    val phoneNo: String,
    val certFile: String? = null,       // Base64 encoded certificate
    val certPassword: String? = null,
)

data class CodefConnectedIdResponse(
    val connectedId: String,
    val organization: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)