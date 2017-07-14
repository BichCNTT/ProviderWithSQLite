package com.example.asus.myprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URI;
import java.util.HashMap;

/**
 * Created by Asus on 7/13/2017.
 */

public class MyProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.asus.myprovider.MyProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/persons";
    static final Uri CONTENT_URI = Uri.parse(URL);
    //simple way
    //public static final String PROVIDER_NAME="dantri.com";
    //public static final Uri CONTENT_URI=Uri.parse("content://"+PROVIDER_NAME+"/persons");
    static final String id = "id";
    static final String name = "name";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "persons", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "persons/*", uriCode);
    }

    @Override
    public boolean onCreate() {
        Context context=getContext();
        DatabaseHelper databaseHelper=new DatabaseHelper(context);
        db=databaseHelper.getWritableDatabase();
        if(db!=null){
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder=new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)){
            case uriCode:
                sqLiteQueryBuilder.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
        if(sortOrder==null||sortOrder==""){
            sortOrder=name;
        }
        Cursor cursor=sqLiteQueryBuilder.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)){
            case uriCode:
                return "com.android.cursor.dic/persons";
                default:
                    throw new IllegalArgumentException("Unsupported URI "+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowID=db.insert(TABLE_NAME,"",values);
        if(rowID>0){
            Uri _uri= ContentUris.withAppendedId(CONTENT_URI,rowID);
            getContext().getContentResolver().notifyChange(_uri,null);
            return _uri;
        }
        throw new SQLiteException("Failed to add a record into "+uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count=0;
        switch (uriMatcher.match(uri)){
            case uriCode:
                count=db.update(TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "mydb";
    static final String TABLE_NAME = "names";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE = "create table names(id integer primary key autoincrement, name text not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists" + TABLE_NAME);
            onCreate(db);
        }
    }
}
