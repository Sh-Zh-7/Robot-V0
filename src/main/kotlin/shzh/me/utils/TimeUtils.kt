package shzh.me.utils

import java.time.YearMonth
import java.util.*

object TimeUtils {
    private fun isLeapYear(year: Int): Boolean {
        val ym = YearMonth.of(year, 2)
        val days = ym.lengthOfMonth()

        return days == 29
    }

    fun getTotalDaysInYear(calendar: Calendar): Int {
        val year = calendar.get(Calendar.YEAR)
        return if (isLeapYear(year)) {
            366
        } else {
            365
        }
    }
}
