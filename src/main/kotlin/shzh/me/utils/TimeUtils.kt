package shzh.me.utils

import java.time.LocalDate
import java.time.YearMonth
import java.util.*

object TimeUtils {
    fun dateToLocalDate(date: Date): LocalDate {
        return java.sql.Date(date.time).toLocalDate()
    }

    private fun isLeapYear(year: Int): Boolean {
        val ym = YearMonth.of(year, 2)
        val days = ym.lengthOfMonth()

        return days == 29
    }

    fun getTotalDaysInYear(calendar: Calendar): Int {
        val year = calendar.get(Calendar.YEAR)
        return if (isLeapYear(year)) { 366 } else { 365 }
    }
}
