package com.finsight.api.config


import com.finsight.infra.codef.CodefClient
import com.finsight.infra.forecast.ForecastClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient


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
                ?: "https://development.codef.io" // sandbox
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
}