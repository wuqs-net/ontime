package net.wuqs.ontime.db

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import net.wuqs.ontime.R
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.shortToast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.ref.WeakReference

/**
 * Checks if external storage is available for read and write.
 */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

fun getExternalDbDir(): File {
    val file = File(Environment.getExternalStorageDirectory(), "OnTime")
    if (!file.mkdir()) logger.e("Directory not created")
    return file
}

class BackupDbTask(context: Context) : AsyncTask<Unit, Int, Boolean>() {

    private val contextRef = WeakReference(context.applicationContext)

    override fun doInBackground(vararg params: Unit?): Boolean? {
        try {
            val context = contextRef.get() ?: return false
            val currentDb = context.getDatabasePath(AppDatabase.ALARM_DATABASE_NAME)
            val backupDb = File(getExternalDbDir(), BACKUP_DB_NAME)

            val srcChannel = FileInputStream(currentDb).channel
            val dstChannel = FileOutputStream(backupDb).channel

            dstChannel.transferFrom(srcChannel, 0, srcChannel.size())
            srcChannel.close()
            dstChannel.close()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun onPostExecute(result: Boolean) {
        val context = contextRef.get() ?: return
        context.shortToast(
                if (result) R.string.msg_backup_successful else R.string.msg_backup_failed
        )
    }
}

class RestoreDbTask(context: Context) : AsyncTask<Unit, Int, Boolean>() {

    private val contextRef = WeakReference(context.applicationContext)

    override fun doInBackground(vararg params: Unit?): Boolean? {
        try {
            val context = contextRef.get() ?: return false
            AppDatabase.destroyInstance()
            val currentDb = context.getDatabasePath(AppDatabase.ALARM_DATABASE_NAME)
            val backupDb = File(getExternalDbDir(), BACKUP_DB_NAME)

            val srcChannel = FileInputStream(backupDb).channel
            val dstChannel = FileOutputStream(currentDb).channel

            dstChannel.transferFrom(srcChannel, 0, srcChannel.size())
            srcChannel.close()
            dstChannel.close()

        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun onPostExecute(result: Boolean) {
        val context = contextRef.get() ?: return

        if (result) {
            context.shortToast(R.string.msg_restore_successful)
        } else {
            context.shortToast(R.string.msg_restore_failed)
        }
    }
}

private const val BACKUP_DB_NAME = "alarms-backup.db"

private val logger = LogUtils.Logger("LocalDbBackupHelper")