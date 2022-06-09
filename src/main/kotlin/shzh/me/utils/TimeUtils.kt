package shzh.me.utils

import java.time.LocalDate
import java.util.*

object TimeUtils {
    fun dateToLocalDate(date: Date): LocalDate {
        return java.sql.Date(date.time).toLocalDate()
    }
}
