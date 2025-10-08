package com.finsight.api.user

import com.finsight.domain.user.CodefRegistrationRequest
import com.finsight.domain.user.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<User> {
        val user = userService.saveOrUpdateUser(authentication)
        return ResponseEntity.ok(user)
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
    fun getMyAccounts(authentication: Authentication): ResponseEntity<List<Map<String, Any>>> {
        val user = userService.saveOrUpdateUser(authentication)
        val accounts = userService.getUserAccounts(user.id)
        return ResponseEntity.ok(accounts)
    }
}