package org.secuso.privacyfriendlybackup.api.worker

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.work.*
import androidx.work.CoroutineWorker
import kotlinx.coroutines.delay
import org.secuso.privacyfriendlybackup.api.IBackupService
import org.secuso.privacyfriendlybackup.api.pfa.BackupDataStore
import org.secuso.privacyfriendlybackup.api.common.BackupApi.ACTION_SEND_MESSENGER
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSAGE_BACKUP
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSAGE_DONE
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSAGE_ERROR
import org.secuso.privacyfriendlybackup.api.common.BackupApi.MESSAGE_RESTORE
import org.secuso.privacyfriendlybackup.api.common.PfaApi.EXTRA_CONNECT_PACKAGE_NAME
import org.secuso.privacyfriendlybackup.api.common.PfaError
import org.secuso.privacyfriendlybackup.api.util.BackupApiConnection
import org.secuso.privacyfriendlybackup.api.util.readString
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

/**
 * @author Christopher Beckmann
 */
class ConnectBackupWorker(val context : Context, params: WorkerParameters) : CoroutineWorker(context, params), BackupApiConnection.IBackupApiListener {

    val TAG = "PFABackup"

    var mConnection : BackupApiConnection =
        BackupApiConnection(
            context,
            params.inputData.getString(EXTRA_CONNECT_PACKAGE_NAME)
                ?: "org.secuso.privacyfriendlybackup",
            this,
            Messenger(MessageHandler(this))
        )

    var workDone = false
    var errorOccurred = false

    internal class MessageHandler(worker : ConnectBackupWorker) : Handler(Looper.getMainLooper()) {
        val worker = WeakReference(worker)

        override fun handleMessage(msg: Message) {
            Executors.newSingleThreadExecutor().run {
                when (msg.what) {
                    MESSAGE_BACKUP -> {
                        Log.d(TAG, "\n\t[Message: MESSAGE_BACKUP($MESSAGE_BACKUP)]")
                        worker.get()?.handleBackup()
                    }
                    MESSAGE_RESTORE -> {
                        Log.d(TAG, "\n\t[Message: MESSAGE_RESTORE($MESSAGE_RESTORE)]")
                        worker.get()?.handleRestore()
                    }
                    MESSAGE_ERROR -> {
                        Log.d(TAG, "\n\t[Message: MESSAGE_ERROR($MESSAGE_ERROR)]")
                    }
                    MESSAGE_DONE -> {
                        Log.d(TAG, "\n\t[Message: MESSAGE_DONE($MESSAGE_DONE)]")
                        worker.get()?.workDone = true
                    }
                    else -> {
                        Log.d(TAG, "\n\t[Message: Unknown(${msg.what})]")
                        worker.get()?.errorOccurred = true
                        worker.get()?.workDone = true
                    }
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

        Log.d(TAG, "Received restore data: $restoreData")

        // save restore data
        BackupDataStore.saveRestoreData(context, restoreData!!)

        // enqueue restore worker
        val restoreBackupWorker = OneTimeWorkRequest.Builder(RestoreBackupWorker::class.java)
            .addTag("org.secuso.privacyfriendlybackup.api.RestoreBackupWork")
            .build()
        WorkManager.getInstance(context)
            .beginUniqueWork("org.secuso.privacyfriendlybackup.api.ConnectBackupWork", ExistingWorkPolicy.APPEND, restoreBackupWorker).enqueue()
    }

    override suspend fun doWork(): Result {
        mConnection.connect()

        var timeout = 60 * 5

        // wait for connection to finish
        do {
            delay(1000)
        } while(!workDone && --timeout > 0)

        if(mConnection.isBound()) {
            mConnection.disconnect()
        }

        if(errorOccurred || timeout <= 0) {
            return Result.failure()
        }

        // keep backup data as long restore is not done
        BackupDataStore.cleanBackupDataIfNoRestoreData(context)
        return Result.success(Data.EMPTY)
    }

    override fun onBound(service: IBackupService?) {
        Log.d(TAG, "onBound($service)")
        // do nothing
    }

    override fun onError(error: PfaError) {
        Log.d(TAG, error.toString())
        errorOccurred = true
        workDone = true
    }

    override fun onSuccess(action: String) {
        Log.d(TAG, "onSuccess($action)")
        when(action) {
            ACTION_SEND_MESSENGER -> { /* nice but do nothing */ }
            else -> {
                errorOccurred = true
                workDone = true
            }
        }
    }

    override fun onDisconnected() {
        Log.d(TAG, "onDisconnected()")
        if(!workDone) {
            errorOccurred = true
            workDone = true
        }
    }
}