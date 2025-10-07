package com.finsight.api.config


import com.finsight.infra.forecast.ForecastClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient


@Configuration
class AppBeans {
    @Bean
    fun restClient(): RestClient = RestClient.builder()
    .baseUrl(System.getenv("ML_FORECAST_URL") ?: "http://ml-forecast:8000")
    .build()


    @Bean
    fun forecastClient(restClient: RestClient) = ForecastClient(restClient)
}