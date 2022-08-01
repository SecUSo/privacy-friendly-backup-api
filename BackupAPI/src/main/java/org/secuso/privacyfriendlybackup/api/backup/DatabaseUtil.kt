package org.secuso.privacyfriendlybackup.api.backup

import android.content.ContentValues
import android.content.Context
import android.database.Cursor.*
import android.database.sqlite.SQLiteDatabase
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.core.database.getBlobOrNull
import androidx.core.database.getFloatOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.secuso.privacyfriendlybackup.api.util.toBase64
import java.io.StringWriter

/**
 * This is a convenience class, that provides utility methods to write databases to json.
 * <p>
 * Structure based on
 * https://github.com/SecUSo/privacy-friendly-app-example/blob/79b6ccbe81062142091196b592121ed9384c7466/app/src/main/java/org/secuso/privacyfriendlyexample/database/DatabaseExporter.java
 * Original class by Karola Marky (yonjuni)
 *
 * @author Christopher Beckmann (Kamuno)
 */
object DatabaseUtil {

    @JvmStatic
    fun writeDatabase(writer: JsonWriter, db: SupportSQLiteDatabase) {
        writer.beginObject()
        writer.name("version").value(db.version)
        writer.name("content")
        writeDatabaseContent(writer, db)
        writer.endObject()
    }

    @JvmStatic
    fun writeDatabaseContent(writer: JsonWriter, db : SupportSQLiteDatabase) {
        writer.beginArray()
        val tableInfo = getTables(db)
        for(table in tableInfo) {
            // do not write android_metadata, as this table will automatically be created when restoring
            if(table.first == "android_metadata") {
                continue
            }

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
    fun getTables(db : SupportSQLiteDatabase) : List<Pair<String, String?>> {
        val resultList = ArrayList<Pair<String, String?>>()

        db.query("SELECT name, sql FROM sqlite_master WHERE type='table'").use { cursor ->
            cursor.moveToFirst()
            while(!cursor.isAfterLast) {
                val name = cursor.getStringOrNull(cursor.getColumnIndex("name")) ?: ""
                val sql = cursor.getStringOrNull(cursor.getColumnIndex("sql"))
                resultList.add(name to sql)
                cursor.moveToNext()
            }
        }
        return resultList
    }

    @JvmStatic
    fun writeTable(writer: JsonWriter, db : SupportSQLiteDatabase, table: String) {
        writer.beginArray()
        db.query("SELECT * FROM $table").use { cursor ->
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
                                writer.value(cursor.getLongOrNull(i))
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

    @JvmStatic
    fun readDatabaseContent(reader: JsonReader, db: SupportSQLiteDatabase) {
        reader.beginArray()

        while(reader.hasNext()) {
            readTable(reader,db)
        }

        reader.endArray()
    }

    @JvmStatic
    fun readTable(reader: JsonReader, db: SupportSQLiteDatabase) {
        reader.beginObject()

        // tableName
        reader.nextName()
        val tableName = reader.nextString()

        // createSql
        reader.nextName()
        val createSql = reader.nextString()
        // do not create android_metadata - because it will automatically be created already
        if(tableName != "android_metadata" && tableName != "sqlite_sequence") {
            db.execSQL(createSql)
        }

        // values
        reader.nextName()
        readValues(reader, db, tableName)

        reader.endObject()
    }

    @JvmStatic
    fun readValues(reader: JsonReader, db: SupportSQLiteDatabase, tableName: String) {
        reader.beginArray()
        while(reader.hasNext()) {
            reader.beginObject()
            val cv = ContentValues()
            while(reader.hasNext()) {
                val name = reader.nextName()
                val isNotNull = reader.peek() != JsonToken.NULL
                val value = if(isNotNull) {
                    reader.nextString()
                } else {
                    reader.nextNull()
                    null
                }
                cv.put(name, value)
            }
            db.insert(tableName, SQLiteDatabase.CONFLICT_NONE, cv)
            reader.endObject()
        }
        reader.endArray()
    }

    @JvmStatic
    fun deleteRoomDatabase(context : Context, databaseName: String) {
        val databaseFile = context.getDatabasePath(databaseName)
        val databaseFileWal = context.getDatabasePath("$databaseName-wal")
        val databaseFileShm = context.getDatabasePath("$databaseName-shm")

        databaseFile.delete()
        databaseFileShm.delete()
        databaseFileWal.delete()
    }

    @JvmStatic
    fun deleteTables(db: SupportSQLiteDatabase) {
        // get table names
        val tableQuery = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';"
        val cursor = db.query(tableQuery)
        val tableNames = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tableNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }

        // delete tables
        for(name in tableNames) {
            db.execSQL("DROP TABLE IF EXISTS $name")
        }
    }

    @JvmStatic
    fun getSupportSQLiteOpenHelper(context: Context, databaseName: String, version: Int) : SupportSQLiteOpenHelper {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context).apply {
            name(databaseName)
            callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {}
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
        }.build()

        return FrameworkSQLiteOpenHelperFactory().create(config)
    }
}

fun SupportSQLiteDatabase.toJSON() : String {
    val writer = JsonWriter(StringWriter())
    writer.setIndent("")
    DatabaseUtil.writeDatabase(writer, this)
    return writer.toString()
}

fun SupportSQLiteDatabase.toReadableJSON() : String {
    val writer = JsonWriter(StringWriter())
    writer.setIndent("  ")
    DatabaseUtil.writeDatabase(writer, this)
    return writer.toString()
}
