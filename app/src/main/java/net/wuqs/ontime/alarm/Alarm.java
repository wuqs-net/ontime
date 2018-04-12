package net.wuqs.ontime.alarm;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import net.wuqs.ontime.db.AlarmModel;

import java.util.Calendar;

public class Alarm implements Parcelable {

    public static final String ALARM_ID = "net.wuqs.ontime.extra.ALARM_ID";
    public static final String ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE";
    public static final String IS_NEW_ALARM = "net.wuqs.ontime.extra.IS_NEW_ALARM";
    public static final String NEW_ALARM_HOUR = "net.wuqs.ontime.extra.NEW_ALARM_HOUR";
    public static final String NEW_ALARM_MINUTE = "net.wuqs.ontime.extra.NEW_ALARM_MINUTE";
    public static final String NEW_CREATED_ALARM = "net.wuqs.ontime.extra.NEW_CREATED_ALARM";

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    private int id;
    private int hour;
    private int minute;
    private String title = "";
    private Uri ringtoneUri;


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
    }

    private Alarm(Parcel in) {
        this.id = in.readInt();
        this.hour = in.readInt();
        this.minute = in.readInt();
        this.title = in.readString();
        this.ringtoneUri = Uri.parse(in.readString());
    }

    public Alarm(AlarmModel model) {
        id = model.getId();
        hour = model.getHour();
        minute = model.getMinute();
        title = model.getTitle();
        ringtoneUri = model.getRingtoneUri();
    }

    public Alarm(int hour, int minute, Uri ringtoneUri) {
        this.hour = hour;
        this.minute = minute;
        this.ringtoneUri = ringtoneUri;
    }

    public Alarm() {
        Calendar cal = Calendar.getInstance();
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setHour(int hour) {
        if (hour >= 0 && hour < 24) this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    public void setMinute(int minute) {
        if (minute >= 0 && minute < 60) this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public Uri getRingtoneUri() {
        return ringtoneUri;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, %d:%d, title=%s, ringtone=%s}", id, hour, minute, title, ringtoneUri.toString());
    }
}
