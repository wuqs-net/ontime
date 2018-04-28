package net.wuqs.ontime.db;

import android.annotation.SuppressLint;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.util.Calendar;

@Entity(tableName = "alarms")
public class Alarm implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "hour")
    private int hour;

    @ColumnInfo(name = "minute")
    private int minute;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "ringtone_uri")
    private Uri ringtoneUri;

    @ColumnInfo(name = "enabled")
    private boolean enabled = true;

    @ColumnInfo(name = "repeat_type")
    private int repeatType = 0;

    @ColumnInfo(name = "repeat_cycle")
    private int repeatCycle = 0;

    @ColumnInfo(name = "repeat_index")
    private int repeatIndex = 0;

    @ColumnInfo(name = "activate_date")
    private Calendar activateDate;

    @ColumnInfo(name = "next_occurrence")
    private Calendar nextOccurrence;

    /**
     * Checks if another {@link Alarm} has the same display summary as this {@link Alarm}.
     *
     * @param alarm the {@link Alarm} to check.
     * @return whether the two {@link Alarm}s have the same display summary.
     */
    public boolean sameDisplaySummaryAs(Alarm alarm) {
        return this.id == alarm.id
                && this.hour == alarm.hour
                && this.minute == alarm.minute
                && this.title.equals(alarm.title)
                && this.enabled == alarm.enabled
                && this.repeatType == alarm.repeatType
                && this.repeatCycle == alarm.repeatCycle
                && this.repeatIndex == alarm.repeatIndex
                && this.activateDate.getTimeInMillis() == alarm.activateDate.getTimeInMillis()
                && this.nextOccurrence.getTimeInMillis() == alarm.nextOccurrence.getTimeInMillis();
    }

    /**
     * Adds the {@link Alarm} to database.
     *
     * @param context the {@link Context} used to get the {@link AppDatabase} instance.
     * @return the {@link Alarm} added to database, with its id updated.
     */
    public Alarm addToDatabase(Context context) {
        this.id = AppDatabase.getInstance(context.getApplicationContext()).alarmDAO()
                .insert(this);
        return this;
    }

    public Alarm() {
        this.activateDate = Calendar.getInstance();
    }

    @Ignore
    public Alarm(int hour, int minute, Uri ringtoneUri) {
        this.hour = hour;
        this.minute = minute;
        this.ringtoneUri = ringtoneUri;
        this.activateDate = Calendar.getInstance();
    }

    @Ignore
    public Alarm(Parcel in) {
        this.id = in.readLong();
        this.hour = in.readInt();
        this.minute = in.readInt();
        this.title = in.readString();
        this.ringtoneUri = DataTypeConverter.toUri(in.readString());
        this.enabled = DataTypeConverter.toBoolean(in.readByte());
        this.repeatType = in.readInt();
        this.repeatCycle = in.readInt();
        this.repeatIndex = in.readInt();
        this.activateDate = DataTypeConverter.toCalendar(in.readLong());
        this.nextOccurrence = DataTypeConverter.toCalendar(in.readLong());
    }

    public Alarm(Alarm another) {
        copyFrom(another);
    }

    public void copyFrom(Alarm another) {
        this.id = another.id;
        this.hour = another.hour;
        this.minute = another.minute;
        this.title = another.title;
        this.ringtoneUri = Uri.parse(another.ringtoneUri.toString());
        this.enabled = another.enabled;
        this.repeatType = another.repeatType;
        this.repeatCycle = another.repeatCycle;
        this.repeatIndex = another.repeatIndex;
        this.activateDate = (Calendar) another.activateDate.clone();
        this.nextOccurrence = (Calendar) another.nextOccurrence.clone();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getRingtoneUri() {
        return ringtoneUri;
    }

    public void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatCycle() {
        return repeatCycle;
    }

    public void setRepeatCycle(int repeatCycle) {
        this.repeatCycle = repeatCycle;
    }

    public int getRepeatIndex() {
        return repeatIndex;
    }

    public void setRepeatIndex(int repeatIndex) {
        this.repeatIndex = repeatIndex;
    }

    public Calendar getActivateDate() {
        return activateDate;
    }

    public void setActivateDate(Calendar activateDate) {
        this.activateDate = activateDate;
    }

    public Calendar getNextOccurrence() {
        return nextOccurrence;
    }

    public void setNextOccurrence(Calendar nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
    }

    public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(hour);
        dest.writeInt(minute);
        dest.writeString(title);
        dest.writeString(DataTypeConverter.toString(ringtoneUri));
        dest.writeByte(DataTypeConverter.toByte(enabled));
        dest.writeInt(repeatType);
        dest.writeInt(repeatCycle);
        dest.writeInt(repeatIndex);
        dest.writeLong(DataTypeConverter.toLong(activateDate));
        dest.writeLong(DataTypeConverter.toLong(nextOccurrence));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("{id=%d, %02d:%02d, title=%s, enabled=%b, " +
                        "repeatType=%d, repeatCycle=%d, repeatIndex=%d, " +
                        "activateDate=%s, next=%s}",
                id, hour, minute, title, enabled,
                repeatType, repeatCycle, repeatIndex,
                activateDate.getTime().toString(), nextOccurrence.getTime().toString());
    }
}
