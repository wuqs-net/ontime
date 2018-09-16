package net.wuqs.ontime.db

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import net.wuqs.ontime.R
import net.wuqs.ontime.util.LogUtils
import net.wuqs.ontime.util.shortToast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Checks if external storage is available for read and write.
 */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

fun getExternalDbDir(): File {
    val file = File(Environment.getExternalStorageDirectory(), "OnTime")
    if (!file.mkdir() && !file.exists()) logger.e("Directory not created")
    return file
}

class BackupDbTask(context: Context) : AsyncTask<Unit, Int, Boolean>() {

    private val contextRef = WeakReference(context)

    override fun doInBackground(vararg params: Unit?): Boolean? {
        try {
            val context = contextRef.get() ?: return false

            val externalDir = getExternalDbDir()

            val currentDb = context.getDatabasePath(AppDatabase.ALARM_DATABASE_NAME)
            val currentDbDir = currentDb.parentFile
            val backupDb = File(externalDir, AppDatabase.ALARM_DATABASE_NAME)
            copy(currentDb, backupDb)

            val currentWal = File(currentDbDir, "${AppDatabase.ALARM_DATABASE_NAME}-wal")
            val backupWal = File(externalDir, "${AppDatabase.ALARM_DATABASE_NAME}-wal")
            copy(currentWal, backupWal)
        } catch (e: Exception) {
            e.printStackTrace()
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

    private val contextRef = WeakReference(context)

    override fun doInBackground(vararg params: Unit?): Boolean? {
        try {
            val context = contextRef.get() ?: return false
            val externalDir = getExternalDbDir()

            val currentDb = context.getDatabasePath(AppDatabase.ALARM_DATABASE_NAME)
            val currentDbDir = currentDb.parentFile
            val backupDb = File(externalDir, AppDatabase.ALARM_DATABASE_NAME)
            copy(backupDb, currentDb)

            val currentWal = File(currentDbDir, "${AppDatabase.ALARM_DATABASE_NAME}-wal")
            val backupWal = File(externalDir, "${AppDatabase.ALARM_DATABASE_NAME}-wal")
            copy(backupWal, currentWal)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun onPostExecute(result: Boolean) {
        val context = contextRef.get() ?: return

        if (result) {
            context.shortToast(R.string.msg_restore_successful)
            AppDatabase.destroyInstance()
            (context as? Activity)?.finishAffinity()
        } else {
            context.shortToast(R.string.msg_restore_failed)
        }
    }
}

@Throws(IOException::class)
private fun copy(from: File, to: File) {
    val src = FileInputStream(from).channel
    val dst = FileOutputStream(to).channel

    dst.transferFrom(src, 0, src.size())

    src.close()
    dst.close()
}

private const val BACKUP_DB_NAME = "alarms-backup.db"

private val logger = LogUtils.Logger("LocalDbBackupHelper")