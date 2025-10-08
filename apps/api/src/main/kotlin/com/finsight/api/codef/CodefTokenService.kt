package com.finsight.api.codef

import com.finsight.domain.codef.CodefToken
import com.finsight.infra.codef.CodefClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Service
class CodefTokenService(
    private val codefClient: CodefClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val lock = ReentrantReadWriteLock()

    @Volatile
    private var cachedToken: CodefToken? = null

    /**
     * 유효한 토큰 반환 (만료 시 자동 갱신)
     */
    fun getValidToken(): String {
        // Read lock으로 먼저 체크
        lock.read {
            val token = cachedToken
            if (token != null && !token.isExpired()) {
                return token.accessToken
            }
        }

        // Write lock으로 갱신
        lock.write {
            // Double-checked locking
            val token = cachedToken
            if (token != null && !token.isExpired()) {
                return token.accessToken
            }

            logger.info("CODEF token expired or not exists, issuing new token...")
            val newToken = codefClient.issueToken()
            cachedToken = newToken
            logger.info("CODEF token issued successfully, expires in ${newToken.expiresIn}s")

            return newToken.accessToken
        }
    }

    /**
     * 토큰 강제 갱신
     */
    fun refreshToken(): String {
        lock.write {
            logger.info("Force refreshing CODEF token...")
            val newToken = codefClient.issueToken()
            cachedToken = newToken
            logger.info("CODEF token refreshed successfully")
            return newToken.accessToken
        }
    }
}