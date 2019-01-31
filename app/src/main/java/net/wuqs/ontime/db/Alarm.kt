package net.wuqs.ontime.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
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
    @ColumnInfo(name = Columns.HOUR) var hour: Int = 0,
    @ColumnInfo(name = Columns.MINUTE) var minute: Int = 0,
    @ColumnInfo(name = Columns.TITLE) var title: String? = "",
    @ColumnInfo(name = Columns.RINGTONE_URI) var ringtoneUri: Uri? = null,
    @ColumnInfo(name = Columns.VIBRATE) var vibrate: Boolean = false,
    @ColumnInfo(name = Columns.ENABLED) var isEnabled: Boolean = true,
    @ColumnInfo(name = Columns.REPEAT_TYPE) var repeatType: Int = 0,
    @ColumnInfo(name = Columns.REPEAT_CYCLE) var repeatCycle: Int = 0,
    @ColumnInfo(name = Columns.REPEAT_INDEX) var repeatIndex: Int = 0,
    @ColumnInfo(name = Columns.ACTIVATE_DATE) var activateDate: Calendar? = Calendar.getInstance(),
    @ColumnInfo(name = Columns.NEXT_OCCURRENCE) var nextTime: Calendar? = null,
    @ColumnInfo(name = Columns.SNOOZED) var snoozed: Int = 0,
    @ColumnInfo(name = Columns.NOTES) var notes: String = ""
) : Parcelable {

    object Columns {
        const val ID = "id"
        const val HOUR = "hour"
        const val MINUTE = "minute"
        const val TITLE = "title"
        const val RINGTONE_URI = "ringtone_uri"
        const val VIBRATE = "vibrate"
        const val ENABLED = "enabled"
        const val REPEAT_TYPE = "repeat_type"
        const val REPEAT_CYCLE = "repeat_cycle"
        const val REPEAT_INDEX = "repeat_index"
        const val ACTIVATE_DATE = "activate_date"
        const val NEXT_OCCURRENCE = "next_occurrence"
        const val SNOOZED = "snoozed"
        const val NOTES = "notes"
    }

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

        /**
         * Creates an [Alarm] with arguments from a [Bundle].
         */
        fun fromBundle(bundle: Bundle): Alarm {
            return Alarm(
                    id = bundle.getLong(Columns.ID),
                    hour = bundle.getInt(Columns.HOUR),
                    minute = bundle.getInt(Columns.MINUTE),
                    title = bundle.getString(Columns.TITLE),
                    ringtoneUri = bundle.getParcelable(Columns.RINGTONE_URI),
                    vibrate = bundle.getBoolean(Columns.VIBRATE),
                    isEnabled = bundle.getBoolean(Columns.ENABLED),
                    repeatType = bundle.getInt(Columns.REPEAT_TYPE),
                    repeatCycle = bundle.getInt(Columns.REPEAT_CYCLE),
                    repeatIndex = bundle.getInt(Columns.REPEAT_INDEX),
                    activateDate = bundle.getLong(Columns.ACTIVATE_DATE).toCalendar(),
                    nextTime = bundle.getLong(Columns.NEXT_OCCURRENCE)
                            .takeIf { it != -1L }
                            .toCalendar(),
                    snoozed = bundle.getInt(Columns.SNOOZED),
                    notes = bundle.getString(Columns.NOTES)!!
            )
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Alarm> = object : Parcelable.Creator<Alarm> {
            override fun createFromParcel(source: Parcel): Alarm = Alarm(source)
            override fun newArray(size: Int): Array<Alarm?> = arrayOfNulls(size)
        }
    }

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
            notes = source.readString()!!
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

    /**
     * Creates a [Bundle] containing information about this [Alarm].
     */
    fun toBundle(): Bundle {
        return Bundle().apply {
            putLong(Columns.ID, id)
            putInt(Columns.HOUR, hour)
            putInt(Columns.MINUTE, minute)
            putString(Columns.TITLE, title)
            putParcelable(Columns.RINGTONE_URI, ringtoneUri)
            putBoolean(Columns.VIBRATE, vibrate)
            putBoolean(Columns.ENABLED, isEnabled)
            putInt(Columns.REPEAT_TYPE, repeatType)
            putInt(Columns.REPEAT_CYCLE, repeatCycle)
            putInt(Columns.REPEAT_INDEX, repeatIndex)
            putLong(Columns.ACTIVATE_DATE, activateDate.toLong()!!)
            putLong(Columns.NEXT_OCCURRENCE, nextTime.toLong() ?: -1L)
            putInt(Columns.SNOOZED, snoozed)
            putString(Columns.NOTES, notes)
        }
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
}
