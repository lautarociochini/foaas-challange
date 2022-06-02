package utils

import java.time.OffsetDateTime
import java.time.ZoneId

object Clock {

    fun utcNow(): OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC"))

    fun parse(dateTime: String): OffsetDateTime {
        return OffsetDateTime.parse(dateTime)
    }
}

