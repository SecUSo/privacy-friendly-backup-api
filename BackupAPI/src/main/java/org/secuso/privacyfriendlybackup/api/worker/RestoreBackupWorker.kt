package org.secuso.privacyfriendlybackup.api.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.secuso.privacyfriendlybackup.api.pfa.BackupDataStore
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager

/**
 * @author Christopher Beckmann
 */
class RestoreBackupWorker(val context : Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val restoreData = BackupDataStore.getRestoreData(context) ?: return Result.failure()
        val backupRestorer = BackupManager.backupRestorer ?: return Result.failure()

        backupRestorer.restoreBackup(context, restoreData)

        BackupDataStore.cleanRestoreData(context)

        return Result.success(Data.EMPTY)
    }

}