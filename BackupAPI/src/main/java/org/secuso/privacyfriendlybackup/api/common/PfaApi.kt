package org.secuso.privacyfriendlybackup.api.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PfaError(val code : PfaErrorCode, val errorMessage : String?) : Parcelable {
    /**
     * New PFAErrorCodes should always be added at the end to avoid API version errors
     */
    enum class PfaErrorCode {
        INTENT_ERROR,
        AUTHENTICATION_ERROR,
        API_VERSION_UNSUPPORTED,
        ACTION_ERROR,
        SERVICE_NOT_BOUND,
        GENERAL_ERROR
    }
}

object CommonApiConstants {
    // Result Code Action
    const val RESULT_CODE = "RESULT_CODE"

    // Result Codes
    const val RESULT_CODE_ERROR = 0
    const val RESULT_CODE_SUCCESS = 1

    /**
     * If RESULT_CODE is RESULT_CODE_ERROR then the error can be retrieved from RESULT_ERROR
     */
    const val RESULT_ERROR = "RESULT_ERROR"

    // Extras
    const val EXTRA_API_VERSION = "EXTRA_API_VERSION"
}

object PfaApi {
    // API Version
    const val API_VERSION = 1

    // Connect Actions
    const val PFA_CONNECT_ACTION = "org.secuso.privacyfriendlybackup.api.pfa.PFAAuthService"

    const val EXTRA_CONNECT_PACKAGE_NAME = "PfaApi.EXTRA_CONNECT_PACKAGE_NAME"
    const val EXTRA_CONNECT_IMMEDIATE = "PfaApi.EXTRA_CONNECT_IMMEDIATE"

    // Command Actions
    const val ACTION_CONNECT = "PfaApi.ACTION_CONNECT"
}

object BackupApi {
    // API Version
    const val API_VERSION = 1

    // Connect Actions
    const val BACKUP_CONNECT_ACTION = "org.secuso.privacyfriendlybackup.services.BackupService"

    // Command Actions
}