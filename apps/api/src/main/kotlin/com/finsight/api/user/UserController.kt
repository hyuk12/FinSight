package com.finsight.api.user

import com.finsight.domain.user.CodefRegistrationRequest
import com.finsight.domain.user.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 정보 및 CODEF 연동 API
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val user = userService.saveOrUpdateUser(authentication)
        return ResponseEntity.ok(
            mapOf(
                "user" to user,
                "codefEnabled" to userService.isCodefEnabled()
            )
        )
    }

    /**
     * CODEF 연동 등록
     *
     * Example request:
     * {
     *   "organization": "0004",
     *   "loginType": "1",
     *   "userName": "홍길동",
     *   "identity": "8801011234567",
     *   "phoneNo": "01012345678",
     *   "certFile": "base64_encoded_cert",
     *   "certPassword": "cert_password"
     * }
     */
    @PostMapping("/me/codef-connection")
    fun registerCodefConnection(
        authentication: Authentication,
        @RequestBody request: CodefRegistrationRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!userService.isCodefEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                mapOf(
                    "error" to "CODEF service is not enabled",
                    "message" to "Please set codef.enabled=true and provide credentials"
                )
            )
        }

        val user = userService.saveOrUpdateUser(authentication)
        val response = userService.registerCodefConnection(user.id, request)

        return ResponseEntity.ok(
            mapOf(
                "connectedId" to response.connectedId,
                "organization" to response.organization,
                "message" to "CODEF connection registered successfully"
            )
        )
    }

    /**
     * 사용자 계좌 목록 조회
     */
    @GetMapping("/me/accounts")
    fun getMyAccounts(authentication: Authentication): ResponseEntity<Any> {
        if (!userService.isCodefEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                mapOf(
                    "error" to "CODEF service is not enabled",
                    "message" to "Please set codef.enabled=true and provide credentials"
                )
            )
        }

        val user = userService.saveOrUpdateUser(authentication)
        val accounts = userService.getUserAccounts(user.id)
        return ResponseEntity.ok(accounts)
    }

    /**
     * CODEF 서비스 상태 확인
     */
    @GetMapping("/codef/status")
    fun getCodefStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "enabled" to userService.isCodefEnabled(),
                "message" to if (userService.isCodefEnabled()) {
                    "CODEF service is available"
                } else {
                    "CODEF service is not configured"
                }
            )
        )
    }
}