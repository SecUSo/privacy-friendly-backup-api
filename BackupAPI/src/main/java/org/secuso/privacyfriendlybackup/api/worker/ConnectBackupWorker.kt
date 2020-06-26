package org.secuso.privacyfriendlybackup.api.worker

import android.content.Context
import android.os.Handler
import android.os.Message
import android.os.Messenger
import androidx.work.*
import org.secuso.privacyfriendlybackup.api.IBackupService
import org.secuso.privacyfriendlybackup.api.pfa.BackupDataStore
import org.secuso.privacyfriendlybackup.api.common.BackupApi.ACTION_SEND_MESSENGER
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSENGER_BACKUP
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSENGER_DONE
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSENGER_RESTORE
import org.secuso.privacyfriendlybackup.api.common.PfaApi.EXTRA_CONNECT_PACKAGE_NAME
import org.secuso.privacyfriendlybackup.api.common.PfaError
import org.secuso.privacyfriendlybackup.api.pfa.BackupApiConnection
import org.secuso.privacyfriendlybackup.api.util.readString
import java.lang.ref.WeakReference

/**
 * @author Christopher Beckmann
 */
class ConnectBackupWorker(val context : Context, params: WorkerParameters) : Worker(context, params), BackupApiConnection.IBackupApiListener {

    var mConnection : BackupApiConnection =
        BackupApiConnection(
            context,
            params.inputData.getString(EXTRA_CONNECT_PACKAGE_NAME) ?: "org.secuso.privacyfriendlybackup",
            this,
            Messenger(MessageHandler(this))
        )

    var workDone = false
    var errorOccurred = false

    internal class MessageHandler(worker : ConnectBackupWorker) : Handler() {
        val worker = WeakReference(worker)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSENGER_BACKUP -> worker.get()?.handleBackup()
                MESSENGER_RESTORE -> worker.get()?.handleRestore()
                MESSENGER_DONE -> {
                    worker.get()?.workDone = true
                }
                else -> {
                    worker.get()?.errorOccurred = true
                    worker.get()?.workDone = true
                }
            }
        }
    }

    fun handleBackup() {
        val backupData = BackupDataStore.getBackupData(context)

        // no backup data available
        if(backupData == null) {
            errorOccurred = true
            workDone = true
            return
        }

        val outputStream = mConnection.initBackup()

        outputStream?.use { stream ->
            stream.write(backupData.toByteArray(Charsets.UTF_8))
        }

        mConnection.sendBackupData()
    }

    fun handleRestore() {
        val stream = mConnection.getRestoreData()

        var restoreData : String? = null

        stream?.use {
            restoreData = it.readString()
        }

        // something went wrong
        if(restoreData == null) {
            errorOccurred = true
            workDone = true
            return
        }

        // save restore data
        BackupDataStore.saveRestoreData(context, restoreData!!)

        // enqueue restore worker
        val restoreBackupWorker = OneTimeWorkRequest.Builder(RestoreBackupWorker::class.java)
            .addTag("org.secuso.privacyfriendlybackup.api.RestoreBackupWork")
            .build()
        WorkManager.getInstance(context)
            .beginUniqueWork("org.secuso.privacyfriendlybackup.api.ConnectBackupWork", ExistingWorkPolicy.APPEND, restoreBackupWorker).enqueue()
    }

    override fun doWork(): Result {
        mConnection.connect()

        try {
            do {
                Thread.sleep(1000)
            } while(!workDone)
        } catch (e : InterruptedException) {
            errorOccurred = true
        } finally {
            if(mConnection.isBound()) {
                mConnection.disconnect()
            }
        }

        if(errorOccurred) {
            return Result.failure()
        }

        BackupDataStore.cleanBackupData(context)
        return Result.success(Data.EMPTY)
    }

    override fun onBound(service: IBackupService?) {
        // do nothing
    }

    override fun onError(error: PfaError) {
        errorOccurred = true
        workDone = true
    }

    override fun onSuccess(action: String) {
        when(action) {
            ACTION_SEND_MESSENGER -> { /* nice but do nothing */ }
            else -> {
                errorOccurred = true
                workDone = true
            }
        }
    }

    override fun onDisconnected() {
        if(!workDone) {
            errorOccurred = true
            workDone = true
        }
    }
}