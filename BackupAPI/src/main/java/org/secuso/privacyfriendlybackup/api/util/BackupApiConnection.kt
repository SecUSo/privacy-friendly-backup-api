package org.secuso.privacyfriendlybackup.api.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.secuso.privacyfriendlybackup.api.IBackupService
import org.secuso.privacyfriendlybackup.api.common.BackupApi
import org.secuso.privacyfriendlybackup.api.common.CommonApiConstants
import org.secuso.privacyfriendlybackup.api.common.PfaError

const val API_VERSION = 1

/**
 * @author Christopher Beckmann
 */
class BackupApiConnection(
    private val mContext: Context,
    private val mBackupServiceName: String,
    private val mBackupApiListener: IBackupApiListener? = null,
    private val mApiVersion: Int = API_VERSION) {

    interface IBackupApiListener {
        fun onBound(service : IBackupService?)
        fun onError(error : PfaError)
        fun onSuccess()
        fun onDisconnected()
    }

    private var mService : IBackupService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBackupApiListener?.onDisconnected()
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = IBackupService.Stub.asInterface(service)
            mBackupApiListener?.onBound(mService)
        }
    }

    fun send(action : String) {
        if(!isBound()) {
            mBackupApiListener?.onError(
                PfaError(
                    PfaError.PfaErrorCode.SERVICE_NOT_BOUND,
                    "Service is not bound."
                )
            )
        }
        val result : Intent = mService!!.send(Intent().apply {
            putExtra(CommonApiConstants.EXTRA_API_VERSION, mApiVersion)
            setAction(action)
        })

        result.setExtrasClassLoader(mContext.classLoader)

        when(result.getIntExtra(CommonApiConstants.RESULT_CODE, -1)) {
            CommonApiConstants.RESULT_CODE_SUCCESS -> {
                mBackupApiListener?.onSuccess()
            }
            CommonApiConstants.RESULT_CODE_ERROR -> {
                try {
                    val error: PfaError? =
                        result.getParcelableExtra(CommonApiConstants.RESULT_ERROR)
                    if (error != null) {
                        mBackupApiListener?.onError(error)
                    } else {
                        mBackupApiListener?.onError(
                            PfaError(
                                PfaError.PfaErrorCode.GENERAL_ERROR,
                                "Unknown error occurred. Couldn't load error."
                            )
                        )
                    }
                } catch (e : Exception) {
                    mBackupApiListener?.onError(
                        PfaError(
                            PfaError.PfaErrorCode.GENERAL_ERROR,
                            "Exception occurred: ${e.message}"
                        )
                    )
                }
            }
            else -> {
                mBackupApiListener?.onError(
                    PfaError(
                        PfaError.PfaErrorCode.GENERAL_ERROR,
                        "RESULT_CODE unknown."
                    )
                )
            }
        }
    }

    fun connect() {
        if(mService == null) {
            Intent(BackupApi.BACKUP_CONNECT_ACTION).also { intent ->
                // this is the name of the Backup Service to connect to
                intent.setPackage(mBackupServiceName)
                mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
            }
        } else {
            mBackupApiListener?.onBound(mService)
        }
    }

    fun disconnect() {
        mContext.unbindService(mConnection)
    }

    fun isBound() : Boolean {
        return mService != null
    }
}