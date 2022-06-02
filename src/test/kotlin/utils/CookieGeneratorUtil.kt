package utils

import java.lang.StringBuilder
import java.time.OffsetDateTime

object CookieGeneratorUtil {

    private const val DATE_SEP = "%2D"
    private const val TIME_SEP = "%253A"
    private const val TOKEN_END = "%2E000000Z"
    private const val INT_EQUAL_SEP = "%3D%2523i"
    private const val STRING_EQUAL_SEP = "%3D%2523s"
    private const val FIELD_SEP = "%26"

    private const val COUNT_PARAM = "count"
    private const val NAME_PARAM = "name"
    private const val TOKEN_PARAM = "token"
    private const val USER_SESSION = "user_session"

    fun generateCookie(count: Int, name: String, token: String): String {
        val builder = StringBuilder()
        builder.append(USER_SESSION)
            .append("=")
            .append(getCountAsParam(count))
            .append(FIELD_SEP)
            .append(getNameAsParam(name))
            .append(FIELD_SEP)
            .append(token)

        return builder.toString()
    }

    fun getTokenAsParam(date: OffsetDateTime? = null): String {
        val dateTime = date ?: Clock.utcNow()
        with(dateTime) {
            val year = year
            val month = safeParse(monthValue)
            val day = safeParse(dayOfMonth)
            val hour = safeParse(hour)
            val minute = safeParse(minute)
            val second = safeParse(second)
            val builder = StringBuilder()
            builder
                .append(TOKEN_PARAM)
                .append(STRING_EQUAL_SEP)
                .append(getDate(year, month, day))
                .append("T")
                .append(getTime(hour, minute, second))
                .append(TOKEN_END)

            return builder.toString()
        }
    }

    private fun getCountAsParam(count: Int) =
        StringBuilder()
            .append(COUNT_PARAM)
            .append(INT_EQUAL_SEP)
            .append(count)
            .toString()

    private fun getNameAsParam(name: String) =
        StringBuilder()
            .append(NAME_PARAM)
            .append(STRING_EQUAL_SEP)
            .append(name)
            .toString()


    private fun getDate(year: Int, month: String, day: String) =
        StringBuilder()
            .append(year)
            .append(DATE_SEP)
            .append(month)
            .append(DATE_SEP)
            .append(day)
            .toString()

    private fun getTime(hour: String, minute: String, second: String) =
        StringBuilder()
            .append(hour)
            .append(TIME_SEP)
            .append(minute)
            .append(TIME_SEP)
            .append(second)
            .toString()

    private fun safeParse(number: Int): String {
        return if (number < 10) "0$number" else "$number"
    }

}