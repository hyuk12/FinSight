package com.finsight.domain.codef

import java.time.LocalDateTime

data class CodefToken (
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val scope: String,
    val issuedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isExpired(): Boolean {
        val expiryTime = issuedAt.plusSeconds(expiresIn)
        return LocalDateTime.now().isAfter(expiryTime)
    }
}

data class CodefTokenRequest(
    val grantType: String = "client_credentials",
    val scope: String = "read",
)

data class CodefApiResponse<T>(
    val result: CodefResult,
    val data: T?,
)

data class CodefResult(
    val code: String,
    val message: String,
    val extraMessage: String? = null,
) {
    fun isSuccess() = code == "CF-00000"
}