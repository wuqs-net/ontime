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
import net.wuqs.ontime.alarm.AlarmStateManager
import net.wuqs.ontime.ui.alarmeditscreen.binString
import net.wuqs.ontime.ui.alarmeditscreen.hexString
import net.wuqs.ontime.util.LogUtils
import java.util.*

@Entity(tableName = "alarms")
class Alarm(
        @PrimaryKey(autoGenerate = true) var id: Long = INVALID_ID,
        @ColumnInfo(name = "hour") var hour: Int = 0,
        @ColumnInfo(name = "minute") var minute: Int = 0,
        @ColumnInfo(name = "title") var title: String? = "",
        @ColumnInfo(name = "ringtone_uri") var ringtoneUri: Uri? = null,
        @ColumnInfo(name = "enabled") var isEnabled: Boolean = true,
        @ColumnInfo(name = "repeat_type") var repeatType: Int = 0,
        @ColumnInfo(name = "repeat_cycle") var repeatCycle: Int = 0,
        @ColumnInfo(name = "repeat_index") var repeatIndex: Int = 0,
        @ColumnInfo(name = "activate_date") var activateDate: Calendar? = Calendar.getInstance(),
        @ColumnInfo(name = "next_occurrence") var nextTime: Calendar? = null,
        @ColumnInfo(name = "snoozed") var snoozed: Int = 0
) : Parcelable {

    init {
        activateDate!!.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * Determines the [Alarm]'s next occurrence.
     */
    fun getNextOccurrence(now: Calendar = Calendar.getInstance()): Calendar? {

        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (repeatType) {
            NON_REPEAT -> {
                with(activateDate!!) {
                    next.set(this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DATE])
                }
                if (next.before(now)) return null
//                with(nextTime!!) {
//                    if (before(now)) {
//                        set(now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DATE])
//                        if (before(now)) add(Calendar.DATE, 1)
//                    }
//                }
            }
            REPEAT_DAILY -> {
                with(activateDate!!) {
                    next.set(this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DATE])
                }
                if (next.before(now)) {
                    val c = ((now.timeInMillis + now[Calendar.DST_OFFSET]) -
                            (next.timeInMillis + next[Calendar.DST_OFFSET])) /
                            86400000 / repeatCycle + 1
                    next.add(Calendar.DATE, c.toInt() * repeatCycle)
                }
            }
            REPEAT_MONTHLY_BY_DATE -> {
                if (repeatIndex == 0) return null
                with(activateDate!!) {
                    next.set(this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DATE])
                    if (next.before(now)) {
                        next[Calendar.DATE] = now[Calendar.DATE]
                        val c = ((now[Calendar.YEAR] * 12 + now[Calendar.MONTH]) -
                                (this[Calendar.YEAR] * 12 + this[Calendar.MONTH])) /
                                repeatCycle
                        next.add(Calendar.MONTH, c * repeatCycle)
                    }
                }
                for (i in next[Calendar.DATE]..31) {
                    if (repeatIndex shr (i - 1) and 1 == 1) {
                        next[Calendar.DATE] = i
                        if (next.after(now)) {
                            if (next[Calendar.DATE] == i) {
                                return next
                            } else {
                                next.add(Calendar.MONTH, -1)
                                break
                            }
                        }
                    }
                }
                next.apply {
                    for (i in 1..31) {
                        if (repeatIndex shr (i - 1) and 1 == 1) {
                            do {
                                add(Calendar.MONTH, repeatCycle)
                            } while (i > getActualMaximum(Calendar.DATE))
                            set(Calendar.DATE, i)
                            if (get(Calendar.DATE) != i) {
                                add(Calendar.MONTH, -1)
                                set(Calendar.DATE, getActualMaximum(Calendar.DATE))
                            }
                            break
                        }
                    }
                }
            }
        }
        return next
    }

    fun updateMissed() {
        when (repeatType) {
            NON_REPEAT -> isEnabled = false
            REPEAT_DAILY -> nextTime = getNextOccurrence()
        }
    }

    /**
     * Checks if another [Alarm] has the same display summary as this [Alarm].
     *
     * @param other the other [Alarm] to check.
     * @return whether the two [Alarm]s have the same display summary.
     */
    fun sameDisplaySummaryAs(other: Alarm?) = when {
        other == null -> false
        id != other.id -> false
        hour != other.hour || minute != minute -> false
        title != other.title -> false
        isEnabled != other.isEnabled -> false
        activateDate != other.activateDate -> false
        nextTime != other.nextTime -> false
        repeatType != other.repeatType -> false
        repeatCycle != other.repeatCycle -> false
        repeatIndex != other.repeatIndex -> false
        snoozed != other.snoozed -> false
        else -> true
    }

    fun createIntent(context: Context, cls: Class<*>): Intent {
        return Intent(context, cls)
    }

    fun createAlarmStartIntent(context: Context): Intent {
        val bundle = Bundle().apply { putParcelable(ALARM_INSTANCE, this@Alarm) }
        return Intent(context, AlarmStateManager::class.java).apply {
            action = AlarmStateManager.ACTION_ALARM_START
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
            isEnabled = DTC.toBoolean(source.readByte().toInt()),
            repeatType = source.readInt(),
            repeatCycle = source.readInt(),
            repeatIndex = source.readInt(),
            activateDate = DTC.toCalendar(source.readLong()),
            nextTime = DTC.toCalendar(source.readLong().takeIf { it != 0L }),
            snoozed = source.readInt()
    )

    @Ignore
    constructor(another: Alarm) : this(
            id = another.id,
            hour = another.hour,
            minute = another.minute,
            title = another.title,
            ringtoneUri = Uri.parse(another.ringtoneUri!!.toString()),
            isEnabled = another.isEnabled,
            repeatType = another.repeatType,
            repeatCycle = another.repeatCycle,
            repeatIndex = another.repeatIndex,
            activateDate = another.activateDate!!.clone() as Calendar,
            nextTime = another.nextTime?.clone() as Calendar?,
            snoozed = another.snoozed
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeLong(id)
            writeInt(hour)
            writeInt(minute)
            writeString(title)
            writeString(DTC.toString(ringtoneUri ?: Uri.EMPTY))
            writeByte(DTC.toByte(isEnabled))
            writeInt(repeatType)
            writeInt(repeatCycle)
            writeInt(repeatIndex)
            writeLong(DTC.toLong(activateDate)!!)
            writeLong(DTC.toLong(nextTime) ?: 0L)
            writeInt(snoozed)
        }
    }

    override fun toString() = "{id=$id, $hour:$minute, hashCode=${hashCode()}, " +
            "title=$title, isEnabled=$isEnabled, " +
            "repeatType=${repeatType.hexString}, repeatCycle=$repeatCycle, " +
            "repeatIndex=${repeatIndex.binString}, " +
            "activate=${activateDate?.time}, next=${nextTime?.time}, " +
            "snoozed=$snoozed}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alarm

        if (id != other.id) return false
        if (hour != other.hour) return false
        if (minute != other.minute) return false
        if (title != other.title) return false
        if (ringtoneUri != other.ringtoneUri) return false
        if (isEnabled != other.isEnabled) return false
        if (repeatType != other.repeatType) return false
        if (repeatCycle != other.repeatCycle) return false
        if (repeatIndex != other.repeatIndex) return false
        if (activateDate != other.activateDate) return false
        if (nextTime != other.nextTime) return false
        if (snoozed != other.snoozed) return false

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

        /**
         * Adds the [Alarm] to database.
         *
         * @param db the [AppDatabase] instance.
         * @return the [Alarm] saved to database, with its id updated.
         */
        fun addAlarm(db: AppDatabase, alarm: Alarm): Alarm {
            alarm.id = db.alarmDAO.insert(alarm)
            LogUtils.i("Alarm added to database: $alarm")
            return alarm
        }

        /**
         * Updates the [Alarm] in database.
         *
         * @param db the [AppDatabase] instance.
         * @return the [Alarm] saved to database, with its id updated.
         */
        fun updateAlarm(db: AppDatabase, alarm: Alarm): Int {
            val count = db.alarmDAO.update(alarm)
            LogUtils.i("Alarm updated in database: $alarm")
            return count
        }

        /**
         * Deletes the [Alarm] from database.
         *
         * @param db the [AppDatabase] instance.
         */
        fun deleteAlarm(db: AppDatabase, alarm: Alarm) {
            db.alarmDAO.delete(alarm)
            LogUtils.i("Alarm deleted from database: $alarm")
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Alarm> = object : Parcelable.Creator<Alarm> {
            override fun createFromParcel(source: Parcel): Alarm = Alarm(source)
            override fun newArray(size: Int): Array<Alarm?> = arrayOfNulls(size)
        }
        val DTC = DataTypeConverter()
    }
}
