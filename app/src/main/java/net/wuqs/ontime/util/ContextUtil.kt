package net.wuqs.ontime.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import net.wuqs.ontime.R

fun Context.shortToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

fun Context.hideSoftInput(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun Activity.getCustomTaskDescription(
        @StringRes labelId: Int = R.string.app_name
): ActivityManager.TaskDescription {
    val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
    val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(R.color.colorPrimaryDark)
    } else {
        @Suppress("DEPRECATION")
        resources.getColor(R.color.colorPrimaryDark)
    }
    return ActivityManager.TaskDescription(getString(labelId), bitmap, color)
}
