package net.wuqs.ontime.util

import android.os.Parcel

fun Parcel.writeBoolean(`val`: Boolean) {
    writeByte(if (`val`) 1 else 0)
}

fun Parcel.readBoolean(): Boolean {
    return readByte() == (1).toByte()
}