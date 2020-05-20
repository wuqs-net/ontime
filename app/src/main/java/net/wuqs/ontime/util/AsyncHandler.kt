package net.wuqs.ontime.util

import android.os.Handler
import android.os.HandlerThread

object AsyncHandler {
    private val handlerThread = HandlerThread("AsyncHandler")
    private val mHandler: Handler

    init {
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
    }

    fun post(r: () -> Unit) = mHandler.post(r)

    fun postDelayed(delayMillis: Long, r: () -> Unit) = mHandler.postDelayed(r, delayMillis)
}