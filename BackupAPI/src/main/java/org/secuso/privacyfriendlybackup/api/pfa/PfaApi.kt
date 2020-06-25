package org.secuso.privacyfriendlybackup.api.pfa

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//@Parcelize
data class PfaError(
    val code : PfaErrorCode,
    val errorMessage : String?
) : Parcelable {

    enum class PfaErrorCode {
        INTENT_ERROR,
        AUTHENTICATION_ERROR,
        API_VERSION_UNSUPPORTED,
        ACTION_ERROR,
        SERVICE_NOT_BOUND,
        GENERAL_ERROR
    }

    constructor(parcel: Parcel) : this(PfaErrorCode.values()[parcel.readInt()], parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(code.ordinal)
        parcel.writeString(errorMessage)
    }

    override fun describeContents(): Int  = 0

    companion object CREATOR : Parcelable.Creator<PfaError> {
        override fun createFromParcel(parcel: Parcel): PfaError {
            return PfaError(parcel)
        }

        override fun newArray(size: Int): Array<PfaError?> {
            return arrayOfNulls(size)
        }
    }
}

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