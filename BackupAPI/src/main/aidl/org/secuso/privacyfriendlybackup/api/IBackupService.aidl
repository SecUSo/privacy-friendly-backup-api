// IBackupService.aidl
package org.secuso.privacyfriendlybackup.api;

interface IBackupService {

    void performRestore();
    void performBackup();
    Intent send(in Intent data);
}
