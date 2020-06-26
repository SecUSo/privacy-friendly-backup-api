package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Context

interface IBackupRestorer {
    fun restoreBackup(context: Context, restoreData: String) : Boolean
}