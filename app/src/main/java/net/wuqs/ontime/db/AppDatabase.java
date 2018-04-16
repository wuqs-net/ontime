package net.wuqs.ontime.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {Alarm.class}, version = 3)
@TypeConverters({DataTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract AlarmDAO alarmDAO();

    private static AppDatabase INSTANCE;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE alarms ADD COLUMN enabled INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE alarms ADD COLUMN repeat_type INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE alarms ADD COLUMN repeat_cycle INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE alarms ADD COLUMN repeat_index INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE alarms ADD COLUMN activate_date INTEGER");
            database.execSQL("ALTER TABLE alarms ADD COLUMN next_occurrence INTEGER");
        }
    };

    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            MIGRATION_1_2.migrate(database);
            MIGRATION_2_3.migrate(database);
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "alarm-database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
