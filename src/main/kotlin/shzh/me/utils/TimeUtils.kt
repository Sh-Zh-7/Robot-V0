package shzh.me.utils

import java.time.LocalDate
import java.util.Date

fun dateToLocalDate(date: Date): LocalDate {
    return java.sql.Date(date.time).toLocalDate()
}
