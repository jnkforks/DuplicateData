package com.sudoajay.duplication_data.databaseClasses

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BackgroundTimerDataBase(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table " + DATABASE_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "Repeatedly INTEGER,Weekdays TEXT,Endlessly DEFAULT CURRENT_DATE)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $DATABASE_TABLE_NAME")
        onCreate(db)
    }

    fun deleteData() {
        val db = this.writableDatabase
        db.delete(DATABASE_TABLE_NAME, "ID =?", arrayOf(1.toString() + ""))
    }

    fun fillIt(repeatedly: Int, weekdays: String?, endlessly: String?) {
        val sqLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(col_2, repeatedly)
        contentValues.put(col_3, weekdays)
        contentValues.put(col_4, endlessly)
        sqLiteDatabase.insert(DATABASE_TABLE_NAME, null, contentValues)
    }

    fun checkForEmpty(): Boolean {
        val sqLiteDatabase = this.writableDatabase
        val cursor = sqLiteDatabase.rawQuery("select * from $DATABASE_TABLE_NAME", null)
        cursor.moveToFirst()
        val count = cursor.count
        cursor.close()
        return count <= 0
    }

    fun getTheValueFromId(): Cursor {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.rawQuery("SELECT * FROM $DATABASE_TABLE_NAME", null)
    }

    fun getTheRepeatedlyWeekdays(): Cursor {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.rawQuery("SELECT  Repeatedly ,Weekdays FROM $DATABASE_TABLE_NAME", null)
    }

    fun getTheChooseTypeRepeatedlyEndlessly(): Cursor {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.rawQuery("SELECT Repeatedly,Weekdays,Endlessly FROM $DATABASE_TABLE_NAME", null)
    }

    fun updateTheTable(id: String, repeatedly: Int, weekdays: String?, endlessly: String?) {
        val sqLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(col_1, id)
        contentValues.put(col_2, repeatedly)
        contentValues.put(col_3, weekdays)
        contentValues.put(col_4, endlessly)
        sqLiteDatabase.update(DATABASE_TABLE_NAME, contentValues, "ID = ?", arrayOf(id))
    }

    companion object {
        private const val DATABASE_NAME = "BackgroundTimerDataBase.db"
        private const val DATABASE_TABLE_NAME = "BackgroundTimerDATABASE_TABLE_NAME"
        private const val col_1 = "ID"
        private const val col_2 = "Repeatedly"
        private const val col_3 = "Weekdays"
        private const val col_4 = "Endlessly"
    }
}