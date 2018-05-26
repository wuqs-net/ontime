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
import net.wuqs.ontime.utils.LogUtils
import java.util.*

@Entity(tableName = "alarms")
class Alarm() : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = INVALID_ID
    @ColumnInfo(name = "hour")
    var hour: Int = 0
    @ColumnInfo(name = "minute")
    var minute: Int = 0
    @ColumnInfo(name = "title")
    var title: String? = ""
    @ColumnInfo(name = "ringtone_uri")
    var ringtoneUri: Uri? = null
    @ColumnInfo(name = "enabled")
    var isEnabled: Boolean = true
    @ColumnInfo(name = "repeat_type")
    var repeatType: Int = 0
    @ColumnInfo(name = "repeat_cycle")
    var repeatCycle: Int = 0
    @ColumnInfo(name = "repeat_index")
    var repeatIndex: Int = 0
    @ColumnInfo(name = "activate_date")
    var activateDate: Calendar? = Calendar.getInstance()
    @ColumnInfo(name = "next_occurrence")
    var nextTime: Calendar? = null

    init {
        activateDate!!.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * Updates the [Alarm]'s next occurrence.
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
                with(next) {
                    if (before(now)) {
                        val c = ((now.timeInMillis + now[Calendar.DST_OFFSET]) -
                                (timeInMillis + this[Calendar.DST_OFFSET])) /
                                86400000 / repeatCycle + 1
                        add(Calendar.DATE, c.toInt() * repeatCycle)
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
    private constructor(parcel: Parcel) : this() {
        this.id = parcel.readLong()
        this.hour = parcel.readInt()
        this.minute = parcel.readInt()
        this.title = parcel.readString()
        this.ringtoneUri = DTC.toUri(parcel.readString())
        this.isEnabled = DTC.toBoolean(parcel.readByte().toInt())
        this.repeatType = parcel.readInt()
        this.repeatCycle = parcel.readInt()
        this.repeatIndex = parcel.readInt()
        this.activateDate = DTC.toCalendar(parcel.readLong())
        this.nextTime = DTC.toCalendar(parcel.readLong().let { if (it != 0L) it else null })
    }

    @Ignore
    constructor(another: Alarm) : this() {
        copyFrom(another)
    }

    @Ignore
    constructor(hour: Int, minute: Int) : this() {
        this.hour = hour
        this.minute = minute
    }

    fun copyFrom(another: Alarm) {
        this.id = another.id
        this.hour = another.hour
        this.minute = another.minute
        this.title = another.title
        this.ringtoneUri = Uri.parse(another.ringtoneUri!!.toString())
        this.isEnabled = another.isEnabled
        this.repeatType = another.repeatType
        this.repeatCycle = another.repeatCycle
        this.repeatIndex = another.repeatIndex
        this.activateDate = another.activateDate!!.clone() as Calendar
        this.nextTime = another.nextTime?.clone() as Calendar?
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeInt(hour)
        dest.writeInt(minute)
        dest.writeString(title)
        dest.writeString(DTC.toString(ringtoneUri ?: Uri.EMPTY))
        dest.writeByte(DTC.toByte(isEnabled))
        dest.writeInt(repeatType)
        dest.writeInt(repeatCycle)
        dest.writeInt(repeatIndex)
        dest.writeLong(DTC.toLong(activateDate)!!)
        dest.writeLong(DTC.toLong(nextTime) ?: 0L)
    }

    override fun hashCode() = id.hashCode()

    override fun toString() = "{id=$id, $hour:$minute, hashCode=${hashCode()}, " +
            "title=$title, isEnabled=$isEnabled, " +
            "repeatType=$repeatType, repeatCycle=$repeatCycle, repeatIndex=$repeatIndex, " +
            "activate=${activateDate?.time}, next=${nextTime?.time}}"

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

        return true
    }

    companion object {

        const val ALARM_ID = "net.wuqs.ontime.extra.ALARM_ID"
        const val ALARM_INSTANCE = "net.wuqs.ontime.extra.ALARM_INSTANCE"
        const val IS_NEW_ALARM = "net.wuqs.ontime.extra.IS_NEW_ALARM"
        const val NEW_ALARM_HOUR = "net.wuqs.ontime.extra.NEW_ALARM_HOUR"
        const val NEW_ALARM_MINUTE = "net.wuqs.ontime.extra.NEW_ALARM_MINUTE"
        const val DELTA_NEXT_OCCURRENCE = "net.wuqs.ontime.extra.DELTA_NEXT_OCCURRENCE"

        /*
        Repeat types
        1st digit: display type
        2nd digit: calculate type
        3rd digit: flag for repeat yearly
         */
        const val NON_REPEAT = 0x0
        const val REPEAT_DAILY = 0x11
        const val REPEAT_WEEKLY = 0x22
        const val REPEAT_MONTHLY_BY_DAY_OF_MONTH = 0x43
        const val REPEAT_MONTHLY_BY_DAY_OF_WEEK = 0x83
        const val REPEAT_YEARLY_BY_DAY_OF_MONTH = 0x144
        const val REPEAT_YEARLY_BY_DAY_OF_WEEK = 0x184

        val REPEAT_TYPES = arrayOf(NON_REPEAT, REPEAT_DAILY, REPEAT_WEEKLY,
                REPEAT_MONTHLY_BY_DAY_OF_MONTH, REPEAT_YEARLY_BY_DAY_OF_MONTH)

        const val INVALID_ID = 0L

        const val INVALID_STATE = 0
        const val FIRED_STATE = 2

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

        @JvmField
        val CREATOR: Parcelable.Creator<Alarm> = object : Parcelable.Creator<Alarm> {
            override fun createFromParcel(source: Parcel): Alarm = Alarm(source)
            override fun newArray(size: Int): Array<Alarm?> = arrayOfNulls(size)
        }
        val DTC = DataTypeConverter()
    }
}
