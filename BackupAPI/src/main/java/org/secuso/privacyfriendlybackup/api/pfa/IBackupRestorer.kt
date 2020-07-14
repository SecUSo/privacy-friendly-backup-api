package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Context
import java.io.InputStream

interface IBackupRestorer {
    fun restoreBackup(context: Context, restoreData: InputStream) : Boolean
}