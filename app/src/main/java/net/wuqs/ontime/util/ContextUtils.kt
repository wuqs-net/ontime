package net.wuqs.ontime.util

import android.content.Context
import android.widget.Toast

fun Context.shortToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}