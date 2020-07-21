package org.secuso.privacyfriendlybackup.api.backup

import android.content.SharedPreferences
import android.util.JsonWriter

/**
 * This is a convenience class, that provides utility methods to write preferences to json.
 * @author Christopher Beckmann (Kamuno)
 */
object PreferenceUtil {
    @JvmStatic
    fun writePreferences(writer: JsonWriter, pref: SharedPreferences) {
        writer.beginObject()
        for((key, value) in pref.all) {
            writer.name(key)

            // App should know the types of the keys when it sees them.
            // So only store the key value pairs.
            when(value) {
                is String -> writer.value(value)
                is Int -> writer.value(value)
                is Boolean -> writer.value(value)
                is Float -> writer.value(value)
                is Long -> writer.value(value)
                is Set<*> -> {
                    writer.beginArray()
                    for(v in value) {
                        writer.value(v as String)
                    }
                    writer.endArray()
                }
            }
        }
        writer.endObject()
    }
}