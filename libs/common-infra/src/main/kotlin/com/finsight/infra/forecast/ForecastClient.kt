package com.finsight.infra.forecast


import com.finsight.domain.forecast.*
import org.springframework.web.client.RestClient


/**
* Lightweight client using Spring 6 RestClient.
* Register as a @Bean in the API app with proper baseUrl.
*/
class ForecastClient(private val restClient: RestClient) {
    fun forecast(req: ForecastRequest): ForecastResponse {
        return restClient.post()
        .uri("/predict")
        .body(req)
        .retrieve()
        .body(ForecastResponse::class.java)!!
    }
}