package com.example.chenchaohao.inote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.widget.DrawerLayout;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class MainActivity extends ActionBarActivity {

    private Menu menuInMain ;
    private int flag_menuItem_search_to_add_to_acctpt = 1;//0为ADD,1为SEARCH
    private int flag_change_menu = 0;//0为主菜单,1为EDIT时的菜单
    /*
    Database 用于使用 数据库  的 变量
     */
    private final static String DATABASE_NAME="INoteDB.db";
    private final static int DATABASE_VERSION=1;
    INoteDBHelper helper = null;
    SQLiteDatabase Inotedb ;
    static int NoteId = 1;
    static int PictureId = 1;
    /*
    Used for take picture  用于拍照的变量 比如照片路径 名字
     */
    public static final int TAKE_PHOTO =1 ;
    public static  final int CROP_PHOTO =2;
    private static int pictureNumber = 1;
    private  Uri imageUri;
    String currentPictureName = null;
    String currentContentName = null;
    /*
    用于SearchActivity的变量
     */
    private static final int SEARCH_ACTIVITY = 3;
    private static final int EDIT_ACTIVITY = 4;

    /*
     开启/隐藏 键盘的变量
     */
    InputMethodManager imm;
//  各种UI组件变量
    private boolean drawerArrowColor; //“抽屉”
    private DrawerLayout mDrawerLayout;//
    private ListView mDrawerList;
    private com.ikimuhendis.ldrawer.ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private int ContentOrPicture = 1; // 1 => conent , -1 => picture

    com.gc.materialdesign.views.Button take;//照相的按钮
    com.gc.materialdesign.views.Button test_db;
    EditText contentText;
    EditText contentNameText;
    View.OnFocusChangeListener mFocusChangedListener;
    View.OnFocusChangeListener mFocusChangedListenerForNameText;
    ImageView contentPicture;
    int OnDisplaying = -1;
    //主ACTIVITY的OnCreate 第一次加载时就会用这个。

    int focusContentID = -1;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//设置 UI页面 为 activity_main.xml
        android.support.v7.app.ActionBar ab = getSupportActionBar();//得到ActionBar 这三句主要为“抽屉”服务
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //使用单例模式打开数据库，为了在不同的ACTIVITY中使用数据库
        helper = INoteDBHelper.getiNoteDBHelper(this,DATABASE_NAME,null,DATABASE_VERSION+2);
        Inotedb = helper.getWritableDatabase();//数据库

        //这几句 是 查询数据库，获得里面的最新的记录的 ID 号，存为 NoteID ，那么新拍笔记的ID号为 NoyeID+1
        Cursor cursorForNoteId = Inotedb.query("Notes",null,null,null,null,null,null);
        cursorForNoteId.moveToLast();
        try{
            NoteId = cursorForNoteId.getInt(cursorForNoteId.getColumnIndex("id"));
        }catch (RuntimeException e){
            NoteId = 0;
        }

        Cursor cursorForNoteId2 = Inotedb.query("Images",null,null,null,null,null,null);
        cursorForNoteId2.moveToLast();
        try{
            PictureId = cursorForNoteId.getInt(cursorForNoteId2.getColumnIndex("id"));
        }catch (RuntimeException e){
            PictureId = 0;
        }


        //想要得到当前页面是否正在编辑的EditText，实际上就是，如何获得或捕获EditText的焦点（的事件），使用OnFocusChangeListener
        mFocusChangedListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    flag_menuItem_search_to_add_to_acctpt = 3;
                    invalidateOptionsMenu();
                    onPrepareOptionsMenu(menuInMain);
                }else{
                    //失去焦点时，保存文本。
                    SaveChangedText(focusContentID,contentText.getText().toString());
                }
            }
        };
        mFocusChangedListenerForNameText = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    flag_menuItem_search_to_add_to_acctpt = 3;
                    invalidateOptionsMenu();
                    onPrepareOptionsMenu(menuInMain);
                }else{
                    //失去焦点时，保存文本。
                    SaveChangedName(focusContentID, contentNameText.getText().toString());
                }
            }
        };
        contentPicture = (ImageView)findViewById(R.id.image);
        //得到EditText
        contentText = (EditText)findViewById(R.id.contentText);
        contentText.setOnFocusChangeListener(mFocusChangedListener);//为EditText添加获得焦点时的事件
        contentNameText = (EditText)findViewById(R.id.contentName);
        contentNameText.setOnFocusChangeListener(mFocusChangedListenerForNameText);

        //这些都是设置“抽屉”的代码，阅读时可跳过，不必深究。
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };

        mDrawerToggle = new com.ikimuhendis.ldrawer.ActionBarDrawerToggle(this,mDrawerLayout,drawerArrow,R.string.drawer_open,R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                flag_menuItem_search_to_add_to_acctpt = 1;
                invalidateOptionsMenu();
                onPrepareOptionsMenu(menuInMain);
                mDrawerLayout.closeDrawer(mDrawerList);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    ContentOrPicture = -ContentOrPicture;
                    updateDrawer();
                    return;
                }
                DisplayContentOnItemtClick(position);/*position+1*/
                flag_menuItem_search_to_add_to_acctpt = 1;
                invalidateOptionsMenu();
                onPrepareOptionsMenu(menuInMain);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
        mDrawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if(position == 0){
                    return false;
                }
                else{
                    final AlertDialog.Builder dialogToRemove = new AlertDialog.Builder(MainActivity.this);
                    if(ContentOrPicture == 1){
                        dialogToRemove.setTitle("Remove this Note?");
                    }
                    else{
                        dialogToRemove.setTitle("Remove this Image?");
                    }
                    dialogToRemove.setIcon(R.drawable.ic_action_camera_light);
                    dialogToRemove.setCancelable(true);
                    dialogToRemove.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeNote(position);
                        }
                    });
                    dialogToRemove.setNegativeButton("No",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //否定
                        }
                    });
                    dialogToRemove.show();
                    return true;
                }
            }
        });

        //这个函数是自己定义的函数。函数代码在最下面。
        updateDrawer();//放在这里的作用：程序第一次进入，此时先更新一次“抽屉”里的名字，把数据库中已有的名字都先放进去
        //刚进去的页面显示为最近保存的笔记
        if(NoteId != 0){
            Cursor cursor = Inotedb.query("Notes",null,null,null,null,null,null);
            cursor.moveToLast();
            String name = cursor.getString(cursor.getColumnIndex("contentName"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            contentNameText.setText(name);
            contentText.setText(content);
        }else{
            contentNameText.setText("Create New Note!");
        }
    }


    //从 拍照 截图 程序返回 以后的 入口， 在里面将图片转换为文本 并储存在数据库
    @Override
    protected  void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    Intent intentToCrop = new Intent("com.android.camera.action.CROP");
                    intentToCrop.setDataAndType(imageUri,"image/*");
                    intentToCrop.putExtra("scale",true);
                    intentToCrop.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intentToCrop,CROP_PHOTO); // 启动裁剪程序
                }
                break;
            case CROP_PHOTO:
                if(resultCode == RESULT_OK){
                    String recognizedText = ocr();//使用OCR转换图片
                    InsertToDB(imageUri,recognizedText);

                }
                break;
            case SEARCH_ACTIVITY:{
                Cursor searchCursor = null;
                if(resultCode == 3){
                    Bundle a = data.getExtras();
                    String searchData = a.getString("searchData");
                    searchCursor = Inotedb.query("Notes",null,"contentName=?",new String[]{searchData},null,null,null);
                    Cursor searchCursorPictures = Inotedb.query("Images",null,"pictureName=?",new String[]{searchData},null,null,null);
                    if(searchCursor.moveToFirst() ){
                        searchcontent(searchCursor);
                    }
                    else if(searchCursorPictures.moveToFirst()){
                        searchpicture(searchCursorPictures);
                    }else{
                        Toast.makeText(this,"Sorry,No Match Result",Toast.LENGTH_LONG).show();
                    }

                }else if(resultCode == 1){//搜索选择的是笔记内容
                    Bundle a = data.getExtras();
                    String searchID = Integer.toString(a.getInt("SearchId"));
                    searchCursor = Inotedb.query("Notes",null,"id=?",new String[]{searchID},null,null,null);
                    searchcontent(searchCursor);
                }else if(resultCode == 2){//搜索选择的是图片
                    Bundle a = data.getExtras();
                    String searchID = Integer.toString(a.getInt("SearchId"));
                    searchCursor = Inotedb.query("Images",null,"id=?",new String[]{searchID},null,null,null);
                    searchpicture(searchCursor);
                }
                else {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
                break;
            }
            case EDIT_ACTIVITY:{
                if(resultCode == 1) {
                    Bundle a = data.getExtras();
                    String content = a.getString("content");
                    String contentName = a.getString("contentName");
                    final ContentValues values = new ContentValues();
                    //把一条数据保存进数据库
                    values.put("id",++NoteId);
                    values.put("content",content);
                    values.put("contentName",contentName);
                    Inotedb.insert("Notes", null, values);
                    values.clear();
                    Log.e("Insert","Now UpdataDreawer!");
                    updateDrawer();
                    contentNameText.setText(contentName);
                    contentText.setText(content);
                }
                break;
            }
            default:
                break;
        }
    }
    //自己定义的方法，用于将 图片路径 识别出来的文本 保存进数据库的函数
    private void InsertToDB(Uri pictureUri,String contentTexts){
        final ContentValues values = new ContentValues();
        //把一条笔记数据保存进Notes表
        values.put("id",++NoteId);
        values.put("content",contentTexts);
        values.put("contentName",currentContentName);
        Inotedb.insert("Notes",null,values);
        values.clear();
        contentNameText.setText(currentContentName);
        contentText.setText(contentTexts);
        //将图片存进Images表
        final ContentValues values1 = new ContentValues();
        values1.put("id",++PictureId);
        values1.put("pictureUri",pictureUri.toString());
        values1.put("pictureName",currentPictureName);
        Inotedb.insert("Images",null,values1);
        Toast.makeText(this, "Save Note Succeed!", Toast.LENGTH_SHORT).
                show();//提示用户保存成功
        updateDrawer();

    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current camera index.

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(flag_change_menu == 0){
            menuInMain = menu;
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        else{
            menuInMain = menu;
            getMenuInflater().inflate(R.menu.menu_main_edit,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        MenuItem item = menu.findItem(R.id.action_addNoteByInput_or_search_or_accept);
        if(flag_menuItem_search_to_add_to_acctpt == 1){
            item.setIcon(R.drawable.ic_action_edit);
        }else if(flag_menuItem_search_to_add_to_acctpt == 2){
            item.setIcon(R.drawable.ic_action_search);
        }else {
            item.setIcon(R.drawable.ic_action_accept);
        }
        /*
        if(flag_menuItem_search_to_add_to_acctpt == 0){
            item.setIcon(R.drawable.ic_action_search);
            flag_menuItem_search_to_add_to_acctpt = 1;
        }else{
            item.setIcon(R.drawable.ic_action_edit);
            flag_menuItem_search_to_add_to_acctpt = 0;
        }*/
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int ItemID = item.getItemId();
        switch (ItemID){
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                    flag_menuItem_search_to_add_to_acctpt = 1;
                } else {
                    flag_menuItem_search_to_add_to_acctpt = 2;
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                invalidateOptionsMenu();
                onPrepareOptionsMenu(menuInMain);
                break;
            case R.id.action_addNoteByInput_or_search_or_accept: {
                if(flag_menuItem_search_to_add_to_acctpt == 1){ //edit
                    Intent mainToEditActivity = new Intent(MainActivity.this,EditActivity.class);
                    startActivityForResult(mainToEditActivity,EDIT_ACTIVITY);
                }
                else if(flag_menuItem_search_to_add_to_acctpt == 2){
                    Intent intentToSearchActivity = new Intent(MainActivity.this,SearchActivity.class);
                    startActivityForResult(intentToSearchActivity,SEARCH_ACTIVITY);
                }else { //accept
                    flag_menuItem_search_to_add_to_acctpt = 1;
                    invalidateOptionsMenu();
                    onPrepareOptionsMenu(menuInMain);
                    contentText.clearFocus();//失去焦点
                    contentNameText.clearFocus();
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                break;
            }

            case R.id.action_addNoteByCamera: {
                final File outputImage = new File(Environment.getExternalStorageDirectory(), "/" + pictureNumber++ + ".jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                final EditText et2 = new EditText(MainActivity.this);
                final AlertDialog.Builder dialogToGetContentName = new AlertDialog.Builder(MainActivity.this);
                dialogToGetContentName.setTitle("Please input the Note name");
                dialogToGetContentName.setIcon(R.drawable.ic_action_camera_light);
                dialogToGetContentName.setView(et2);
                dialogToGetContentName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //点击OK了，存下名字，开始拍照
                        currentContentName = et2.getText().toString();
                        imageUri = Uri.fromFile(outputImage);
                        Intent intentToCapture = new Intent("android.media.action.IMAGE_CAPTURE");
                        intentToCapture.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                        startActivityForResult(intentToCapture,TAKE_PHOTO);
                        imm.showSoftInput(et2, 0);
                    }
                });
                final EditText et = new EditText(MainActivity.this);
                AlertDialog.Builder dialogToGetPictureName = new AlertDialog.Builder(MainActivity.this);
                dialogToGetPictureName.setTitle("Please input the Picture name");
                dialogToGetPictureName.setIcon(R.drawable.ic_action_camera_light);
                dialogToGetPictureName.setView(et);
                dialogToGetPictureName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //点击OK了，存下名字，显示第二个框
                        currentPictureName = et.getText().toString();
                        dialogToGetContentName.show();//弹框

                    }
                });
                dialogToGetPictureName.setNegativeButton("Back",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialogToGetPictureName.show();//弹框
                break;
            }

        }
                return super.onOptionsItemSelected(item);

    }



    @Override
    public void onPause() {

        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    /*
     2015/11/10 23:13 加载笔记名称或内容名称
     2015/11/11 13:00 根据状态查找Images表
     */
    private String[] getContentNameFromDB(){

        Vector<String> a = new Vector<String>();
        if(ContentOrPicture == 1){//查Notes表获得笔记内容
            Cursor cursor = Inotedb.query("Notes",null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do{
                    String contentName = cursor.getString(cursor.getColumnIndex("contentName"));
                    a.add(contentName);
                }while(cursor.moveToNext());
            }
            cursor.close();
        }
        else{//查Images表，获得图片名称
            Cursor cursor = Inotedb.query("Images",null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do{
                    String pictureName = cursor.getString(cursor.getColumnIndex("pictureName"));
                        a.add(pictureName);
                }while(cursor.moveToNext());
            }
            cursor.close();
        }
        String[] contentNames = new String[a.size()];
        int i=0;
        for(Iterator<String> it = a.iterator();it.hasNext(); ){
            contentNames[i++] = it.next();
        }
        return contentNames;
    }
    //使用 OCR 把照片 转化为 文字。
    protected String ocr(){
        TessBaseAPI baseApi = new TessBaseAPI();
        Bitmap bitmap;
        String recognizedText = null;
        baseApi.init(Environment.getExternalStorageDirectory()+"/tesseract", "eng");
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            baseApi.setImage(bitmap);
            recognizedText = baseApi.getUTF8Text();
            baseApi.end();
            Log.e("success", recognizedText);

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        return  recognizedText;
    }

    //更新“抽屉”， 比如每次把数据 保存进数据库之x后的同时 要调用这个方法，因为又有新的笔记，“抽屉”里的项需要增加这个新笔记名字
    private void updateDrawer(){
        String[] contentNames = getContentNameFromDB();
        String[] items = new String[contentNames.length+1];
        System.arraycopy(contentNames,0,items,1,contentNames.length);
        if(ContentOrPicture == 1){
            items[0] = "Show Images";
        }else{
            items[0] = "Show Notes";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,android.R.id.text1,items);
        mDrawerList.setAdapter(adapter);
    }

    private void DisplayContentOnItemtClick(int position){
       /*Cursor cursor = Inotedb.query("Notes",new String[]{"id","content"},"id=?",new String[]{id},null,null,null);
       String content =  cursor.getString(cursor.getColumnIndex("content"));
       contentText.setText(content);*/
        OnDisplaying = position;
        contentPicture.setImageBitmap(null);
        contentText.setText(null);
        if(ContentOrPicture == 1){
            Cursor AlwaysCursor = Inotedb.query("Notes",null,null,null,null,null,null);
            if(AlwaysCursor.moveToFirst()){
                do{
                    int id = AlwaysCursor.getInt(AlwaysCursor.getColumnIndex("id"));
                    if(id == position){
                        String content = AlwaysCursor.getString(AlwaysCursor.getColumnIndex("content"));
                        String title = AlwaysCursor.getString(AlwaysCursor.getColumnIndex("contentName"));
                        contentText.setText(content);
                        contentNameText.setText(title);
                        focusContentID = id;
                        break;
                    }
                }while(AlwaysCursor.moveToNext());
            }
        }
        else{
            Cursor AlwaysCursor = Inotedb.query("Images",null,null,null,null,null,null);
            if(AlwaysCursor.moveToFirst()){
                do{
                    int id = AlwaysCursor.getInt(AlwaysCursor.getColumnIndex("id"));
                    if(id == position){
                        String title = AlwaysCursor.getString(AlwaysCursor.getColumnIndex("pictureName"));
                        String imageUri_s = AlwaysCursor.getString(AlwaysCursor.getColumnIndex("pictureUri"));
                        Uri uri = Uri.parse(imageUri_s);
                        try{
                            Bitmap bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                            contentPicture.setImageBitmap(bm);
                        }catch(FileNotFoundException e){
                            Log.e("error","No picture");
                        }
                        contentNameText.setText(title);
                        focusContentID = id;
                        break;
                    }
                }while(AlwaysCursor.moveToNext());
            }
        }
      }
    /*
    删除笔记
     */
    private void removeNote(int position){
        String ID = Integer.toString(position);
        if(ContentOrPicture == 1){
            //删笔记
            Inotedb.delete("Notes","id = ?",new String[]{ID});
            updateIDinDB();
            updateDrawer();
        }
        else{
            Inotedb.delete("Images","id = ?",new String[]{ID});
            updateIDinDB();
            updateDrawer();
            }


        if(OnDisplaying == position){ //如果删除的内容和正在展示的内容一样，就清空展示的内容
            contentNameText.setText("");
            contentText.setText("");
            contentPicture.setImageBitmap(null);
        }

        Toast.makeText(this,"Delete Success!",Toast.LENGTH_LONG).show();
    }
    /*
    更新ID ,记得修改全局变量NOTEID ,PictureID
    */
    private void updateIDinDB(){
        int i = 1;
        if(ContentOrPicture == 1){
            Cursor AlwaysCursor = Inotedb.query("Notes",null,null,null,null,null,null);
            if(AlwaysCursor.moveToFirst()){
                do{
                    String contentName =  AlwaysCursor.getString(AlwaysCursor.getColumnIndex("contentName"));
                    ContentValues values = new ContentValues();
                    values.put("id",i);
                    i=i+1;
                    Inotedb.update("Notes",values,"contentName = ?",new String[]{contentName});
                }while(AlwaysCursor.moveToNext());
            }
            NoteId = i;
        }else{
            Cursor AlwaysCursor = Inotedb.query("Images",null,null,null,null,null,null);
            if(AlwaysCursor.moveToFirst()){
                do{
                    String pictureName =  AlwaysCursor.getString(AlwaysCursor.getColumnIndex("pictureName"));
                    ContentValues values = new ContentValues();
                    values.put("id",i);
                    i=i+1;
                    Inotedb.update("Notes",values,"pictureName = ?",new String[]{pictureName});
                }while(AlwaysCursor.moveToNext());
            }
            PictureId = i;
        }

    }
    /*
     根据用户输入的新笔记内容，更新数据库
     */
    private void SaveChangedText(int id,String NewText) {
        ContentValues values = new ContentValues();
        String ID = Integer.toString(id);
        values.put("content", NewText);
        Inotedb.update("Notes", values, "id=?", new String[]{ID});
    }
    /*
     根据用户输入的新笔记标题，更新数据库,,更新完以后要更新抽屉。
     */
    private void SaveChangedName(int id,String NewName) {
        ContentValues values = new ContentValues();
        String ID = Integer.toString(id);
        if(ContentOrPicture == 1){
            values.put("contentName",NewName);
        }else {
            values.put("pictureName", NewName);
        }
        Inotedb.update("Notes",values,"id=?",new String[]{ID});
        updateDrawer();
    }
    private void searchcontent(Cursor searchCursor){
        mDrawerLayout.closeDrawer(mDrawerList);
        flag_menuItem_search_to_add_to_acctpt = 1;//将菜单变成edit
        contentNameText.setText(null);
        contentPicture.setImageBitmap(null);
        contentText.setText(null);
        invalidateOptionsMenu();
        onPrepareOptionsMenu(menuInMain);
        if(searchCursor.moveToFirst()){
            String searchResult = searchCursor.getString(searchCursor.getColumnIndex("content"));
            String title = searchCursor.getString(searchCursor.getColumnIndex("contentName"));
            int id = searchCursor.getInt(searchCursor.getColumnIndex("id"));
            if(searchResult != null){
                contentText.setText(searchResult);
                contentNameText.setText(title);
                OnDisplaying = id;
            }
        }
    }
    private void searchpicture(Cursor searchCursor) {
        mDrawerLayout.closeDrawer(mDrawerList);
        flag_menuItem_search_to_add_to_acctpt = 1;//将菜单变成edit
        contentNameText.setText(null);
        contentPicture.setImageBitmap(null);
        contentText.setText(null);
        invalidateOptionsMenu();
        onPrepareOptionsMenu(menuInMain);
        if (searchCursor.moveToFirst()) {
            String pictureUri = searchCursor.getString(searchCursor.getColumnIndex("pictureUri"));
            String title = searchCursor.getString(searchCursor.getColumnIndex("pictureName"));
            int id = searchCursor.getInt(searchCursor.getColumnIndex("id"));
            Uri uri = Uri.parse(pictureUri);
            try{
                Bitmap bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                contentPicture.setImageBitmap(bm);
            }catch(FileNotFoundException e){
                Log.e("error","No picture");
            }
            contentNameText.setText(title);
            focusContentID = id;
        }
    }
}