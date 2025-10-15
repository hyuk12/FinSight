package com.finsight.api.home

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Controller
class HomeController {

    /**
     * 루트 경로 - 로그인 여부에 따라 분기
     */
    @GetMapping("/")
    fun home(authentication: Authentication?): String {
        return if (authentication != null && authentication.isAuthenticated) {
            // 로그인되어 있으면 대시보드로 리다이렉션
            "redirect:/dashboard.html"
        } else {
            // 로그인 안되어 있으면 로그인 페이지
            "forward:/login.html"
        }
    }
}

/**
 * 사용자 정보 API
 */
@RestController
@RequestMapping("/api/user")
class UserInfoController {

    @GetMapping("/info")
    fun getUserInfo(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val oauth2User = authentication.principal as OAuth2User

        val userInfo = mapOf(
            "name" to (oauth2User.getAttribute<String>("name") ?: "Unknown"),
            "email" to (oauth2User.getAttribute<String>("email") ?: "unknown@example.com"),
            "picture" to (oauth2User.getAttribute<String>("picture") ?: ""),
            "authenticated" to true
        )

        return ResponseEntity.ok(userInfo)
    }

    @GetMapping("/status")
    fun getStatus(authentication: Authentication?): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "authenticated" to (authentication != null && authentication.isAuthenticated)
            )
        )
    }
}