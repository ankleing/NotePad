# NotePad
由于由于recyclerview只支持高版本gradle，故重写项目，以实现增删改查，以及
基本功能时间戳和查询笔记，美化UI，特色功能卡片式布局，随时间切换深色背景
## 一基本功能实现
   + 1 **添加时间戳**
   + 创建列表投影
```
   private static HashMap<String,String> NotesProjectionMap;
    static {
        NotesProjectionMap=new HashMap<String,String>();
        NotesProjectionMap.put(NotePad.NoteTable._ID,NotePad.NoteTable._ID);
        NotesProjectionMap.put(NotePad.NoteTable.NOTE_CONTENT,NotePad.NoteTable.NOTE_CONTENT);
        NotesProjectionMap.put(NotePad.NoteTable.NOTE_TITLE,NotePad.NoteTable.NOTE_TITLE);
        NotesProjectionMap.put(NotePad.NoteTable.CREATE_DATE,NotePad.NoteTable.CREATE_DATE);
        NotesProjectionMap.put(NotePad.NoteTable.MODIFY_DATE,NotePad.NoteTable.MODIFY_DATE);
    }
```
+ 在契约类中为创建表格的sql语句添加modify词条
```
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
```    
+ 格式化输出事件戳为日期格式
```
  private void query(){
       /* 利用ContentResolver读取数据库中存放的数据*/
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
            /* 利用SimpleDateFormat 将从数据库中提取出的时间戳格式化为日期格式
            * 并存放进List中*/
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
```
 +  修改时获得修改时的时间
```
            /* 根据Intent传来的Action判断是否为编辑状态*/
   else if (mState==STATE_EDIT||!(localTitle.equals(title.getText().toString())) || !(localNote.equals(note.getText().toString())) ) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NotePad.NoteTable.NOTE_TITLE, title.getText().toString());
            contentValues.put(NotePad.NoteTable.NOTE_CONTENT, note.getText().toString());
            /* 修改时获得当前时间并传给ContentProvider*/
            modify = System.currentTimeMillis();
            contentValues.put(NotePad.NoteTable.MODIFY_DATE, modify);
            this.getContentResolver().update(getIntent().getData(), contentValues, null, null);

        }
```
   + 2 **实现搜索笔记功能**
   + 监听回车键，并在按下回车键是接受EditText的数据，
      且对列表中的数据进行查询，而非查询数据库以节省资源
   ```
   /*定位到xml中的EditText,并且检测回车键 ，一旦输入回车则调用Show2*/
        EditText et = findViewById(R.id.edtx);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                /*检测回车键是否按下 */
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = v.getText().toString().trim();
                    Show2();
                    return true;
                }
                return false;
            }
        });
        public void Show2(){
        EditText et = findViewById(R.id.edtx);
        dList.clear();
        /*获取搜索框内容*/
        String s =et.getText().toString();
        mRecyclerView = this.findViewById(R.id.recyclerView);
        /*设置内容为瀑布流输出*/
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        lm.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        /*遍历mList查询要搜索的数据*/
        for(int i=0;i<mList.size();i++){
            if(mList.get(i).title.equals(s)){
                dList.add(mList.get(i));
            }
        }
        mList.clear();
        mList = dList;
        /*逆序输出mList，让更新的项目在前，更符合人的直觉*/
        Collections.reverse(mList);
        cardAdapter =new CardAdapter(this, mList );
        /*设置点击事件，一旦点击则进入修改界面*/
        cardAdapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Uri uri = ContentUris.withAppendedId(getIntent().getData(),mList.get(position).id);
                startActivity(new Intent(Intent.ACTION_EDIT,uri));
            }
        });
        /*设置点击事件，一旦长按则进行删除*/
        cardAdapter.setOnItemLongClickListener(new CardAdapter.OnItemLongClickListener() {
            @Override
            public void onClick(int position) {
                delete(position);
            }
        });
        mRecyclerView.setAdapter(cardAdapter);
    }
   ```
   + 利用android:maxLines="1"禁止回车使用换行，防止误监听回车
   ```
   <EditText
            android:id="@+id/edtx"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@drawable/rounded_corner_box"
            android:hint="@string/edittext_hint"
            android:textColor="#fff"
            android:imeOptions="actionSearch"
            android:singleLine="true"
            android:maxLines="1"
            >
        </EditText>
   ```
   
 ## 二附加功能实现
   + 1 **卡片式布局**
   借助RecyclerView将笔记以卡片的央视进行瀑布流输出
   ```
        public void Show(){
        query();
        mRecyclerView = this.findViewById(R.id.recyclerView);
        /*设置内容为瀑布流输出*/
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        lm.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        /*逆序输出mList，让更新的项目在前，更符合人的直觉*/
        Collections.reverse(mList);
        cardAdapter =new CardAdapter(this, mList );
        /*设置点击事件，一旦点击则进入修改界面*/
        cardAdapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Uri uri = ContentUris.withAppendedId(getIntent().getData(),mList.get(position).id);
                startActivity(new Intent(Intent.ACTION_EDIT,uri));
                Toast.makeText(NoteList.this,"click " + position, Toast.LENGTH_SHORT).show();
            }
        });
         /*设置点击事件，一旦长按则进行删除*/
        cardAdapter.setOnItemLongClickListener(new CardAdapter.OnItemLongClickListener() {
            @Override
            public void onClick(int position) {
                delete(position);
                Toast.makeText(NoteList.this,"longclick " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(cardAdapter);
    }
   ```
   + 在drawable中设置卡片样式
   ```
    <?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#80858175"/>
    <stroke android:width="1dp" android:color="#fefefe"/>
    <corners android:radius="20dp"/>
    <padding
        android:top="8dp"
        android:right="8dp"
        android:bottom="8dp"
        android:left="8dp"
        />
</shape>
   ```
   ![主界面](https://github.com/ankleing/NotePad/tree/main/image/image1.png)
   + 2 **根据时间切换深色背景**
   在夜晚切换深色背景减缓人眼疲劳
   主页面切换深色背景
```
   LinearLayout ll =findViewById(R.id.layout);
        /*获取时间戳*/
        Long df = Long.valueOf(System.currentTimeMillis());
        SimpleDateFormat sdf=new SimpleDateFormat("HH");
        String now = sdf.format(new Date(Long.parseLong(String.valueOf(df))));
        /*获取当前时间并判断是否到夜晚*/
        final int t =Integer.parseInt(now);
        if(t<21){
            ll.setBackgroundResource(R.drawable.day);
        }else {
            ll.setBackgroundResource(R.drawable.night);
        }
 ```
    ![主界面](https://github.com/ankleing/NotePad/tree/main/image/image3.png)
  编辑界面切换深色背景
```
 ConstraintLayout ll =findViewById(R.id.consly);
        Long df = Long.valueOf(System.currentTimeMillis());
        SimpleDateFormat sdf=new SimpleDateFormat("HH");
        String now = sdf.format(new Date(Long.parseLong(String.valueOf(df))));
        Log.e("TIME",now);
        final int t =Integer.parseInt(now);
        if(t<21){
            ll.setBackgroundResource(R.drawable.day);
        }else {
            ll.setBackgroundResource(R.drawable.night);
        }
```   
   ![主界面](https://github.com/ankleing/NotePad/tree/main/image/image4.png)
   + 3 **美化UI**
   使卡片变得透明以及增加圆角
```
   <shape xmlns:android="http://schemas.android.com/apk/res/android">
   <!-- 以#80开头的颜色都为透明-->
    <solid android:color="#80858175"/>
    <stroke android:width="1dp" android:color="#fefefe"/>
    <corners android:radius="20dp"/>
    <padding
        android:top="8dp"
        android:right="8dp"
        android:bottom="8dp"
        android:left="8dp"
        />
</shape>
```
  分开标题栏和内容栏，方便输入内容，让人增大EditText字体，并将其改成高亮橙色让人更容易看到
```
<EditText
        android:id="@+id/editorTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toTopOf="@+id/editorNote"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.145"
        android:textColor="@android:color/holo_orange_dark"
        android:textSize="30dp"
        android:background="@drawable/rounded_corner_box"
        android:hint="@string/edittext_hint2"
        tools:ignore="MissingConstraints"
        android:theme="@style/MyEditText"/>

    <EditText
        android:id="@+id/editorNote"
        android:layout_width="match_parent"
        android:layout_height="330dp"
        android:layout_marginBottom="156dp"
        android:background="@drawable/rounded_corner_box"
        android:hint="@string/edittext_hint3"
        android:textColor="@android:color/holo_orange_dark"
        android:theme="@style/MyEditText"
        app:layout_constraintBottom_toTopOf="@+id/button"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.0"
        app:layout_constraintStart_toStartOf="parent"
        tools:ignore="MissingConstraints" />
```        
   ![主界面](https://github.com/ankleing/NotePad/tree/main/image/image2.png)
