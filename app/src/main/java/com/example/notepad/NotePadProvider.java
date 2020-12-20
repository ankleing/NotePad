package com.example.notepad;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotePadProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher;
    private static final int COLLECTION_INDICATOR = 1;
    private static final int SINGLE_INDICATOR = 2;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", COLLECTION_INDICATOR);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", SINGLE_INDICATOR);
    }
    private static HashMap<String,String> NotesProjectionMap;
    static {
        NotesProjectionMap=new HashMap<String,String>();
        NotesProjectionMap.put(NotePad.NoteTable._ID,NotePad.NoteTable._ID);
        NotesProjectionMap.put(NotePad.NoteTable.NOTE_CONTENT,NotePad.NoteTable.NOTE_CONTENT);
        NotesProjectionMap.put(NotePad.NoteTable.NOTE_TITLE,NotePad.NoteTable.NOTE_TITLE);
        NotesProjectionMap.put(NotePad.NoteTable.CREATE_DATE,NotePad.NoteTable.CREATE_DATE);
        NotesProjectionMap.put(NotePad.NoteTable.MODIFY_DATE,NotePad.NoteTable.MODIFY_DATE);
    }
    private DatabaseHelper mDbHelper;
    private static class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(@Nullable Context context) {
            super(context, NotePad.DATABASE_NAME, null, NotePad.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(NotePad.NoteTable.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+NotePad.NoteTable.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Log.e("query detail uri", String.valueOf(uri));
        switch(sUriMatcher.match(uri)){
            case COLLECTION_INDICATOR:
                Log.e("COLLECTION uri", String.valueOf(uri));
                queryBuilder.setTables(NotePad.NoteTable.TABLE_NAME);
                queryBuilder.setProjectionMap(NotesProjectionMap);
                break;
//            case SINGLE_INDICATOR:
//                Log.e("SINGLE uri ", String.valueOf(uri));
//                queryBuilder.setTables(NotePad.NoteTable.TABLE_NAME);
//                queryBuilder.setProjectionMap(NotesProjectionMap);
//                queryBuilder.appendWhere(NotePad.NoteTable._ID + "=" + "'1'");
            default:
                Log.e("SINGLE uri ", String.valueOf(uri));
                queryBuilder.setTables(NotePad.NoteTable.TABLE_NAME);
                queryBuilder.setProjectionMap(NotesProjectionMap);
                queryBuilder.appendWhere(NotePad.NoteTable._ID + "="+uri.getPathSegments().get(1));
//                throw new IllegalArgumentException("query Unknown URI: "+uri);
        }
        String orderBy;
        if(TextUtils.isEmpty(sortOrder))
        {
            orderBy=NotePad.NoteTable.DEFAULT_ORDER_BY;
        }else {
            orderBy=sortOrder;
        }
        SQLiteDatabase db =mDbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db,projection,selection,selectionArgs,null,null,orderBy);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)){
            case COLLECTION_INDICATOR:
                return NotePad.NoteTable.CONTENT_TYPE;
            case SINGLE_INDICATOR:
                return NotePad.NoteTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("getType Unknown URI: "+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if(sUriMatcher.match(uri)!=COLLECTION_INDICATOR){
            throw new IllegalArgumentException("insert Unknown URI: "+uri);
        }
        SQLiteDatabase db =mDbHelper.getWritableDatabase();
        long rowID=db.insert(NotePad.NoteTable.TABLE_NAME,null,contentValues);
        if (rowID>0){
            Uri retUri = ContentUris.withAppendedId(NotePad.NoteTable.CONTENT_URI,rowID);
            return retUri;
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        int count=-1;
        switch (sUriMatcher.match(uri)){
            case COLLECTION_INDICATOR:
                count = db.delete(NotePad.NoteTable.TABLE_NAME,s,strings);
                break;
            case SINGLE_INDICATOR:
                String rowID = uri.getPathSegments().get(1);
                count=db.delete(NotePad.NoteTable.TABLE_NAME, NotePad.NoteTable._ID+" = "+rowID,null);
                break;
            default:
                throw new IllegalArgumentException("delete Unknown URI: "+uri);

        }
        this.getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = -1;
        String rowID;
        switch (sUriMatcher.match(uri)){
            case COLLECTION_INDICATOR:
                count=db.update(NotePad.NoteTable.TABLE_NAME,contentValues,s,strings);
                Log.e("Update Multiply .id","");
                break;
            case SINGLE_INDICATOR:
                 rowID=uri.getPathSegments().get(1);
                Log.e("Update single .id",rowID);
                count = db.update(NotePad.NoteTable.TABLE_NAME, contentValues,NotePad.NoteTable._ID+"="+rowID,null);
            default:
                 rowID=uri.getPathSegments().get(1);
                Log.e("Update single .id",rowID);
                count = db.update(NotePad.NoteTable.TABLE_NAME, contentValues,NotePad.NoteTable._ID+"="+rowID,null);
//                throw new IllegalArgumentException("update Unknown URI:"+uri);
        }
        this.getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
