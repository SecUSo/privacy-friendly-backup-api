package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Context

interface IBackupCreator {
    fun createBackup(context: Context) : String
}