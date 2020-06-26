package org.secuso.privacyfriendlybackup.api

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.TestWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlybackup.api.worker.CreateBackupWorker
import org.secuso.privacyfriendlybackup.api.worker.RestoreBackupWorker
import java.util.concurrent.Executors

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PfaWorkerTest {
    val packageName = "org.secuso.privacyfriendlybackup.api"
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val context = InstrumentationRegistry.getInstrumentation().context
    lateinit var workManager : WorkManager

    @BeforeClass
    fun setUp() {
        //val executor = Executors.newSingleThreadExecutor()
        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        BackupManager.backupRestorer = object : IBackupRestorer {
            override fun restoreBackup(context: Context, restoreData: String) : Boolean {
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

    @Test fun testCreateBackupWorker() {

        //val constraints: Constraints = TODO()
        //val worker = OneTimeWorkRequestBuilder<CreateBackupWorker>().build()
        //workManager.enqueue(worker)

        // driver = WorkManagerTestInitHelper.getTestDriver(context)
        // driver.setAllConstraintsMet(worker.id)
        //val workInfo = workManager.getWorkInfoById(worker.id).get()
        //assertEquals(WorkInfo.State.SUCCEEDED, workInfo.state)
        assertEquals(WorkInfo.State.SUCCEEDED, runWorker<CreateBackupWorker>().state)
    }

    @Test fun testRestoreBackupWorker() {
        assertEquals(WorkInfo.State.SUCCEEDED, runWorker<RestoreBackupWorker>().state)
    }

    inline fun <reified T : Worker> runWorker() : WorkInfo {
        val worker = OneTimeWorkRequestBuilder<T>().build()
        workManager.enqueue(worker)
        return workManager.getWorkInfoById(worker.id).get()
    }
}