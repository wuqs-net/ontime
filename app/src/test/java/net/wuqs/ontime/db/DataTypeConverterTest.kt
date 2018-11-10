package net.wuqs.ontime.db

import android.net.Uri
import org.junit.Test

class DataTypeConverterTest {

    @Test
    fun uriStringTest() {
        val uri: Uri? = null

        assert(uri.toString() == "null")
        assert(uri.toStringOrNull() == null)
    }
}