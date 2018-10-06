package com.beyond.fly.note3.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by beyond on 17-12-5.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    /**
    *防止SQLiteHelper泄露
    */
    private static MySQLiteHelper mInstance=null;
    public synchronized static MySQLiteHelper getmInstance(Context context,String databaseName){
        if (mInstance==null){
            mInstance=new MySQLiteHelper(context,databaseName);
        }
        return mInstance;
    }

    private MySQLiteHelper(Context context,String databaseName){
        super(context,databaseName,null,1);
    }

    public boolean isTableExist(String tableName){
        boolean result=false;
        if (tableName==null){
            return false;
        }
        SQLiteDatabase database;

        try{
            Cursor cursor;
            database=this.getReadableDatabase();
            String sql="SELECT COUNT(*) AS c FROM sqlite_master where type='table' and name='"+tableName.trim()+"' ";
            cursor=database.rawQuery(sql,null);
            if (cursor.moveToNext()){
                int count=cursor.getInt(0);
                if (count>0){
                    result=true;
                }
            }
            cursor.close();
        }catch (Exception e){
            // TODO: handle exception
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
