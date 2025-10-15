package com.finsight.infra.agent

import com.finsight.domain.analysis.AnalysisRequest
import com.finsight.domain.analysis.AnalysisResult
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class AgentClient(
    private val restTemplate: RestTemplate,
    private val baseUrl: String = System.getenv("AGENT_API_URL") ?: "http://localhost:8000"
) {

    /**
     * 간단한 통계 기반 분석 (빠름)
     */
    fun analyze(request: AnalysisRequest): AnalysisResult {
        return callAnalysisApi("/analyze", request)
    }

    /**
     * LLM 기반 심층 분석 (느리지만 고품질)
     */
    fun analyzeWithLlm(request: AnalysisRequest): AnalysisResult {
        return callAnalysisApi("/analyze-with-llm", request)
    }

    private fun callAnalysisApi(endpoint: String, request: AnalysisRequest): AnalysisResult {
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity(request, headers)

            println("🔍 POST $baseUrl$endpoint")
            println("📤 Request: userId=${request.userId}, transactions=${request.transactions.size}")

            val response = restTemplate.postForEntity(
                "$baseUrl$endpoint",
                entity,
                AnalysisResult::class.java
            )

            println("✅ Response: ${response.statusCode} - ${response.body?.nickname}")

            return response.body ?: throw AgentApiException("Empty response")

        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            throw AgentApiException("Agent API failed: ${e.message}", e)
        }
    }
}

class AgentApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)