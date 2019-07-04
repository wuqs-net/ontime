package net.wuqs.ontime.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import net.wuqs.ontime.R

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.hideSoftInput(view: View) {
    val imm = getSystemService<InputMethodManager>()
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.getIntArray(@ArrayRes id: Int) = resources.getIntArray(id)

fun Fragment.getStringArray(@ArrayRes id: Int) = resources.getStringArray(id)

fun Activity.changeTaskDescription(@StringRes labelId: Int = R.string.app_name) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val color = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        val taskDescription = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityManager.TaskDescription(getString(labelId), R.mipmap.ic_launcher, color)
        } else {
            @Suppress("DEPRECATION")
            ActivityManager.TaskDescription(
                    getString(labelId),
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                    color
            )
        }
        setTaskDescription(taskDescription)
    }
}
