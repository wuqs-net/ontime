package net.wuqs.ontime.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import net.wuqs.ontime.R

fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

fun Context.hideSoftInput(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.changeTaskDescription(@StringRes labelId: Int = R.string.app_name) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val color = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        setTaskDescription(ActivityManager.TaskDescription(getString(labelId), bitmap, color))
    }
}
