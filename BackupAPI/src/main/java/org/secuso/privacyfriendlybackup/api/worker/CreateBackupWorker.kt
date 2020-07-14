package org.secuso.privacyfriendlybackup.api.worker

import android.content.Context
import android.util.Log
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
        Log.d("PFA BackupWorker", "doWork()")
        //if(BackupDataStore.isBackupDataSaved(context)) return Result.success()

        Log.d("PFA BackupWorker", "creating backup...")
        val backupData = BackupManager.backupCreator?.createBackup(context) ?: return Result.failure()
        Log.d("PFA BackupWorker", "backup created")

        BackupDataStore.saveBackupData(context, backupData)

        return Result.success()
    }

}