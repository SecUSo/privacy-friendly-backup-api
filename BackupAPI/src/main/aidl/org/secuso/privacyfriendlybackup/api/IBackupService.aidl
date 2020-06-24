// IBackupService.aidl
package org.secuso.privacyfriendlybackup.api;

interface IBackupService {
    Intent send(in Intent data);
}
