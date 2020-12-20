package com.example.notepad;

import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.reflect.Modifier;

public class NotePad {
    public static final String AUTHORITY = "com.example.notepad.NotePadProvider";
    public static final String DATABASE_NAME= "note.db";
    public  static final int DATABASE_VERSION = 1;
    private NotePad(){

    }
    public static final class NoteTable implements BaseColumns{
        public static final String TABLE_NAME ="notes";
        public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/notes");
        public static final String CONTENT_TYPE  = "vnd.android.cursor.dir/vnd.google.notes";
        public static final String CONTENT_ITEM_TYPE  = "vnd.android.cursor.item/vnd.google.notes";
        public static final String NOTE_TITLE ="title";
        public static final String NOTE_CONTENT ="content";
        public static final String CREATE_DATE ="create_date";
        public static final String MODIFY_DATE = "modify_date";
        public static final String DEFAULT_ORDER_BY  ="create_date DESC";
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + NOTE_TITLE + " VARCHAR(50),"
                + NOTE_CONTENT + " TEXT,"
                + CREATE_DATE + " INTEGER,"
                + MODIFY_DATE + " INTEGER"
                + ");" ;

    }
}
