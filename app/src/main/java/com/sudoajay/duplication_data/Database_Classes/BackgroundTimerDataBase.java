package com.sudoajay.duplication_data.Database_Classes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BackgroundTimerDataBase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "BackgroundTimerDataBase.db";
    public static final String DATABASE_TABLE_NAME = "BackgroundTimerDATABASE_TABLE_NAME";
    public static final String col_1 = "ID";
    public static final String col_2 = "Repeatedly";
    public static final String col_3 = "Weekdays";
    public static final String col_4 = "Endlessly";

    public BackgroundTimerDataBase(Context context  )
    {
        super(context, DATABASE_NAME, null,1);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DATABASE_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "Repeatedly INTEGER,Weekdays TEXT,Endlessly DEFAULT CURRENT_DATE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME);
        onCreate(db);
    }
    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DATABASE_TABLE_NAME, "ID =?",new String[] {id});
    }
    public void FillIt(final int repeatedly ,final String weekdays , final String endlessly){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_2,repeatedly);
        contentValues.put(col_3,weekdays);
        contentValues.put(col_4, endlessly);
        sqLiteDatabase.insert(DATABASE_TABLE_NAME,null,contentValues);
    }
    public boolean check_For_Empty(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery("select * from "+DATABASE_TABLE_NAME,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        if(count > 0) {
            return false;
        }
        return true;
    }
    public Cursor GetTheValueFromId(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery( "SELECT * FROM " + DATABASE_TABLE_NAME,null);
    }
    public Cursor GetTheRepeatedlyWeekdays(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery( "SELECT  Repeatedly ,Weekdays FROM " + DATABASE_TABLE_NAME,null);
    }
    public Cursor GetTheChoose_TypeRepeatedlyEndlessly(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT Repeatedly,Weekdays,Endlessly FROM "+ DATABASE_TABLE_NAME  ,null);
    }
    public Cursor GetTheId(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT ID FROM "+ DATABASE_TABLE_NAME  ,null);
    }

    public void UpdateTheTable(final String id ,final int repeatedly ,final String weekdays ,final String endlessly){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_1,id);
        contentValues.put(col_2,repeatedly);
        contentValues.put(col_3,weekdays);
        contentValues.put(col_4, endlessly);
        sqLiteDatabase.update(DATABASE_TABLE_NAME,contentValues,"ID = ?",new String[] { id });
    }


}
