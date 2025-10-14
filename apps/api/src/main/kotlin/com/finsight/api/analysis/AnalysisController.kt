package com.finsight.api.analysis

import com.finsight.domain.analysis.AnalysisResult
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analysis")
class AnalysisController (
    private val analysisService: AnalysisService,
){
    /**
     * 테스트용: 더미 데이터로 분석 실행
     */
    @PostMapping("/test")
    fun testAnalysis(authentication: Authentication): AnalysisResult {
        val user = authentication.principal as OAuth2User
        val email = user.getAttribute<String>("email") ?: "test@example.com"
        val name = user.getAttribute<String>("name") ?: "테스트"

        return analysisService.analyzeDummyData(
            userId = email,
            userName = name
        )
    }

    /**
     * Agent 서버 상태 확인
     */
    @GetMapping("/status")
    fun getAgentStatus(): Map<String, Any> {
        return mapOf(
            "agent_url" to (System.getenv("AGENT_API_URL") ?: "http://localhost:8000"),
            "message" to "Agent service is available"
        )
    }
}