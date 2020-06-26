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
class CreateBackupWorker(val context : Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        if(BackupDataStore.isBackupDataSaved(context)) return Result.success()

        val backupData = BackupManager.backupCreator?.createBackup(context) ?: return Result.failure()

        BackupDataStore.saveBackupData(context, backupData)

        return Result.success()
    }

}