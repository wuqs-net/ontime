package net.wuqs.ontime.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.util.Calendar;

@Entity(tableName = "alarms")
public class Alarm implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

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

    /**
     * Repeat type of the alarm
     * <ul>
     * <li>0x00: non-repeat</li>
     * <li>0x11: every d days</li>
     * <li>0x22: dow every w weeks</li>
     * <li>0x43: dom every m months</li>
     * <li>0x83: a of bth week of every m months</li>
     * <li>0x144: date of every y years</li>
     * <li>0x184: a of bth week of mth month of every y years</li>
     * </ul>
     */
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
        dest.writeInt(id);
        dest.writeInt(hour);
        dest.writeInt(minute);
        dest.writeString(title);
        dest.writeString(ringtoneUri.toString());
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeInt(repeatType);
        dest.writeInt(repeatCycle);
        dest.writeInt(repeatIndex);
        dest.writeLong(activateDate.getTimeInMillis());
        dest.writeLong(nextOccurrence.getTimeInMillis());
    }

    public Alarm() {
        Calendar cal = Calendar.getInstance();
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
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
        this.id = in.readInt();
        this.hour = in.readInt();
        this.minute = in.readInt();
        this.title = in.readString();
        this.ringtoneUri = Uri.parse(in.readString());
        this.enabled = in.readByte() != 0;
        this.repeatType = in.readInt();
        this.repeatCycle = in.readInt();
        this.repeatIndex = in.readInt();
        this.activateDate = Calendar.getInstance();
        this.activateDate.setTimeInMillis(in.readLong());
        this.nextOccurrence = Calendar.getInstance();
        this.nextOccurrence.setTimeInMillis(in.readLong());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public String toString() {
        return String.format("{id=%d, %02d:%02d, title=%s, ringtone=%s, enabled=%b, repeatType=%d, repeatCycle=%d, repeatIndex=%d, activateDate=%s, nextOccurrence=%s}",
                id, hour, minute, title, ringtoneUri.toString(), enabled, repeatType, repeatCycle, repeatIndex, activateDate.getTime().toString(), nextOccurrence.getTime().toString());
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

    public boolean sameSummary(Alarm alarm) {
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
}
