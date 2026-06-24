package com.xooxz.notification.infrastructure.redis

import com.xooxz.notification.domain.RateAlertCondition
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class RedisAlertConditionRepository : AlertConditionRepository {

    companion object {
        private val log =
            LoggerFactory.getLogger(
                RedisAlertConditionRepository::class.java)
    }

    override fun save(condition: RateAlertCondition) {
        log.info("TODO save")
    }

    override fun findBySymbol(symbol: String): List<RateAlertCondition> {
        return emptyList()
    }
}