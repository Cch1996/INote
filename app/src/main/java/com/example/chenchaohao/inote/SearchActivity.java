package com.example.chenchaohao.inote;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Handler;

/*  *打开数据库，存入图片名字和笔记名
/   *搜索，打字时会显示出名字匹配的项
/   *点击选择一个项，返回MainActivity显示。
*/
public class SearchActivity extends ActionBarActivity implements  SearchView.OnQueryTextListener{

    private final static String DATABASE_NAME="INoteDB.db";
    private final static int DATABASE_VERSION=1;
    INoteDBHelper helper = INoteDBHelper.getiNoteDBHelper(this,DATABASE_NAME,null,DATABASE_VERSION+2);
    SQLiteDatabase Inotedb = helper.getWritableDatabase();

    InputMethodManager imm;
    private SearchView searchView;
    private ListView listView;
    private SimpleCursorAdapter mAdapter;
    private SimpleCursorAdapter mAdapterPicture;
    private Cursor cursor;
    private Cursor cursorPicture;
    private Button cancel_button;
    private ListView listViewPicture;

    private ShimmerTextView shimer_tv ;
    private ShimmerTextView shimer_tv2;
    private Shimmer shimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        shimer_tv = (ShimmerTextView)findViewById(R.id.shimmer_tv);
        shimer_tv2 = (ShimmerTextView)findViewById(R.id.shimmer_tv2);
        shimmer = new Shimmer();
        shimmer.setRepeatCount(100)
                .setDuration(1500)
                .setStartDelay(300)
                .setDirection(Shimmer.ANIMATION_DIRECTION_RTL).setAnimatorListener(shimmer.getAnimatorListener());
        shimmer.start(shimer_tv);
        shimmer.start(shimer_tv2);

       // String[] names = ReadNamesFromDB();
        cancel_button = (Button)findViewById(R.id.back_to_main);
        listView = (ListView)findViewById(R.id.list_view);
        listViewPicture = (ListView)findViewById(R.id.list_view_picture);
        searchView = (SearchView) findViewById(R.id.search_view);
        cursor = Inotedb.query("Notes",new String[] {"id as _id","contentName"},null,null,null,null,null);
        mAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursor,new String[] {"contentName"}, new int[] { android.R.id.text1},0);
        listView.setAdapter(mAdapter);
        listView.setTextFilterEnabled(true);

        cursorPicture = Inotedb.query("Images",new String[]{"id as _id","pictureName"},null,null,null,null,null);
        mAdapterPicture = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorPicture,new String[]{"pictureName"},new int[]{android.R.id.text1},0);
        listViewPicture.setAdapter(mAdapterPicture);
        listViewPicture.setTextFilterEnabled(true);

        // 设置该SearchView默认是否自动缩小为图标
        searchView.setIconifiedByDefault(false);
        // 为该SearchView组件设置事件监听器
        searchView.setOnQueryTextListener(this);
        // 设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        // 设置该SearchView内默认显示的提示文本
        searchView.setQueryHint("Photo Name/Note Name");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               //Toast.makeText(SearchActivity.this,"从ListView中选择",Toast.LENGTH_LONG).show();
               Intent intentToMian = getIntent();
               Bundle data = new Bundle();
               data.putInt("SearchId",(int)id);
               intentToMian.putExtras(data);
               SearchActivity.this.setResult(1,intentToMian);
               SearchActivity.this.finish();
            }
        });
        listViewPicture.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(SearchActivity.this, "从ListView中选择", Toast.LENGTH_LONG).show();
                Intent intentToMain = getIntent();
                Bundle data = new Bundle();
                data.putInt("SearchId",(int)id);
                intentToMain.putExtras(data);
                SearchActivity.this.setResult(2,intentToMain);
                SearchActivity.this.finish();
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToMain = getIntent();
                SearchActivity.this.setResult(-1,intentToMain);
                SearchActivity.this.finish();
            }
        });
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onQueryTextChange(String newText){
        String selection = "contentName LIKE '%" + newText + "%'" ; //" OR " +  "pictureName LIKE '%" + newText +"%' ";
        String selectionpicture = "pictureName LIKE '%"+ newText + "%'";
        cursor = Inotedb.query("Notes",new String[] {"id as _id","contentName"},selection,null,null,null,null,null);
        cursorPicture = Inotedb.query("Images",new String[]{"id as _id","pictureName"},selectionpicture,null,null,null,null,null);
        mAdapterPicture.swapCursor(cursorPicture);
        mAdapter.swapCursor(cursor);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // 实际应用中应该在该方法内执行实际查询
        // 此处仅使用Toast显示用户输入的查询内容
        //Toast.makeText(this, "您的选择是:" + query, Toast.LENGTH_SHORT).show();
        Intent intentToMain = getIntent();
        Bundle data = new Bundle();
        data.putString("searchData",query);
        intentToMain.putExtras(data);
        SearchActivity.this.setResult(3,intentToMain);
        SearchActivity.this.finish();
        return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
    }

    private String[] ReadNamesFromDB(){
        Vector<String> namesVector = new Vector<String>();

        Cursor cursor = Inotedb.query("Notes",new String[] {"id as _id","pictureName","contentName"},null,null,null,null,null);

        if(cursor.moveToFirst()){
            do{
                namesVector.add(cursor.getString(cursor.getColumnIndex("pictureName")));
                namesVector.add(cursor.getString(cursor.getColumnIndex("contentName")));
            }while(cursor.moveToNext());
        }
        String[] names  = new String[namesVector.size()];
        int i=0;
        for(Iterator<String> it = namesVector.iterator();it.hasNext(); ){

            names[i++] = it.next();
        }
        return names;
    }
}
