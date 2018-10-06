package com.beyond.fly.note3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beyond.fly.note3.Utils.ContactUtils;
import com.beyond.fly.note3.Utils.HttpUtils;
import com.beyond.fly.note3.Utils.MyHashmap;

import java.util.ArrayList;

/**
 * Created by beyond on 2017/12/30.
 */

public class ShareActivity extends AppCompatActivity {
    private ListView listView;
    private TextInputEditText textInputEditText;
    private ProgressBar progressBar;
    private ArrayList<MyHashmap> userData;
    private ArrayList<MyHashmap> friendData;
    public static String SHARE_ACTIVITY="com.beyond.action.SHARE_ACTIVITY";
    private Intent intent;
    private HttpUtils httpUtils;
    private SharedPreferences sharedPreferences;
    private ContactUtils contactUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        sharedPreferences=ShareActivity.this.getSharedPreferences("beyond",MODE_PRIVATE);
        httpUtils = new HttpUtils();
        intent=getIntent();

        initViews();

        new GetFriendDataOnline().execute();

        new ReadOnlineUserData().execute();
    }
    private void initViews(){

        listView=findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String username=friendData.get(i).get("title").toString();
                if (SHARE_ACTIVITY.equals(intent.getAction())) {
                    new ShareOnline(username).execute();
                }
            }
        });

        progressBar=findViewById(R.id.progress_bar);
    }

    //第一次开启时，输入手机号，并将手机号上传至网络
    private void addTelephoneNumber(){
        AlertDialog.Builder builder=new AlertDialog.Builder(ShareActivity.this);
        View dialogLayout= LayoutInflater.from(ShareActivity.this).inflate(R.layout.add_phone_number_alert_dialog,null);
        textInputEditText =dialogLayout.findViewById(R.id.add_phone_number_edit_text);
        builder.setView(dialogLayout)
                .setTitle("请输入手机号以方便其他人能找到你")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String content=textInputEditText.getText().toString();

                        new InsertUserPhoneOnline(content.trim()).execute();
                    }
                })
                .setCancelable(true);
        builder.create().show();
    }
    class InsertUserPhoneOnline extends AsyncTask<String,String,String>{
        String phoneNumber;
        private InsertUserPhoneOnline(String phoneNumber){
            this.phoneNumber=phoneNumber;
        }
        @Override
        protected String doInBackground(String... strings) {
            //向user_name中添加user
            if (!httpUtils.isUserExistWithHttpUrlConnection(new String[]{"tablename","title"},new String[]{"user_name",HttpUtils.onlineTableName})){
                httpUtils.createWithHttpUrlConnection(new String[]{"tablename","title","content"},new Object[]{"user_name",HttpUtils.onlineTableName,phoneNumber});
            }else {
                int user_id=-1;
                for (int i = 0; i < userData.size(); i++) {
                    if (HttpUtils.onlineTableName.equals(userData.get(i).get("title").toString())){
                        user_id=Integer.parseInt(userData.get(i).get("_id").toString());
                    }
                }
                httpUtils.updateItemWithHttpUrlConnection(new String[]{"tablename","_id","content"},new Object[]{"user_name",user_id,phoneNumber});
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("phone_number",phoneNumber);
            editor.apply();
        }
    }

    //分享到online
    class ShareOnline extends AsyncTask<String,String,String>{
        private String username;
        private ShareOnline(String username){
            this.username=username;
        }
        @Override
        protected String doInBackground(String... strings) {

            String[] keys = new String[]{"tablename", "title", "content", "dates", "remind_date", "color", "calendar_event_id", "type", "creator"};
            Object[] values = new Object[]{username, getMyTitle(),getMyContent(),getCurrentTimeMillis(),getMyRemindDate(),getMyColor(),getMyCalendarEventId(),getType(),getCreator()};
            httpUtils.createWithHttpUrlConnection(keys,values);

            if (!httpUtils.createSuccessful.equals("1")){
                doInBackground();
            }else {
                finish();
            }

            return null;
        }
    }

    //读取所有用户信息
    class ReadOnlineUserData extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            userData=httpUtils.readWithHttpUrlConnection(new String[]{"tablename"},new String[]{"user_name"});
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //如果没有输入手机号，弹出输入手机号的窗口，因为弹出窗口中需要用到userData所以在这加载
            if (sharedPreferences.getString("phone_number","").length()!=11) {
                addTelephoneNumber();
            }
        }
    }

    class MyListViewAdapter extends BaseAdapter{
        private Context context;
        private ArrayList<MyHashmap> userData;

        private MyListViewAdapter(Context context, ArrayList<MyHashmap> userData){
            this.context=context;
            this.userData=userData;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder=new ViewHolder();
            if (view==null){
                view=View.inflate(context,R.layout.share_activity_list_item,null);
                viewHolder.textView16=view.findViewById(R.id.textView16);
                viewHolder.textView17=view.findViewById(R.id.textView17);

                view.setTag(viewHolder);
            }else {
                viewHolder=(ViewHolder) view.getTag();
            }
            contactUtils=new ContactUtils();
            String name;
            if (userData.get(i).get("content").toString().trim().length()==11){
                name=contactUtils.getContactsName(context,userData.get(i).get("content").toString().trim());
            }else {
                name="No Phone Number";
            }
            viewHolder.textView16.setText(name);
            viewHolder.textView17.setText(userData.get(i).get("content").toString());

            return view;
        }
        class ViewHolder{
            TextView textView16;
            TextView textView17;
        }

        @Override
        public int getCount() {
            return userData.size();
        }

        @Override
        public Object getItem(int i) {
            return userData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }

    //读取好友信息
    class GetFriendDataOnline extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(String... strings) {
            contactUtils=new ContactUtils();

            //获取所有通讯录上的号码
            String[] phoneNumbers=contactUtils.getContactsNumbers(ShareActivity.this);
            String[] phoneNumbersTrim=new String[phoneNumbers.length];
            for (int i = 0; i < phoneNumbers.length; i++) {
                if (phoneNumbers[i]!=null) {
                    //去除电话号码中的横线和空格
                    String string = phoneNumbers[i].replaceAll("[ |-]", "");
                    phoneNumbersTrim[i]=string;
                }
            }

            //从网上选取通讯录中的号码
            friendData = httpUtils.readFriendDataWithHttpUrlConnection("content",phoneNumbersTrim);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            listView.setAdapter(new MyListViewAdapter(ShareActivity.this,friendData));
        }
    }

    private int getMyId(){
        return intent.getIntExtra("_id",-1);
    }
    private String getMyTitle(){
        return intent.getStringExtra("title");
    }
    private String getMyContent(){
        return intent.getStringExtra("content");
    }
    private long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }
    private long getMyRemindDate(){return intent.getLongExtra("remind_date",0);}
    private int getMyColor(){
        return 0;
    }
    private long getMyCalendarEventId(){
        return intent.getLongExtra("calendar_event_id",-1);
    }
    private String getType(){return "remind";}
    private String getCreator(){return intent.getStringExtra("creator");}
}
