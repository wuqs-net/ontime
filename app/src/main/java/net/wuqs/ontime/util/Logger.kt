package net.wuqs.ontime.util

import android.util.Log

class Logger(private val logTag: String) {

    fun v(message: String) = Log.v(logTag, message)
    fun d(message: String) = Log.d(logTag, message)
    fun i(message: String) = Log.i(logTag, message)
    fun w(message: String) = Log.w(logTag, message)
    fun e(message: String) = Log.e(logTag, message)
    fun e(message: String, tr: Throwable) = Log.e(logTag, message, tr)
    fun wtf(message: String) = Log.wtf(logTag, message)
    fun wtf(message: String, tr: Throwable) = Log.wtf(logTag, message, tr)
}