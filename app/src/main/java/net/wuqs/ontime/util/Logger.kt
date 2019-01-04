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

fun Any.logV(message: String) = Log.v(javaClass.simpleName, message)
fun Any.logD(message: String) = Log.d(javaClass.simpleName, message)
fun Any.logI(message: String) = Log.i(javaClass.simpleName, message)
fun Any.logW(message: String) = Log.w(javaClass.simpleName, message)
fun Any.logE(message: String) = Log.e(javaClass.simpleName, message)
fun Any.logE(message: String, tr: Throwable) = Log.e(javaClass.simpleName, message, tr)
fun Any.logWtf(message: String) = Log.wtf(javaClass.simpleName, message)
fun Any.logWtf(message: String, tr: Throwable) = Log.wtf(javaClass.simpleName, message, tr)