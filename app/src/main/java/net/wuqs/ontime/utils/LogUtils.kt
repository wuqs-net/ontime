package net.wuqs.ontime.utils

import android.util.Log

object LogUtils {

    class Logger(val logTag: String) {

        fun v(message: String) = Log.v(logTag, message)
        fun d(message: String) = Log.d(logTag, message)
        fun i(message: String) = Log.i(logTag, message)
        fun w(message: String) = Log.w(logTag, message)
        fun e(message: String) = Log.e(logTag, message)
        fun e(message: String, tr: Throwable) = Log.e(logTag, message, tr)
    }

    val DEFAULT_LOGGER = Logger("OnTime")

    fun v(message: String) = DEFAULT_LOGGER.v(message)
    fun d(message: String) = DEFAULT_LOGGER.d(message)
    fun i(message: String) = DEFAULT_LOGGER.i(message)
    fun w(message: String) = DEFAULT_LOGGER.w(message)
    fun e(message: String) = DEFAULT_LOGGER.e(message)
    fun e(message: String, tr: Throwable) = DEFAULT_LOGGER.e(message, tr)
}