package com.beyond.fly.note3.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;

import com.beyond.fly.note3.SyncService;

import java.util.ArrayList;

/**
 * Created by beyond on 17-12-5.
 */

public class MyDatabaseUtils {

    private SQLiteDatabase database;
    public static String databaseName="Note.db";
    public static String tableName="note_table";
    private HttpUtils httpUtils;

    public int deleteId=-1;
    private int count=0;

    private static MyDatabaseUtils myDatabaseUtilsInstance=null;
    public synchronized static MyDatabaseUtils getMyDatabaseUtilsInstance(Context context){
        if (myDatabaseUtilsInstance==null){
            myDatabaseUtilsInstance=new MyDatabaseUtils(context);
        }
        return myDatabaseUtilsInstance;
    }

    public MyDatabaseUtils(Context context){
        database=MySQLiteHelper.getmInstance(context,databaseName).getWritableDatabase();
        httpUtils=new HttpUtils();
    }

    //从数据库取出数据
    public ArrayList<MyHashmap> getDataFromDatabase(String tableName){

        ArrayList<MyHashmap> arrayList=new ArrayList<>();
        Cursor cursor=database.rawQuery("SELECT * FROM "+tableName+" ORDER BY _id DESC;",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            MyHashmap hashMap=new MyHashmap();
            hashMap.put("_id",cursor.getInt(0));
            hashMap.put("title",cursor.getString(1));
            hashMap.put("content",cursor.getString(2));
            hashMap.put("dates",cursor.getLong(3));
            hashMap.put("remind_date",cursor.getLong(4));
            hashMap.put("color",cursor.getInt(5));
            hashMap.put("calendar_event_id",cursor.getLong(6));
            hashMap.put("type",cursor.getString(7));
            hashMap.put("creator",cursor.getString(8));
            arrayList.add(hashMap);
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    //向数据库中添加数据
    public void insertDataToDatabase(String tableName,String[] keys,Object[] values){
        ContentValues contentValues=new ContentValues();
        for (int i=0;i<keys.length;i++) {
            contentValues.put(keys[i],values[i].toString());
        }
        database.insert(tableName,null,contentValues);

        //onlineInsert
        //new CreateOrChangeOnline(keys,values,-1,1).execute();
    }

    //向数据库中更新数据
    public void updateDataToDatabase(String tableName,int _id,String[] keys,Object[] values){
        ContentValues contentValues=new ContentValues();
        for (int i=0;i<keys.length;i++) {
            contentValues.put(keys[i],values[i].toString());
        }
        database.update(tableName,contentValues,"_id=?",new String[]{String.valueOf(_id)});

        if (SyncService.isNetworkAvailable) {
            //onlineUpdate
            new CreateOrChangeOnline(keys, values, _id, 2).execute();
        }
    }

    //删除数据库中某条数据
    public void deleteDataFromDatabase(String tableName, String key,Object value){
        String deleteData_sql="DELETE FROM "+tableName+" WHERE "+key+"="+value+";";
        database.execSQL(deleteData_sql);
        System.out.println("deleteExx");

        if (SyncService.isNetworkAvailable) {
            //onlineDelete
            new CreateOrChangeOnline(new String[]{key}, new Object[]{value}, -1, 3).execute();
            deleteId = Integer.parseInt(value.toString());
        }
    }

    //删除本地数据库中某条数据而不删除网上的，同步专用
    public void deleteDataOnlyFromLocalDatabase(String tableName, String key,Object value){
        String deleteData_sql="DELETE FROM "+tableName+" WHERE "+key+"="+value+";";
        database.execSQL(deleteData_sql);
        System.out.println("deleteExx");
    }

    //关闭数据库
    public void closeDatabase(){
        database.close();
    }

    class CreateOrChangeOnline extends AsyncTask<String,String,String>{
        private String[] keys;
        private Object[] values;
        private String onlineTableName;
        private int valueOfWhere;
        private int doWhat;
        private CreateOrChangeOnline(String[] keys,Object[] values,int valueOfWhere,int doWhat){
            this.keys=keys;
            this.values=values;
            this.onlineTableName =HttpUtils.onlineTableName;
            this.valueOfWhere=valueOfWhere;
            this.doWhat=doWhat;
        }
        private void createOnline(String onlineTableName,String[] keys,Object[] values){
            String[] newKeys=new String[10];
            Object[] newValues=new Object[10];
            for (int i = 0; i <keys.length ; i++) {
                newKeys[i]=keys[i];
                newValues[i]=values[i];
            }
            newKeys[9]="tablename";
            newValues[9]=onlineTableName;
            httpUtils.createWithHttpUrlConnection(newKeys, newValues);
        }
        private void updateOnline(String onlineTableName,String where,int valueOfWhere,String[] keys,Object[] values){
            String[] newKeys=new String[10];
            Object[] newValues=new Object[10];
            for (int i = 0; i <keys.length ; i++) {
                newKeys[i]=keys[i];
                newValues[i]=values[i];
            }

            newKeys[8]="_id";
            newValues[8]=valueOfWhere;

            newKeys[9]="tablename";
            newValues[9]=onlineTableName;
            httpUtils.updateItemWithHttpUrlConnection(newKeys,newValues);
        }
        private void deleteOnline(String onlineTableName,String[] keys,Object[] values){
            String[] newKeys=new String[2];
            Object[] newValues=new Object[2];
            for (int i = 0; i <keys.length ; i++) {
                newKeys[i]=keys[i];
                newValues[i]=values[i];
            }

            newKeys[1]="tablename";
            newValues[1]=onlineTableName;
            httpUtils.deleteItemWithHttpUrlConnection(newKeys,newValues);
        }
        @Override
        protected String doInBackground(String... strings) {
            if (doWhat==1) {
                createOnline(onlineTableName, keys, values);
            }else if (doWhat==2){
                updateOnline(onlineTableName,"_id=?",valueOfWhere,keys,values);
            }else if (doWhat==3){
                deleteOnline(onlineTableName,keys,values);
            }
            if (!httpUtils.createSuccessful.equals("1")&&!httpUtils.updateSuccessful.equals("1")&&!httpUtils.deleteSuccessful.equals("1")){
                Handler handler=new Handler();
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        if (count<6) {
                            doInBackground();
                            count++;
                        }else {
                            count=0;
                        }
                    }
                };
                handler.post(runnable);
            }else {
                httpUtils.createSuccessful="0";
                httpUtils.updateSuccessful="0";
                httpUtils.deleteSuccessful="0";
            }
            return null;
        }
    }
}
