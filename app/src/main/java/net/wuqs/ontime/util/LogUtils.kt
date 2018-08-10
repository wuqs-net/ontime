package net.wuqs.ontime.util

import android.util.Log

object LogUtils {

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

    fun v(message: String) = DEFAULT_LOGGER.v(message)
    fun d(message: String) = DEFAULT_LOGGER.d(message)
    fun i(message: String) = DEFAULT_LOGGER.i(message)
    fun w(message: String) = DEFAULT_LOGGER.w(message)
    fun e(message: String) = DEFAULT_LOGGER.e(message)
    fun e(message: String, tr: Throwable) = DEFAULT_LOGGER.e(message, tr)
    fun wtf(message: String) = DEFAULT_LOGGER.wtf(message)
    fun wtf(message: String, tr: Throwable) = DEFAULT_LOGGER.wtf(message, tr)

    private val DEFAULT_LOGGER = Logger("OnTime")
}