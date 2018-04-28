package net.wuqs.ontime.db;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

import java.util.Calendar;

public class DataTypeConverter {

    @TypeConverter
    public static String toString(Uri uri) {
        return uri.toString();
    }

    @TypeConverter
    public static Uri toUri(String string) {
        return Uri.parse(string);
    }

    public static byte toByte(boolean b) {
        return (byte) toInt(b);
    }

    @TypeConverter
    public static int toInt(boolean b) {
        return b ? 1 : 0;
    }

    @TypeConverter
    public static boolean toBoolean(int n) {
        return n != 0;
    }

    @TypeConverter
    public static Long toLong(Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    @TypeConverter
    public static Calendar toCalendar(Long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        if (timeInMillis == null) timeInMillis = 0L;
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }
}
