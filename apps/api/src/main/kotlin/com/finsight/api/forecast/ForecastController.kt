package com.finsight.api.forecast

import com.finsight.domain.forecast.ForecastRequest
import com.finsight.domain.forecast.ForecastResponse
import com.finsight.infra.forecast.ForecastClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/forecast")
class ForecastController(private val client: ForecastClient) {
    @GetMapping
    fun getForecast(
        @RequestParam accountId: String,
        @RequestParam(defaultValue = "7") horizonDays: Int,
    ): ForecastResponse = client.forecast(ForecastRequest(accountId, horizonDays))
}