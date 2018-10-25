package net.wuqs.ontime.util

import android.content.Context
import android.os.PowerManager

object AlarmWakeLock {

    private const val TAG = "net.wuqs.ontime:AlarmWakeLock"

    private var cpuWakeLock: PowerManager.WakeLock? = null

    @JvmStatic
    fun createPartialWakeLock(context: Context): PowerManager.WakeLock {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
    }

    @JvmStatic
    fun acquireCpuWakeLock(context: Context) {
        if (cpuWakeLock != null) return

        cpuWakeLock = createPartialWakeLock(context)
        cpuWakeLock?.acquire()
    }

    @JvmStatic
    fun acquireScreenCpuWakeLock(context: Context) {
        if (cpuWakeLock != null) return

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                or PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG)
        cpuWakeLock?.acquire()
    }

    @JvmStatic
    fun releaseCpuLock() {
        cpuWakeLock?.release()
        cpuWakeLock = null
    }
}