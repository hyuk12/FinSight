package com.finsight.infra.codef

import com.finsight.domain.codef.CodefApiResponse
import com.finsight.domain.codef.CodefToken
import com.finsight.domain.user.CodefConnectedIdResponse
import com.finsight.domain.user.CodefRegistrationRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.util.Base64

class CodefClient(
    private val restClient: RestClient,
    private val clientId: String,
    private val clientSecret: String,
) {

    fun issueToken(): CodefToken {
        val credentials = Base64.getEncoder()
            .encodeToString("$clientId:$clientSecret".toByteArray())

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("scope", "read")
        }

        return restClient.post()
            .uri("/v1/oauth/2.0/token")
            .header(HttpHeaders.AUTHORIZATION, "Basic $credentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(CodefToken::class.java)
            ?: throw CodefApiException("Failed to issue token")
    }
    
    fun createConnectedId(
        token: String,
        request: CodefRegistrationRequest,
    ): CodefConnectedIdResponse {
        val requestBody = buildMap {
            put("organization", request.organization)
            put("loginType", request.loginType)
            put("userName", request.userName)
            put("identity", request.identity)
            put("phoneNo", request.phoneNo)
            request.certFile?.let { put("certFile", it) }
            request.certPassword?.let { put("certPassword", it) }
        }

        val response = (restClient.post()
            .uri("/v1/account/create")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(CodefApiResponse::class.java)
            ?: throw CodefApiException("Failed to create connected ID"))

        if (!response.result.isSuccess()) {
            throw CodefApiException(
                "CODEF API error: ${response.result.code} - ${response.result.message}"
            )
        }

        @Suppress("UNCHECKED_CAST")
        val data = response.data as? Map<String, Any>
            ?: throw CodefApiException("Invalid response data")

        return CodefConnectedIdResponse(
            connectedId = data["connectedId"] as String,
            organization = request.organization,
        )
    }

    fun getAccounts(token: String, connectedId: String): List<Map<String, Any>> {
        val requestBody = mapOf(
            "connectedId" to connectedId,
            "organization" to "0004" // 예: 신한은행
        )

        val response = restClient.post()
            .uri("/v1/kr/bank/p/account/account-list")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(CodefApiResponse::class.java)
            ?: throw CodefApiException("Failed to get accounts")

        if (!response.result.isSuccess()) {
            throw CodefApiException(
                "CODEF API error: ${response.result.code} - ${response.result.message}"
            )
        }

        @Suppress("UNCHECKED_CAST")
        return (response.data as? List<Map<String, Any>>) ?: emptyList()
    }
}

class CodefApiException(msg: String) : RuntimeException(msg)