package org.secuso.privacyfriendlybackup.api.pfa

import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer

/**
 * @author Christopher Beckmann
 */
object BackupManager {
    var backupCreator : IBackupCreator? = null
    var backupRestorer : IBackupRestorer? = null
}