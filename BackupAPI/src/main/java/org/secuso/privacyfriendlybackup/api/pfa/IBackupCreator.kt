package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Context
import java.io.OutputStream

interface IBackupCreator {
    fun writeBackup(context: Context, outputStream: OutputStream)
}