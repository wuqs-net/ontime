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

    // TODO: new primary key using hashcode/md5

    init {
        activateDate?.setHms(0)
    }

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

    fun updateMissed() {
        when (repeatType) {
            NON_REPEAT -> isEnabled = false
            REPEAT_DAILY -> nextTime = getNextOccurrence()
        }
    }

    fun createAlarmStartIntent(context: Context): Intent {
        val bundle = Bundle().apply { putParcelable(ALARM_INSTANCE, this@Alarm) }
        return Intent(context, AlarmStateManager::class.java).apply {
            action = ACTION_ALARM_START
            putExtra(ALARM_ID, id)
            putExtra(ALARM_INSTANCE, bundle)
        }
    }

    @Ignore
    private constructor(source: Parcel) : this(
            id = source.readLong(),
            hour = source.readInt(),
            minute = source.readInt(),
            title = source.readString(),
            ringtoneUri = DTC.toUri(source.readString()),
            vibrate = DTC.toBoolean(source.readByte()),
            isEnabled = DTC.toBoolean(source.readByte()),
            repeatType = source.readInt(),
            repeatCycle = source.readInt(),
            repeatIndex = source.readInt(),
            activateDate = DTC.toCalendar(source.readLong()),
            nextTime = DTC.toCalendar(source.readLong().takeIf { it != 0L }),
            snoozed = source.readInt(),
            notes = source.readString()
    )

    @Ignore
    constructor(another: Alarm) : this(
            id = another.id,
            hour = another.hour,
            minute = another.minute,
            title = another.title,
            ringtoneUri = Uri.parse(another.ringtoneUri!!.toString()),
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
        writeString(DTC.toString(ringtoneUri ?: Uri.EMPTY))
        writeByte(DTC.toByte(vibrate))
        writeByte(DTC.toByte(isEnabled))
        writeInt(repeatType)
        writeInt(repeatCycle)
        writeInt(repeatIndex)
        writeLong(DTC.toLong(activateDate)!!)
        writeLong(DTC.toLong(nextTime) ?: 0L)
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
        const val ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE"
        const val IS_NEW_ALARM = "net.wuqs.ontime.extra.IS_NEW_ALARM"

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
        val DTC = DataTypeConverter()
    }
}
