package com.finsight.domain.analysis

data class Transaction(
    val id: String,
    val date: String,
    val amount: Int,
    val category: String,
    val merchant: String,
    val description: String,
)

data class AnalysisRequest(
    val userId: String,
    val userName: String,
    val transactions: List<Transaction>,
    val month: String,  // "2025-10"
)

data class CategorySummary(
    val category: String,
    val amount: Int,
    val percentage: Double,
)

data class AnalysisResult(
    val userId: String,
    val month: String,
    val nickname: String,  // 소비 성향 별명
    val topCategories: List<CategorySummary>,
    val insights: List<String>,
    val advice: List<String>,
    val totalAmount: Int,
    val generatedAt: String,
)