package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.secuso.privacyfriendlybackup.api.IPFAService
import org.secuso.privacyfriendlybackup.api.common.AbstractAuthService
import org.secuso.privacyfriendlybackup.api.common.CommonApiConstants.RESULT_CODE
import org.secuso.privacyfriendlybackup.api.common.CommonApiConstants.RESULT_CODE_ERROR
import org.secuso.privacyfriendlybackup.api.common.CommonApiConstants.RESULT_CODE_SUCCESS
import org.secuso.privacyfriendlybackup.api.common.CommonApiConstants.RESULT_ERROR
import org.secuso.privacyfriendlybackup.api.common.PfaApi.ACTION_CONNECT
import org.secuso.privacyfriendlybackup.api.common.PfaApi.EXTRA_CONNECT_IMMEDIATE
import org.secuso.privacyfriendlybackup.api.common.PfaApi.EXTRA_CONNECT_PACKAGE_NAME
import org.secuso.privacyfriendlybackup.api.common.PfaError
import org.secuso.privacyfriendlybackup.api.util.ApiFormatter
import org.secuso.privacyfriendlybackup.api.worker.ConnectBackupWorker
import org.secuso.privacyfriendlybackup.api.worker.CreateBackupWorker
import java.util.concurrent.Executors

/**
 * This class is meant to be extended by the PFA. Also it should then be included in the PFA's
 * AndroidManifest.xml file.
 *
 * <pre>
 * {@code
 *      <service
 *           android:name=".PFAAuthService"
 *           android:enabled="true"
 *           android:exported="true"
 *           android:process=":backup"
 *           tools:ignore="ExportedService">
 *           <intent-filter>
 *               <action android:name="org.secuso.privacyfriendlybackup.api.pfa.PFAAuthService" />
 *           </intent-filter>
 *      </service>
 *      }
 * </pre>
 *
 * @author Christopher Beckmann
 */
abstract class PFAAuthService : AbstractAuthService() {

    val executor = Executors.newSingleThreadExecutor()

    override val SUPPORTED_API_VERSIONS = listOf(1)

    override val mBinder : IPFAService.Stub = object : IPFAService.Stub()  {

        override fun send(data: Intent?): Intent {
            Log.d(this.javaClass.simpleName, "Intent received: ${ApiFormatter.formatIntent(data)}")
            val result = canAccess(data)
            if(result != null) {
                return result
            }
            // data can not be null here else canAccess(Intent) would have returned an error
            val resultIntent = handle(data!!)
            Log.d(this.javaClass.simpleName, "Sent Reply: ${ApiFormatter.formatIntent(resultIntent)}")
            return resultIntent
        }

        private fun handle(data: Intent): Intent {
            return when(data.action) {
                ACTION_CONNECT -> handleConnect(data)
                else -> Intent().apply {
                    putExtra(RESULT_CODE, RESULT_CODE_ERROR)
                    putExtra(RESULT_ERROR,
                        PfaError(
                            PfaError.PfaErrorCode.ACTION_ERROR,
                            "Action ${data.action} is unsupported."
                        )
                    )
                }
            }
        }

        private fun handleConnect(data: Intent): Intent {
            val backupPackageName = data.getStringExtra(EXTRA_CONNECT_PACKAGE_NAME)
            val connectImmediately = data.getBooleanExtra(EXTRA_CONNECT_IMMEDIATE, false)

            startBackupProcess()

            return Intent().apply {
                putExtra(RESULT_CODE, RESULT_CODE_SUCCESS)
            }
        }
    }

    private fun startBackupProcess() {
        executor.run {
            val workManager = WorkManager.getInstance(this@PFAAuthService)

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
}