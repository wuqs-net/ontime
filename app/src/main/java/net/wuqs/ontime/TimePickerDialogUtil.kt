package net.wuqs.ontime

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat

fun createTimePickerDialog(context: Context,
                           listener: TimePickerDialog.OnTimeSetListener,
                           hourOfDay: Int, minute: Int): TimePickerDialog {
    return TimePickerDialog(context, listener, hourOfDay, minute,
            DateFormat.is24HourFormat(context))
}