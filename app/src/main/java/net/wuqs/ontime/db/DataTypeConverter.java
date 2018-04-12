package net.wuqs.ontime.db;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

public class DataTypeConverter {

    @TypeConverter
    public static String toString(Uri uri) {
        return uri.toString();
    }

    @TypeConverter
    public static Uri toUri(String string) {
        return Uri.parse(string);
    }
}
