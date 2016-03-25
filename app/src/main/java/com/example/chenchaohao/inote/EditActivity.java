package com.example.chenchaohao.inote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends ActionBarActivity {
    private EditText editText;
    private EditText contentNameText;
    String content;
    String contentName;
    private final static String DATABASE_NAME="INoteDB.db";
    private final static int DATABASE_VERSION=1;
    INoteDBHelper helper = INoteDBHelper.getiNoteDBHelper(this,DATABASE_NAME,null,DATABASE_VERSION+2);
    SQLiteDatabase Inotedb = helper.getWritableDatabase();
    static int NoteId = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        editText = (EditText)findViewById(R.id.EditActivity_EditText);
        contentNameText = (EditText)findViewById(R.id.EditContentName);
        Cursor cursorForNoteId = Inotedb.query("Notes",null,null,null,null,null,null);
        cursorForNoteId.moveToLast();
        try{
            NoteId = cursorForNoteId.getInt(cursorForNoteId.getColumnIndex("id"));
        }catch (RuntimeException e){
            NoteId = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.EditActivity_Accept:{
                content = editText.getText().toString();
                contentName = contentNameText.getText().toString();
                Intent intentToMain = getIntent();
                Bundle data = new Bundle();
                data.putString("content",content);
                data.putString("contentName",contentName);
                intentToMain.putExtras(data);
                EditActivity.this.setResult(1,intentToMain);
                EditActivity.this.finish();
                break;
            }
            case R.id.EditActivity_Cancel:{
                final AlertDialog.Builder dialogToConfirm = new AlertDialog.Builder(EditActivity.this);
                dialogToConfirm.setTitle("Discard all the content?");
                dialogToConfirm.setIcon(R.drawable.ic_action_edit_light);
                dialogToConfirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentToMain = getIntent();
                        EditActivity.this.setResult(0, intentToMain);
                        EditActivity.this.finish();
                    }
                });
                dialogToConfirm.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                    }
                });
                dialogToConfirm.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}