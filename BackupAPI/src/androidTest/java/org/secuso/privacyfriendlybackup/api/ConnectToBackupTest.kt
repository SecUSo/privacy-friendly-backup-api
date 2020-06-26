package org.secuso.privacyfriendlybackup.api

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlybackup.api.worker.ConnectBackupWorker
import org.secuso.privacyfriendlybackup.api.worker.CreateBackupWorker
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class ConnectToBackupTest {
    val packageName = "org.secuso.privacyfriendlybackup.api"
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        val config = Configuration.Builder().setExecutor(SynchronousExecutor())
            .setMinimumLoggingLevel(Log.DEBUG).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(appContext, config)
        workManager = WorkManager.getInstance(appContext)

        BackupManager.backupRestorer = object : IBackupRestorer {
            override fun restoreBackup(context: Context, restoreData: String): Boolean {
                Thread.sleep(1000)
                return true
            }
        }

        BackupManager.backupCreator = object : IBackupCreator {
            override fun createBackup(context: Context): String {
                Thread.sleep(1000)
                return "{ 'test': [] }"
            }
        }
    }

    @Test
    fun connect() {
        startBackupProcess()
    }

    private fun startBackupProcess() {
        val backupWork = OneTimeWorkRequest.Builder(CreateBackupWorker::class.java)
            .addTag("org.secuso.privacyfriendlybackup.api.CreateBackupWork")
            //.setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

        val connectWork = OneTimeWorkRequest.Builder(ConnectBackupWorker::class.java)
            .addTag("org.secuso.privacyfriendlybackup.api.ConnectBackupWork")
            .build()

        workManager
            .beginUniqueWork("org.secuso.privacyfriendlybackup.api.ConnectBackupWork", ExistingWorkPolicy.KEEP, backupWork)
            .then(connectWork).enqueue()
    }
}