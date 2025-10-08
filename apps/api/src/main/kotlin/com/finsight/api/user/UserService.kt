package com.finsight.api.user

import com.finsight.api.codef.CodefTokenService
import com.finsight.domain.user.AuthProvider
import com.finsight.domain.user.CodefConnectedIdResponse
import com.finsight.domain.user.CodefRegistrationRequest
import com.finsight.domain.user.User
import com.finsight.infra.codef.CodefClient
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class UserService(
    private val codefClient: CodefClient,
    private val tokenService: CodefTokenService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 임시 저장소 (프로덕션에서는 PostgreSQL 등 사용)
    private val userStore = ConcurrentHashMap<String, User>()

    /**
     * OAuth2 로그인 후 사용자 정보 저장/갱신
     */
    fun saveOrUpdateUser(authentication: Authentication): User {
        val oauth2User = authentication.principal as OAuth2User
        val email = oauth2User.getAttribute<String>("email")
            ?: throw IllegalArgumentException("Email not found in OAuth2 user")

        val existingUser = userStore[email]

        return if (existingUser != null) {
            existingUser.copy(updatedAt = LocalDateTime.now())
                .also { userStore[email] = it }
        } else {
            val newUser = User(
                id = UUID.randomUUID().toString(),
                email = email,
                name = oauth2User.getAttribute("name") ?: "Unknown",
                provider = AuthProvider.GOOGLE,
                providerId = oauth2User.getAttribute("sub") ?: "",
                profileImageUrl = oauth2User.getAttribute("picture")
            )
            userStore[email] = newUser
            logger.info("New user registered: ${newUser.email}")
            newUser
        }
    }

    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): User? = userStore[email]

    /**
     * CODEF ConnectedId 등록
     */
    fun registerCodefConnection(
        userId: String,
        request: CodefRegistrationRequest
    ): CodefConnectedIdResponse {
        val user = userStore.values.find { it.id == userId }
            ?: throw IllegalArgumentException("User not found: $userId")

        if (user.codefConnectedId != null) {
            logger.warn("User ${user.email} already has CODEF connection")
        }

        // CODEF API 호출
        val token = tokenService.getValidToken()
        val response = codefClient.createConnectedId(token, request)

        // ConnectedId 저장
        val updatedUser = user.copy(
            codefConnectedId = response.connectedId,
            updatedAt = LocalDateTime.now()
        )
        userStore[user.email] = updatedUser

        logger.info("CODEF connection registered for user: ${user.email}")
        return response
    }

    /**
     * 사용자 계좌 목록 조회
     */
    fun getUserAccounts(userId: String): List<Map<String, Any>> {
        val user = userStore.values.find { it.id == userId }
            ?: throw IllegalArgumentException("User not found: $userId")

        val connectedId = user.codefConnectedId
            ?: throw IllegalStateException("User has no CODEF connection")

        val token = tokenService.getValidToken()
        return codefClient.getAccounts(token, connectedId)
    }
}