package net.wuqs.ontime.db

import android.arch.persistence.room.TypeConverter
import android.net.Uri

import java.util.Calendar

class DataTypeConverter {

    @TypeConverter
    fun toString(uri: Uri) = uri.toString()

    @TypeConverter
    fun toUri(string: String): Uri? = Uri.parse(string)

    fun toByte(b: Boolean) = toInt(b).toByte()

    @TypeConverter
    fun toInt(b: Boolean) = if (b) 1 else 0

    @TypeConverter
    fun toBoolean(n: Int) = n != 0

    @TypeConverter
    fun toLong(calendar: Calendar?): Long? = calendar?.timeInMillis

    @TypeConverter
    fun toCalendar(timeInMillis: Long?): Calendar? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis ?: return null
        return calendar
    }
}
