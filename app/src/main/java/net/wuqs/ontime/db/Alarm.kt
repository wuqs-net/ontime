package net.wuqs.ontime.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import net.wuqs.ontime.alarm.*
import net.wuqs.ontime.feature.editalarm.binString
import net.wuqs.ontime.feature.editalarm.hexString
import java.util.*

@Entity(tableName = "alarms")
class Alarm(
        @PrimaryKey var id: Long = INVALID_ID,
        @ColumnInfo(name = "hour") var hour: Int = 0,
        @ColumnInfo(name = "minute") var minute: Int = 0,
        @ColumnInfo(name = "title") var title: String? = "",
        @ColumnInfo(name = "ringtone_uri") var ringtoneUri: Uri? = null,
        @ColumnInfo(name = "vibrate") var vibrate: Boolean = false,
        @ColumnInfo(name = "enabled") var isEnabled: Boolean = true,
        @ColumnInfo(name = "repeat_type") var repeatType: Int = 0,
        @ColumnInfo(name = "repeat_cycle") var repeatCycle: Int = 0,
        @ColumnInfo(name = "repeat_index") var repeatIndex: Int = 0,
        @ColumnInfo(name = "activate_date") var activateDate: Calendar? = Calendar.getInstance(),
        @ColumnInfo(name = "next_occurrence") var nextTime: Calendar? = null,
        @ColumnInfo(name = "snoozed") var snoozed: Int = 0,
        @ColumnInfo(name = "notes") var notes: String = ""
) : Parcelable {

    init {
        activateDate?.setHms(0)
    }

    // TODO: Move to AlarmUtil.kt
    /**
     * Determines the [Alarm]'s next occurrence.
     */
    fun getNextOccurrence(now: Calendar = Calendar.getInstance()): Calendar? = when (repeatType) {
        NON_REPEAT -> nextTimeNonRepeat(now)
        REPEAT_DAILY -> nextTimeDaily(now)
        REPEAT_WEEKLY -> nextTimeWeekly(now)
        REPEAT_MONTHLY_BY_DATE -> nextTimeMonthlyByDate(now)
        REPEAT_YEARLY_BY_DATE -> nextTimeYearlyByDate(now)
        else -> null
    }

    // TODO: Move to AlarmUtil.kt
    fun createAlarmStartIntent(context: Context): Intent {
        val bundle = Bundle().apply { putParcelable(EXTRA_ALARM_INSTANCE, this@Alarm) }
        return Intent(context, AlarmService::class.java).apply {
            action = ACTION_ALARM_START
            addCategory("ALARM_MANAGER")
            putExtra(EXTRA_ALARM_INSTANCE, bundle)
        }
    }

    @Ignore
    private constructor(source: Parcel) : this(
            id = source.readLong(),
            hour = source.readInt(),
            minute = source.readInt(),
            title = source.readString(),
            ringtoneUri = source.readParcelable(Uri::class.java.classLoader),
            vibrate = source.readInt().toBoolean(),
            isEnabled = source.readInt().toBoolean(),
            repeatType = source.readInt(),
            repeatCycle = source.readInt(),
            repeatIndex = source.readInt(),
            activateDate = source.readLong().toCalendar(),
            nextTime = source.readLong().takeIf { it != -1L }.toCalendar(),
            snoozed = source.readInt(),
            notes = source.readString()
    )

    @Ignore
    constructor(another: Alarm) : this(
            id = another.id,
            hour = another.hour,
            minute = another.minute,
            title = another.title,
            ringtoneUri = another.ringtoneUri,
            vibrate = another.vibrate,
            isEnabled = another.isEnabled,
            repeatType = another.repeatType,
            repeatCycle = another.repeatCycle,
            repeatIndex = another.repeatIndex,
            activateDate = another.activateDate!!.clone() as Calendar,
            nextTime = another.nextTime?.clone() as Calendar?,
            snoozed = another.snoozed,
            notes = another.notes
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.run {
        writeLong(id)
        writeInt(hour)
        writeInt(minute)
        writeString(title)
        writeParcelable(ringtoneUri, 0)
        writeInt(vibrate.toInt())
        writeInt(isEnabled.toInt())
        writeInt(repeatType)
        writeInt(repeatCycle)
        writeInt(repeatIndex)
        writeLong(activateDate.toLong()!!)
        writeLong(nextTime.toLong() ?: -1L)
        writeInt(snoozed)
        writeString(notes)
    }

    override fun toString(): String {
        return "{id=$id, $hour:$minute, " +
                "title=$title, isEnabled=$isEnabled, " +
                "ringtone=$ringtoneUri, vibrate=$vibrate, " +
                "repeatType=${repeatType.hexString}, repeatCycle=$repeatCycle, " +
                "repeatIndex=${repeatIndex.binString}, " +
                "activate=${activateDate?.time}, next=${nextTime?.time}, " +
                "snoozed=$snoozed, notes=$notes}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alarm

        if (id != other.id) return false
        if (hour != other.hour) return false
        if (minute != other.minute) return false
        if (title != other.title) return false
        if (ringtoneUri != other.ringtoneUri) return false
        if (vibrate != other.vibrate) return false
        if (isEnabled != other.isEnabled) return false
        if (repeatType != other.repeatType) return false
        if (repeatCycle != other.repeatCycle) return false
        if (repeatIndex != other.repeatIndex) return false
        if (activateDate != other.activateDate) return false
        if (nextTime != other.nextTime) return false
        if (snoozed != other.snoozed) return false
        if (notes != other.notes) return false

        return true
    }

    override fun hashCode() = id.hashCode()

    companion object {

        const val ALARM_ID = "net.wuqs.ontime.extra.ALARM_ID"

        /*
        Repeat types
        1st digit: display type
        2nd digit: calculate type
        3rd digit: flag for repeat yearly
         */
        const val NON_REPEAT = 0x0
        const val REPEAT_DAILY = 0x11
        const val REPEAT_WEEKLY = 0x22
        const val REPEAT_MONTHLY_BY_DATE = 0x43
        const val REPEAT_MONTHLY_BY_WEEK = 0x83
        const val REPEAT_YEARLY_BY_DATE = 0x144
        const val REPEAT_YEARLY_BY_WEEK = 0x184

        const val INVALID_ID = 0L

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Alarm> = object : Parcelable.Creator<Alarm> {
            override fun createFromParcel(source: Parcel): Alarm = Alarm(source)
            override fun newArray(size: Int): Array<Alarm?> = arrayOfNulls(size)
        }
    }
}
