package com.example.notepad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class NoteList extends AppCompatActivity {


    private static final String TAG = "NoteList";
    private RecyclerView mRecyclerView;
    private CardAdapter cardAdapter;
    private List<ListFormat> mList = new ArrayList<>();
    private List<ListFormat> dList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    String q;
    public interface OnItemClickListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);

    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener=onItemClickListener;
    }

    public static class ListFormat{
        String title;
        String content;
        String date;
        int id;

        @Override
        public String toString() {
            return "ListFormat{" +
                    "title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", date='" + date + '\'' +
                    '}';
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout ll =findViewById(R.id.layout);
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

        Intent intent = getIntent();
        if(intent.getData()==null){
            intent.setData(NotePad.NoteTable.CONTENT_URI);
        }
        EditText et = findViewById(R.id.edtx);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = v.getText().toString().trim();
                    Show2();
                    Toast.makeText(NoteList.this, keyword, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
            Show();
    }
    public void Show(){
        query();
        mRecyclerView = this.findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        lm.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        Collections.reverse(mList);
        cardAdapter =new CardAdapter(this, mList );
        cardAdapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Uri uri = ContentUris.withAppendedId(getIntent().getData(),mList.get(position).id);
                startActivity(new Intent(Intent.ACTION_EDIT,uri));
                Toast.makeText(NoteList.this,"click " + position, Toast.LENGTH_SHORT).show();
            }
        });
        cardAdapter.setOnItemLongClickListener(new CardAdapter.OnItemLongClickListener() {
            @Override
            public void onClick(int position) {
                delete(position);
                Toast.makeText(NoteList.this,"longclick " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(cardAdapter);
    }
    public void Show2(){
        EditText et = findViewById(R.id.edtx);
        dList.clear();
        String s =et.getText().toString();
        mRecyclerView = this.findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        lm.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        for(int i=0;i<mList.size();i++){
            if(mList.get(i).title.equals(s)){
                dList.add(mList.get(i));
            }
        }
        mList.clear();
        mList = dList;
        Collections.reverse(mList);
        cardAdapter =new CardAdapter(this, mList );
        cardAdapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Uri uri = ContentUris.withAppendedId(getIntent().getData(),mList.get(position).id);
                startActivity(new Intent(Intent.ACTION_EDIT,uri));
                Toast.makeText(NoteList.this,"click " + position, Toast.LENGTH_SHORT).show();
            }
        });
        cardAdapter.setOnItemLongClickListener(new CardAdapter.OnItemLongClickListener() {
            @Override
            public void onClick(int position) {
                delete(position);
                Toast.makeText(NoteList.this,"longclick " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(cardAdapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = data.getExtras().getString("result");//得到新Activity 关闭后返回的数据
        Log.i(TAG, result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.list_options_menu,menu);
        Intent intent = new Intent(null,getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NoteList.class), null, intent, 0, null);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Log.e("ffff",getIntent().getData().getPath());
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));

                return true;
            case R.id.menu_paste:
                startActivityForResult(new Intent(Intent.ACTION_PASTE, getIntent().getData()),1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }


    private void insert(){
        ContentValues values = new ContentValues();
        values.put("title","hello");
        values.put("content","my name is alex zhou");
        long time = System.currentTimeMillis();
        values.put("create_date",time);
        values.put("modify_date",time);
        Uri uri = this.getContentResolver().insert(NotePad.NoteTable.CONTENT_URI,values);
        Log.e("test",uri.toString());
    }
    private void delete(int position) {
        int count = this.getContentResolver().delete(NotePad.NoteTable.CONTENT_URI, "_id="+mList.get(position).id, null);
        onRestart();
        Log.e("delete ", "count="+count);
        query();
    }
    private void query(){
        Cursor cursor = this.getContentResolver().query(NotePad.NoteTable.CONTENT_URI,null,null,null,null);
        Log.e("test","count="+ cursor.getCount());
        cursor.moveToFirst();
        String title;
        String content;
        String now;
        ListFormat l;
        int id;
        while(!cursor.isAfterLast()){
            title = cursor.getString(cursor.getColumnIndex("title"));
            content = cursor.getString(cursor.getColumnIndex("content"));
            long createDate = cursor.getLong(cursor.getColumnIndex(NotePad.NoteTable.MODIFY_DATE));
            id =cursor.getInt(cursor.getColumnIndex(NotePad.NoteTable._ID));
            Log.e("test ", "title: " + title);
            Log.e("test ", "content: " + content);
            Log.e("test ", "date: " + createDate);
            Long df = Long.valueOf(createDate);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            now = sdf.format(new Date(Long.parseLong(String.valueOf(df))));
            l = new ListFormat();
            l.title=title;
            l.content=content;
            l.date=now;
            l.id=id;
            mList.add(l);
            cursor.moveToNext();

        }
        cursor.close();
    }
}