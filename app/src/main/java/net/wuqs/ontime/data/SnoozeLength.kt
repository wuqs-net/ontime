package net.wuqs.ontime.data

import java.util.*

class SnoozeLength {

    val amount: Int

    val unit: Int

    constructor(length: String) {

        if (!length.matches(Regex("[0-9]+[smhdw]?"))) {
            throw IllegalArgumentException("Invalid format")
        }

        if (length.matches(Regex("[0-9]+"))) {
            unit = Calendar.MINUTE
            amount = length.toInt()
        } else {
            unit = when (length.last()) {
                's' -> Calendar.SECOND
                'm' -> Calendar.MINUTE
                'h' -> Calendar.HOUR_OF_DAY
                'd' -> Calendar.DATE
                'w' -> Calendar.WEEK_OF_YEAR
                else -> Calendar.MINUTE
            }
            amount = length.dropLast(1).toInt()
        }
    }

    operator fun component1() = amount

    operator fun component2() = unit

}

fun Calendar.add(length: SnoozeLength) = add(length.unit, length.amount)