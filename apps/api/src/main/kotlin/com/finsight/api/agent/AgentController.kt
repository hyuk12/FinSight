package com.finsight.api.agent

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

data class AgentRequest(
    val user_id: String,
    val request: String
)

data class AgentResponse(
    val user_id: String,
    val request: String,
    val workflow: Map<String, Any>,
    val results: List<Map<String, Any>>,
    val execution_time: Double,
    val status: String
)

@RestController
@RequestMapping("/api/agent")
class AgentController(
    private val restTemplate: RestTemplate
) {
    private val agentUrl = System.getenv("AGENT_API_URL") ?: "http://localhost:8000"

    /**
     * 자연어 요청을 Multi-Agent 시스템으로 전달
     */
    @PostMapping("/execute")
    fun executeAgent(
        authentication: Authentication,
        @RequestBody request: Map<String, String>
    ): AgentResponse {

        val user = authentication.principal as OAuth2User
        val userId = user.getAttribute<String>("email") ?: "test@example.com"

        val agentRequest = AgentRequest(
            user_id = userId,
            request = request["request"] ?: throw IllegalArgumentException("request is required")
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(agentRequest, headers)

        return restTemplate.postForObject(
            "$agentUrl/agent/execute",
            entity,
            AgentResponse::class.java
        ) ?: throw RuntimeException("Agent execution failed")
    }

    /**
     * 사용 가능한 Agent 목록 조회
     */
    @GetMapping("/list")
    fun listAgents(): Map<String, Any> {
        return restTemplate.getForObject(
            "$agentUrl/agents",
            Map::class.java
        ) as Map<String, Any>
    }

    /**
     * 미리 정의된 시나리오 실행
     */
    @PostMapping("/scenarios/{scenario}")
    fun executeScenario(
        authentication: Authentication,
        @PathVariable scenario: String
    ): AgentResponse {

        val user = authentication.principal as OAuth2User
        val userId = user.getAttribute<String>("email") ?: "test@example.com"

        val request = when (scenario) {
            "monthly-analysis" -> "이번 달 소비 패턴을 분석해서 HTML 리포트를 만들고 이메일로 보내줘"
            "cafe-savings" -> "카페 지출이 너무 많은데, 줄일 수 있는 방법을 분석해줘"
            "budget-check" -> "예산 대비 소비 현황을 확인하고 남은 예산을 알려줘"
            "category-deep-dive" -> "가장 많이 소비한 카테고리를 분석하고 절약 팁을 제공해줘"
            else -> throw IllegalArgumentException("Unknown scenario: $scenario")
        }

        return executeAgent(authentication, mapOf("request" to request))
    }
}
