package com.example.chenchaohao.inote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chenchaohao on 15/4/19.
 */
public class INoteDBHelper extends SQLiteOpenHelper {
    private Context mContext;
    private static String DATABASE_NAME="INoteDB";
    private static String DATABSE_TABLE="Notes";
    private static String DATABSE_TABLE2="Images";
    private static int DATABASE_VERSION=1;
    public static final String KEY_ID="id";
    public static final String PICTUREURI="pictureUri";
    private static final String CREATA_TABLE_PICTURE_CONTENT="create table Notes ("+"id integer,content text,contentName text)";
    private static final String CREATA_TABLE_PICTURE ="create table Images (" + "id integer,pictureUri text,pictureName text)";
    static private INoteDBHelper iNoteDBHelper  = null;

    private INoteDBHelper(Context context,String name,SQLiteDatabase.CursorFactory cursorFactory,int version){
        super(context,name,cursorFactory,version);
        mContext=context;
    }
    public static INoteDBHelper getiNoteDBHelper(Context context,String name,SQLiteDatabase.CursorFactory cursorFactory,int version){
        if(iNoteDBHelper == null){
            iNoteDBHelper = new INoteDBHelper(context,name,cursorFactory,version);
            return iNoteDBHelper;
        }
        else{
            return  iNoteDBHelper;
        }
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATA_TABLE_PICTURE_CONTENT);
        db.execSQL(CREATA_TABLE_PICTURE);
    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+DATABSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+DATABSE_TABLE2);
        onCreate(db);
    }
}
