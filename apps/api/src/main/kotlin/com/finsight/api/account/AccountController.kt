package com.finsight.api.account

import com.finsight.common.Logging
import com.finsight.domain.account.Account
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accounts")
class AccountController {
    @GetMapping("/sample")
    fun sample(): Account = Account(
        id = "acc_001",
        ownerName = "Haehyuk",
        balance = 125_000L,
    )

    @GetMapping("/health")
    fun health() = mapOf("status" to "OK", "msg" to Logging.banner("API alive"))
}