package com.finsight.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.finsight.infra.agent.AgentClient
import com.finsight.infra.codef.CodefClient
import com.finsight.infra.forecast.ForecastClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@Configuration
class AppBeans {
    @Bean
    fun forecastRestClient(): RestClient = RestClient.builder()
        .baseUrl(System.getenv("ML_FORECAST_URL") ?: "http://ml-forecast:8000")
        .build()

    @Bean
    fun forecastClient(forecastRestClient: RestClient) =
        ForecastClient(forecastRestClient)

    @Bean
    @ConditionalOnProperty(
        prefix = "codef",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun codefRestClient(): RestClient = RestClient.builder()
        .baseUrl(
            System.getenv("CODEF_API_URL")
                ?: "https://development.codef.io"
        )
        .build()

    @Bean
    @ConditionalOnProperty(
        prefix = "codef",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun codefClient(codefRestClient: RestClient): CodefClient {
        val clientId = System.getenv("CODEF_CLIENT_ID")
            ?: throw IllegalStateException("CODEF_CLIENT_ID not set")
        val clientSecret = System.getenv("CODEF_CLIENT_SECRET")
            ?: throw IllegalStateException("CODEF_CLIENT_SECRET not set")

        return CodefClient(codefRestClient, clientId, clientSecret)
    }

    // ===== Agent 클라이언트 (RestTemplate) =====

    @Bean
    fun agentObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
    }

    @Bean
    fun agentRestTemplate(agentObjectMapper: ObjectMapper): RestTemplate {
        val restTemplate = RestTemplate()

        // Jackson 컨버터 교체
        val converter = MappingJackson2HttpMessageConverter(agentObjectMapper)
        restTemplate.messageConverters.removeIf {
            it is MappingJackson2HttpMessageConverter
        }
        restTemplate.messageConverters.add(0, converter)

        return restTemplate
    }

    @Bean
    fun agentClient(agentRestTemplate: RestTemplate): AgentClient {
        return AgentClient(agentRestTemplate)
    }
}