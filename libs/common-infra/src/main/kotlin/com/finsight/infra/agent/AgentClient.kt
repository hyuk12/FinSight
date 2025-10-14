// libs/common-infra/src/main/kotlin/com/finsight/infra/agent/AgentClient.kt
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

    fun analyze(request: AnalysisRequest): AnalysisResult {
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity(request, headers)

            println("🔍 POST $baseUrl/analyze")
            println("📤 Request: userId=${request.userId}, transactions=${request.transactions.size}")

            val response = restTemplate.postForEntity(
                "$baseUrl/analyze",
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

    fun analyzeWithLlm(request: AnalysisRequest): AnalysisResult {
        throw AgentApiException("LLM analysis not implemented yet")
    }
}

class AgentApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)