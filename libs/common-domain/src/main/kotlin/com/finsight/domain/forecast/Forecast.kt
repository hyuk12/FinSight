package com.finsight.domain.forecast


data class ForecastRequest(
    val accountId: String,
    val horizonDays: Int = 7
)


data class ForecastPoint(
    val date: String,
    val value: Double
)


data class ForecastResponse(
    val accountId: String,
    val points: List<ForecastPoint>
)