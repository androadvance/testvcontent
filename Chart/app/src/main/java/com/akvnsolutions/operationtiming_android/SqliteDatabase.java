package com.akvnsolutions.operationtiming_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SqliteDatabase extends SQLiteOpenHelper {

    SQLiteDatabase sqLiteDatabase;
    SqliteDatabase cxt;

    public SqliteDatabase(@Nullable Context context) {
        super(context, "OperationTiming.db", null, 1);
        sqLiteDatabase = getWritableDatabase();
        cxt = this;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table login(Username text,Company text,Mailid text,MobileNumber text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long saveit(String s_username,String s_company,String mailid,String mobilenumber){
        sqLiteDatabase=this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Username",s_username);
        contentValues.put("Company",s_company);
        contentValues.put("Mailid",mailid);
        contentValues.put("MobileNumber",mobilenumber);
        long k = sqLiteDatabase.insert("login",null,contentValues);
        return k;
    }

    public String getuser() {

        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from login", null );
        res.moveToFirst();

        if(res.getCount()>0)

            return  (res.getString(res.getColumnIndex("Mailid")));

        return "";
    }

    public Cursor fetchData() {

        Cursor cursor;
        cursor = sqLiteDatabase.rawQuery("select * from login", new String[]{});
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchMobilenumber(String s_email) {

        Cursor cursor;
        cursor = sqLiteDatabase.rawQuery("select MobileNumber from login where Mailid=?   COLLATE NOCASE", new String[]{s_email});
        cursor.moveToFirst();
        return cursor;
    }


    public boolean updateData(String s_username,String s_company,String mailid,String mobilenumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Username",s_username);
        contentValues.put("Company",s_company);
        contentValues.put("MobileNumber",mobilenumber);
        contentValues.put("Mailid",mailid);
        db.update("login", contentValues, "Mailid = ?",new String[] { mailid });
        return true;
    }

}
