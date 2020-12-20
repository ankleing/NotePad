package com.example.notepad;

import android.app.AppComponentFactory;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotePadEditor extends AppCompatActivity {
    private List<NoteList.ListFormat> mapList;
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    private int mState;
    private Uri mUri;
    private EditText title;
    private EditText note;
    private String localTitle;
    private String localNote;

    @Override
    public void registerForContextMenu(View view) {
        super.registerForContextMenu(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notepad_editor);
        ConstraintLayout ll =findViewById(R.id.consly);
        Long df = Long.valueOf(System.currentTimeMillis());
        SimpleDateFormat sdf=new SimpleDateFormat("HH");
        String now = sdf.format(new Date(Long.parseLong(String.valueOf(df))));
        Log.e("TIME",now);
        final int t =Integer.parseInt(now);
        if(t>21){
            ll.setBackgroundResource(R.drawable.day);
        }else {
            ll.setBackgroundResource(R.drawable.night);
        }
        title = findViewById(R.id.editorTitle);
        note = findViewById(R.id.editorNote);
        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Click(view);
            }
        });
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            //对数据库插入
        } else {
            Log.e("Editor", "Unknown Uri:" + intent.getData());
            finish();
        }
        if(mState==STATE_EDIT) {
            Log.e("EditorUri----", String.valueOf(intent.getData()));
            Cursor cursor = this.getContentResolver().query(getIntent().getData(), null, null, null);
            while (cursor.moveToNext()) {
                title.setText(cursor.getString(cursor.getColumnIndex(NotePad.NoteTable.NOTE_TITLE)));
                localTitle = title.getText().toString();
                note.setText(cursor.getString(cursor.getColumnIndex(NotePad.NoteTable.NOTE_CONTENT)));
                localNote = note.getText().toString();

            }
            cursor.close();
        }

    }

    public void Click(View view) {
        int count = -1;
        long modify = 0;
//        if(!(localTitle.equals(title.getText().toString())) || !(localNote.equals(note.getText().toString()))){
        Log.e("state", String.valueOf(mState));
        if(mState==STATE_INSERT){
            ContentValues contentValues = new ContentValues();
            contentValues.put(NotePad.NoteTable.NOTE_TITLE, title.getText().toString());
            contentValues.put(NotePad.NoteTable.NOTE_CONTENT, note.getText().toString());
            modify = System.currentTimeMillis();
            contentValues.put(NotePad.NoteTable.MODIFY_DATE, modify);
            Uri uri = this.getContentResolver().insert(NotePad.NoteTable.CONTENT_URI,contentValues);

        } else if (mState==STATE_EDIT||!(localTitle.equals(title.getText().toString())) || !(localNote.equals(note.getText().toString())) ) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NotePad.NoteTable.NOTE_TITLE, title.getText().toString());
            contentValues.put(NotePad.NoteTable.NOTE_CONTENT, note.getText().toString());
            modify = System.currentTimeMillis();
            contentValues.put(NotePad.NoteTable.MODIFY_DATE, modify);
            Log.e("UPDATE", String.valueOf(getIntent().getData()));
            count = this.getContentResolver().update(getIntent().getData(), contentValues, null, null);

        }

    }

}
