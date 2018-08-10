package net.wuqs.ontime.util

import android.os.Handler
import android.os.HandlerThread

object AsyncHandler {
    private val handlerThread = HandlerThread("AsyncHandler")
    private val handler: Handler

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    fun post(r: () -> Unit) = handler.post(r)
}