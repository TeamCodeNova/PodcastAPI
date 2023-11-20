package com.example.podcastapi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_UUID TEXT PRIMARY KEY, $COLUMN_NAME TEXT, $COLUMN_RSS_URL TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(uuid: String, name: String, rssUrl: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_UUID, uuid)
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_RSS_URL, rssUrl)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getData(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "PodcastDB"
        const val TABLE_NAME = "podcasts"
        const val COLUMN_UUID = "uuid"
        const val COLUMN_NAME = "name"
        const val COLUMN_RSS_URL = "rss_url"
    }
}
