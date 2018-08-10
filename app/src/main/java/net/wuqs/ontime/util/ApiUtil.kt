package net.wuqs.ontime.util

import android.os.Build

object ApiUtil {
    fun isPreL() = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
    fun isLOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    fun isLMR1OrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
    fun isMOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    fun isNOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    fun isNMR1OrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    fun isOOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    fun isOMR1OrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
}
