package org.secuso.privacyfriendlybackup.api.pfa

import android.content.Context

/**
 * @author Christopher Beckmann
 */
object BackupDataStore {

    // shared preference is prolly not the best choice...
    const val BACKUP_DATA = "BackupDataStore.BACKUP_DATA"
    const val RESTORE_DATA = "BackupDataStore.RESTORE_DATA"

    fun getRestoreData(context: Context) : String? {
        val pref = context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
        return pref.getString(RESTORE_DATA, null)
    }

    fun saveRestoreData(context: Context, restoreData: String) {
        val pref = context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
        pref.edit().putString(RESTORE_DATA, restoreData).apply()
    }

    fun cleanRestoreData(context: Context) {
        val pref = context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
        pref.edit().remove(RESTORE_DATA).apply()
    }

    fun getBackupData(context: Context): String? {
        val pref = context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
        return pref.getString(BACKUP_DATA, null)
    }

    fun saveBackupData(context: Context, backupData: String) {
        val pref = context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
        pref.edit().putString(BACKUP_DATA, backupData).apply()
    }

    private fun cleanBackupData(context: Context) {
        val pref = context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
        pref.edit().remove(BACKUP_DATA).apply()
    }

    fun isBackupDataSaved(context: Context): Boolean {
        return context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
            .contains(BACKUP_DATA)
    }

    fun isRestoreDataSaved(context: Context): Boolean {
        return context.getSharedPreferences(BackupDataStore::class.simpleName, Context.MODE_PRIVATE)
            .contains(RESTORE_DATA)
    }

    fun cleanBackupDataIfNoRestoreData(context: Context) {
        if(!isRestoreDataSaved(context)) {
            cleanBackupData(context)
        }
    }
}