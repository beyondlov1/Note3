package com.beyond.fly.note3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.beyond.fly.note3.Utils.HttpUtils;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;

/**
 * Created by beyond on 17-12-15.
 */

public class BlankActivity extends Activity {
    public static String SEND="android.intent.action.SEND";
    private Context context;
    private MyDatabaseUtils myDatabaseUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
        getTableName();

        final Intent intent=getIntent();
        if (("text/plain").equals(intent.getType())) {
            if ((SEND).equals(intent.getAction())) {
                Handler handler=new Handler();
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        handleSendText(intent);
                    }
                };
                handler.post(runnable);
            }
        }
        finish();
    }

    private void handleSendText(Intent intent){
        String[] keys=new String[]{"title","content","dates","type","creator"};
        Object[] values=new Object[]{getTitle(intent),getContent(intent),getDates(),getType(),getCreator()};
        myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,keys,values);

        //刷新
        Intent intent1=new Intent();
        intent1.setAction(MainActivity.REFRESH);
        context.sendBroadcast(intent1);

        Toast.makeText(context,"已记录",Toast.LENGTH_SHORT).show();
    }

    //获取数据
    private String getTableName(){
        String tableName;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED){
            TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager!=null) {
                String deviceId=telephonyManager.getDeviceId();
                tableName="note"+deviceId;
                HttpUtils.onlineTableName=tableName;
                MyDatabaseUtils.tableName=tableName;
            }else {
                tableName="note_table";
            }
        }else {
            tableName="note_table";
        }
        return tableName;
    }
    private String getTitle(Intent intent){
        if (intent.getStringExtra(Intent.EXTRA_SUBJECT)!=null){
            return intent.getStringExtra(Intent.EXTRA_SUBJECT);
        }else {
            return "";
        }
    }
    private String getContent(Intent intent){
        if (intent.getStringExtra(Intent.EXTRA_TEXT)!=null){
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        }else {
            return "";
        }
    }
    private long getDates(){
        return System.currentTimeMillis();
    }
    private String getType(){
        return "share";
    }
    private String getCreator(){
        return MyDatabaseUtils.tableName;
    }
}
