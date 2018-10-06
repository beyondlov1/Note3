package com.beyond.fly.note3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.beyond.fly.note3.Fragments.FragmentCreate4;
import com.beyond.fly.note3.Fragments.FragmentRemind3;
import com.beyond.fly.note3.Fragments.FragmentShow2;
import com.beyond.fly.note3.Utils.HttpUtils;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;
import com.beyond.fly.note3.Utils.MyHashmap;
import com.beyond.fly.note3.Utils.MySQLiteHelper;
import com.beyond.fly.note3.Utils.SpeechUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private HttpUtils httpUtils;

    private static final int MY_PERMISSION_CALENDAR=1;
    private static final int MY_PERMISSION_SYSTEM_ALERT_WINDOW=1;

    private FragmentShow2 fragmentShow2;
    private FragmentRemind3 fragmentRemind3;
    private FragmentCreate4 fragment4;

    private FloatingActionButton floatingActionButton;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    public static String REFRESH="com.beyond.action.REFRESH";
    public static String REMINDER_UPDATE="com.beyond.action.REMINDER_UPDATE";
    public static String REMINDER_CREATE="com.beyond.action.REMINDER_CREATE";
    public static String START="com.beyond.action.START";

    private NotificationManager notificationManager;
    public static Notification notification;
    private Notification.Builder builder;

    private SpeechUtils speechUtils;
    private EditText resultEditText;
    private ImageButton start;
    private TextView textView;

    //OCR
    private boolean hasGotToken=false;
    private static final int REQUEST_CODE_ACCURATE_BASIC = 107;
    private ImageButton cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initUtils();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED) {

            getTableName();

            new InitLocalDatabase().execute();

            new CreateOnlineTable().execute();

            initViews();

            startSyncService();

            //initNotification();
        }else {
            finish();
        }

        //OCR
        initAccessTokenWithAkSk();

        /**
        *adapter中直接发送intent但是有bug：长按之后控制MainActivity.instance.finish，然后再点击就会崩溃，改用接口方法
         *
         Intent intent=getIntent();
         if (intent.getAction().equals(REMINDER_UPDATE)||intent.getAction().equals(REMINDER_CREATE)){
         showFragment3(intent);
         }
        */

    }

    //OCR
    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(), "SpsYQexVGSuXO418HRgGY0cw", "NUqhxqZzMiARiLo5c6ZMsqsyejRBTIq4");
    }
    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }
    private void getOCRResult(){
        if (!checkTokenStatus()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                com.beyond.fly.note3.Utils.FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent,REQUEST_CODE_ACCURATE_BASIC);
    }
    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }
    private void showResult(String result){
        addToLocalDatabase("",result);
        Intent intent=new Intent(REFRESH);
        sendBroadcast(intent);
    }
    private void addToLocalDatabase(String title, String content){
        long currentDate=System.currentTimeMillis();
        MyDatabaseUtils myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
        String[] keys={"title","content","dates","type","creator"};
        Object[] values={title,content,currentDate,"remind",MyDatabaseUtils.tableName};
        myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,keys,values);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 识别成功回调，通用文字识别
        if (requestCode == REQUEST_CODE_ACCURATE_BASIC && resultCode == Activity.RESULT_OK) {
            RecognizeService.recGeneralBasic(com.beyond.fly.note3.Utils.FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            showResult(result);
                        }
                    });
        }
    }

    //初始化utils工具
    private void initUtils(){
        hideActionBar();
        testCalendarPermission();
        context=MainActivity.this;
        httpUtils=new HttpUtils();
        notificationManager=(NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        fragmentManager=getFragmentManager();
        speechUtils=new SpeechUtils(MainActivity.this);
    }

    //通知栏快速添加note
    private void initNotification(){
        Notification.InboxStyle inboxStyle=new Notification.InboxStyle();
        //inboxStyle.addLine("快速添加记录");

        Intent intent=new Intent(context,NotificationService.class);
        intent.setAction(NotificationService.START_NOTIFICATION_SERVICE);
        PendingIntent pendingIntent=PendingIntent.getService(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput.Builder remoteInputBuilder=new RemoteInput.Builder(NotificationService.resultKey);
        RemoteInput remoteInput=remoteInputBuilder
                .setLabel("记录")
                .build();

        Notification.Action noteDown=new Notification.Action.Builder(R.mipmap.icon,"记录",pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        builder=new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.status_bar_icon)
                .setContentTitle("快速添加记录")
                .setContentText(null)
                .setStyle(inboxStyle)
                .addAction(noteDown)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        notification=builder.build();

        notificationManager.notify(101, notification);
    }

    class CreateOnlineTable extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            String[] keys={"tablename"};
            String[] values={HttpUtils.onlineTableName};
            httpUtils.createTableWithHttpURLConnection(keys,values);

            if (!httpUtils.createTableSuccessful.equals("1")){
                doInBackground();
            }

            //向user_name中添加user
            if (!httpUtils.isUserExistWithHttpUrlConnection(new String[]{"tablename","title"},new String[]{"user_name",HttpUtils.onlineTableName})){
                httpUtils.createWithHttpUrlConnection(new String[]{"tablename","title"},new Object[]{"user_name",HttpUtils.onlineTableName});
            }

            return null;
        }
    }
    class InitLocalDatabase extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            initDatabase();
            return null;
        }
    }

    //创建数据库和表
    public void initDatabase(){
        String tableName=getTableName();
        SQLiteDatabase database;
        MySQLiteHelper mySQLiteHelper=MySQLiteHelper.getmInstance(context,getDatabaseName());
        String createTable_sql="CREATE TABLE "+tableName+"(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,content TEXT,dates REAL,remind_date REAL,color REAL,calendar_event_id REAL,type TEXT, creator TEXT);";

        database=context.openOrCreateDatabase(getDatabaseName(),Context.MODE_PRIVATE,null);
        if (!mySQLiteHelper.isTableExist(tableName)){
            database.execSQL(createTable_sql);
        }else {
            System.out.println("table is exist");
        }

        //database需要关闭否则会内存泄漏
        database.close();
    }
    private String getTableName(){
        String tableName;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED){
            TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager!=null) {
                String deviceId=telephonyManager.getDeviceId();
                tableName="note"+deviceId;
                MyDatabaseUtils.tableName=tableName;
                HttpUtils.onlineTableName="note"+deviceId;
            }else {
                tableName="note_table";
            }
        }else {
            tableName="note_table";
        }
        return tableName;
    }
    private String getDatabaseName(){
        return MyDatabaseUtils.databaseName;
    }

    private void startSyncService(){
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction("com.beyond.action.sync_service");
        context.startService(intent);
    }

    private void showFragment2(){
        if (fragmentShow2==null){
            fragmentShow2=new FragmentShow2();
        }
        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.replace(R.id.fragment,fragmentShow2);
        fragmentManager.popBackStack();
        fragmentTransaction.commit();
        floatingActionButton.setVisibility(View.VISIBLE);
    }
    private void popFragment2(){
        if (fragmentShow2 ==null){
            fragmentShow2 =new FragmentShow2();
        }
        fragmentTransaction=fragmentManager.beginTransaction();
        //清空回退栈中所有的Fragment
        fragmentManager.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.replace(R.id.fragment, fragmentShow2);
        fragmentTransaction.commit();
        floatingActionButton.setVisibility(View.VISIBLE);
    }
    private void showFragment3(Intent intent){
        if (fragmentRemind3 ==null){
            fragmentRemind3 =new FragmentRemind3();
        }
        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.replace(R.id.fragment,fragmentRemind3);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        //向fragment3中传递数据
        fragmentRemind3.initIntent(intent);

        floatingActionButton.setVisibility(View.INVISIBLE);
    }
    private void showFragment4(){
        if (fragment4==null){
            fragment4=new FragmentCreate4();
        }
        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.replace(R.id.fragment,fragment4);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        floatingActionButton.setVisibility(View.INVISIBLE);
    }

    private void initViews(){
        //设置控件样式
        Toolbar toolbar=findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu1);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_1:
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED ){
                            ActivityCompat.requestPermissions(MainActivity.this,new String []{Manifest.permission.RECORD_AUDIO},MY_PERMISSION_CALENDAR);
                        }
                        resultEditText.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                        speechUtils.initSpeechRecognizer();
                        speechUtils.getResultText();
                        break;
                    case R.id.menu_2:
                        getOCRResult();
                        break;
                }
                return true;
            }
        });
        textView=findViewById(R.id.titleTextView);
        textView.setText(R.string.title);
        floatingActionButton=findViewById(R.id.fab);
        floatingActionButton.setUseCompatPadding(true);
        floatingActionButton.setImageResource(R.mipmap.ic_add_white_24dp);
        resultEditText=findViewById(R.id.resultEditText);
        resultEditText.setVisibility(View.INVISIBLE);
        start=findViewById(R.id.speechButton);
        speechUtils.initViews(resultEditText,textView);
        //OCR
        cameraButton=findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOCRResult();
            }
        });

        //语音识别文字
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED ){
                    ActivityCompat.requestPermissions(MainActivity.this,new String []{Manifest.permission.RECORD_AUDIO},MY_PERMISSION_CALENDAR);
                }
                resultEditText.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                speechUtils.initSpeechRecognizer();
                speechUtils.getResultText();
            }
        });

        //设置初始Fragment
        showFragment2();

        fragmentRemind3 =new FragmentRemind3();
        fragment4=new FragmentCreate4();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment4();
            }
        });

        //调用fragment2的接口，实现对fragment2中长按事件的响应
        fragmentShow2.setOnItemLongClickListener(new FragmentShow2.OnItemLongClickListener() {
            @Override
            public void OnItemLongClick(ArrayList<MyHashmap> data, int position) {
                Intent intent=new Intent();
                intent.setAction(REMINDER_UPDATE);
                intent.putExtra("title",data.get(position).get("title").toString());
                intent.putExtra("content",data.get(position).get("content").toString());
                intent.putExtra("dates",(long)data.get(position).get("dates"));
                intent.putExtra("remind_date",(long)data.get(position).get("remind_date"));
                intent.putExtra("_id",(int)data.get(position).get("_id"));
                intent.putExtra("calendar_event_id",(long)data.get(position).get("calendar_event_id"));
                intent.putExtra("type",data.get(position).get("type").toString());
                intent.putExtra("color",(int)data.get(position).get("color"));
                intent.putExtra("creator",data.get(position).get("creator").toString());
                showFragment3(intent);
            }
        });

        //添加提醒
        fragmentRemind3.setOnButtonClickListener(new FragmentRemind3.OnButtonClickListener() {
            @Override
            public void onClick(View view) {
                popFragment2();
            }
        });

        //创建时转到添加提醒界面
        fragment4.setOnMoreClickListener(new FragmentCreate4.OnMoreClickListener() {
            @Override
            public void onClick(View view, Intent intent) {
                showFragment3(intent);
            }
        });

        //回到主界面
        fragment4.setOnNormalClickListener(new FragmentCreate4.OnNormalClickListener() {
            @Override
            public void OnClick(View view) {
                showFragment2();
            }
        });
    }

    private void hideActionBar(){
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
    }
    private void testCalendarPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)!= PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
            //获取多个权限要在String[]中添加多个权限
            ActivityCompat.requestPermissions(MainActivity.this,new String []{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS},MY_PERMISSION_CALENDAR);
            ActivityCompat.requestPermissions(MainActivity.this,new String []{Manifest.permission.READ_CALENDAR},MY_PERMISSION_CALENDAR);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fragment4.isMyIsVisable()||fragmentRemind3.isMyIsVisable()){
            floatingActionButton.setVisibility(View.INVISIBLE);
        }else {
            floatingActionButton.setVisibility(View.VISIBLE);
        }
    }
}
