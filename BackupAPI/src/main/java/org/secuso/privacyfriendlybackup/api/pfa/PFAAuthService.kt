package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Intent
import org.secuso.privacyfriendlybackup.api.IPFAService
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.ACTION_BACKUP
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.ACTION_RESTORE
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.EXTRA_CONNECT_PACKAGE_NAME

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

    override val SUPPORTED_API_VERSIONS = listOf(1)

    override val mBinder : IPFAService.Stub = object : IPFAService.Stub()  {

        override fun send(data: Intent?): Intent {
            val result = canAccess(data)
            if(result != null) {
                return result
            }
            // data can not be null here else canAccess(Intent) would have returned an error
            return handle(data!!)
        }

        private fun handle(data: Intent): Intent {
            return when(data.action) {
                ACTION_BACKUP -> handleBackup(data)
                ACTION_RESTORE -> handleRestore(data)
                else -> Intent().apply {
                    putExtra(RESULT_CODE, RESULT_CODE_ERROR)
                    putExtra(RESULT_ERROR, PfaError(PfaError.PfaErrorCode.ACTION_ERROR, "Action ${data.action} is unsupported."))
                }
            }
        }

        private fun handleBackup(data: Intent): Intent {
            TODO("Not yet implemented")
        }

        private fun handleRestore(data: Intent): Intent {
            TODO("Not yet implemented")
        }

        private fun successIntent(data : Intent) : Intent {
            // TODO: do something with this?
            val backupPackageName = data.getStringExtra(EXTRA_CONNECT_PACKAGE_NAME)

            return Intent().apply {
                putExtra(RESULT_CODE, RESULT_CODE_SUCCESS)
            }
        }
    }
}