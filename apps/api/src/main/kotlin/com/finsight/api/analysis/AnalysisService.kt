package com.finsight.api.analysis

import com.finsight.domain.analysis.AnalysisRequest
import com.finsight.domain.analysis.AnalysisResult
import com.finsight.domain.analysis.Transaction
import com.finsight.infra.agent.AgentClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class AnalysisService(
    private val agentClient: AgentClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 더미 데이터로 분석 테스트
     */
    fun analyzeDummyData(userId: String, userName: String): AnalysisResult {
        val dummyTransactions = generateDummyTransactions()
        val month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

        val request = AnalysisRequest(
            userId = userId,
            userName = userName,
            transactions = dummyTransactions,
            month = month,
        )

        logger.info("Analyzing ${dummyTransactions.size} transactions for user $userId")
        return agentClient.analyze(request)
    }

    /**
     * 실제 거래내역으로 분석 (추후 구현)
     */
    fun analyzeUserTransactions(userId: String, yearMonth: String): AnalysisResult {
        // TODO: DB에서 거래내역 조회
        // val transactions = transactionRepository.findByUserIdAndMonth(userId, yearMonth)
        throw NotImplementedError("Real transaction analysis not implemented yet")
    }

    private fun generateDummyTransactions(): List<Transaction> {
        return listOf(
            Transaction("1", "2025-10-01", 4500, "카페", "스타벅스", "아메리카노"),
            Transaction("2", "2025-10-03", 45000, "식비", "올리브영", "생필품"),
            Transaction("3", "2025-10-05", 15000, "교통", "카카오T", "택시"),
            Transaction("4", "2025-10-07", 89000, "온라인쇼핑", "쿠팡", "전자기기"),
            Transaction("5", "2025-10-10", 5500, "카페", "이디야", "라떼"),
            Transaction("6", "2025-10-12", 120000, "구독", "Netflix", "프리미엄"),
            Transaction("7", "2025-10-15", 35000, "식비", "배달의민족", "치킨"),
            Transaction("8", "2025-10-18", 4800, "카페", "투썸플레이스", "아이스티"),
            Transaction("9", "2025-10-20", 250000, "온라인쇼핑", "Apple", "AirPods"),
            Transaction("10", "2025-10-25", 6000, "카페", "메가커피", "아메리카노"),
        )
    }
}