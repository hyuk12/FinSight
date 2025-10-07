package com.finsight.domain.account


data class Account(
    val id: String,
    val ownerName: String,
    val balance: Long,
)