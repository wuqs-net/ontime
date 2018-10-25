package net.wuqs.ontime.db

import android.arch.persistence.room.TypeConverter
import android.net.Uri
import java.util.*

fun String.toUri(): Uri? = Uri.parse(this)

fun Boolean.toInt() = if (this) 1 else 0

fun Int.toBoolean() = this != 0

fun Calendar?.toLong() = this?.timeInMillis

fun Long?.toCalendar() = this?.let { Calendar.getInstance().apply { timeInMillis = it } }

class DataTypeConverter {

    @TypeConverter
    fun toString(uri: Uri) = uri.toString()

    @TypeConverter
    fun toUri(string: String): Uri? = string.toUri()

    @TypeConverter
    fun toInt(b: Boolean) = b.toInt()

    @TypeConverter
    fun toBoolean(n: Int) = n.toBoolean()

    @TypeConverter
    fun toLong(calendar: Calendar?) = calendar.toLong()

    @TypeConverter
    fun toCalendar(timeInMillis: Long?) = timeInMillis.toCalendar()
}
