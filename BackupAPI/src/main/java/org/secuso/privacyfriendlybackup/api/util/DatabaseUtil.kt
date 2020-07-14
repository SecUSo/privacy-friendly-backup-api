package org.secuso.privacyfriendlybackup.api.util

import android.database.Cursor.*
import android.database.sqlite.SQLiteDatabase
import android.util.JsonReader
import android.util.JsonWriter
import androidx.core.database.getBlobOrNull
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import java.io.StringReader
import java.io.StringWriter

/**
 * This class turns a database into a JSON string
 * <p>
 * Structure based on http://tech.sarathdr.com/android-app/convert-database-cursor-result-to-json-array-android-app-development/
 * accessed at 25th December 2016
 *
 * @author Karola Marky (yonjuni), Christopher Beckmann (Kamuno)
 */
object DatabaseUtil {

    @JvmStatic
    fun jsonToDatabase(json: String) {
        val reader = JsonReader(StringReader(json))
    }

    @JvmStatic
    fun writeDatabase(writer: JsonWriter, db: SQLiteDatabase) {
        writer.beginObject()
        writer.name("version").value(db.version)
        writer.name("content")
        writeDatabaseContent(writer, db)
        writer.endObject()
    }

    @JvmStatic
    fun writeDatabaseContent(writer: JsonWriter, db : SQLiteDatabase) {
        writer.beginArray()
        val tableInfo = getTables(db)
        for(table in tableInfo) {
            writer.beginObject()
            writer.name("tableName").value(table.first)
            writer.name("createSql").value(table.second)
            writer.name("values")
            writeTable(writer, db, table.first)
            writer.endObject()
        }
        writer.endArray()
    }

    @JvmStatic
    fun getTables(db : SQLiteDatabase) : List<Pair<String, String?>> {
        val resultList = ArrayList<Pair<String, String?>>()

        db.query("sqlite_master", arrayOf("name", "sql"), "type = ?", arrayOf("table"), null, null, null).use { cursor ->
            cursor.moveToFirst()
            while(!cursor.isAfterLast) {
                val name = cursor.getStringOrNull(cursor.getColumnIndex("name")) ?: ""
                val sql = cursor.getStringOrNull(cursor.getColumnIndex("sql"))
                resultList.add(name to sql)
                cursor.moveToNext();
            }
        }
        return resultList
    }

    @JvmStatic
    fun writeTable(writer: JsonWriter, db : SQLiteDatabase, table: String) {
        writer.beginArray()
        db.query(table, null, null, null, null, null, null).use { cursor ->
            cursor.moveToFirst()
            while(!cursor.isAfterLast) {
                writer.beginObject()
                for(i in 0 until cursor.columnCount) {
                    writer.name(cursor.getColumnName(i))
                    try {
                        when(cursor.getType(i)) {
                            FIELD_TYPE_NULL -> {
                                writer.value(cursor.getStringOrNull(i))
                            }
                            FIELD_TYPE_INTEGER ->   {
                                writer.value(cursor.getIntOrNull(i))
                            }
                            FIELD_TYPE_FLOAT ->     {
                                writer.value(cursor.getFloatOrNull(i))
                            }
                            FIELD_TYPE_STRING ->    {
                                writer.value(cursor.getStringOrNull(i))
                            }
                            FIELD_TYPE_BLOB ->      {
                                writer.value(cursor.getBlobOrNull(i)?.toBase64())
                            }
                            else ->                 {
                                writer.value(cursor.getStringOrNull(i))
                            }
                        }
                    } catch (e : Exception) {
                        writer.nullValue()
                    }
                }
                writer.endObject()
                cursor.moveToNext()
            }
        }
        writer.endArray()
    }

}

fun SQLiteDatabase.toJSON() : String {
    val writer = JsonWriter(StringWriter())
    writer.setIndent("")
    DatabaseUtil.writeDatabase(writer, this)
    return writer.toString()
}

fun SQLiteDatabase.toReadableJSON() : String {
    val writer = JsonWriter(StringWriter())
    writer.setIndent("  ")
    DatabaseUtil.writeDatabase(writer, this)
    return writer.toString()
}