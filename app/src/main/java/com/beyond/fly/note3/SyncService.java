package com.beyond.fly.note3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beyond.fly.note3.Utils.CalendarMethod;
import com.beyond.fly.note3.Utils.HttpUtils;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;
import com.beyond.fly.note3.Utils.MyHashmap;
import com.beyond.fly.note3.Utils.NotificationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by beyond on 2017/12/24.
 */

public class SyncService extends Service{
    private Context context;
    private HttpUtils httpUtils;
    private SoundPool soundPool;
    private int voiceId;
    public static boolean isNetworkAvailable;
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            Network[] networks=connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network network :networks) {
                networkInfo=connectivityManager.getNetworkInfo(network);
                if (networkInfo.getState()==NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }
    private String[] keysWithTableNameAnd_id =new String[] {"tablename","_id","title","content","dates","remind_date","color","calendar_event_id","type","creator"};
    private String[] keys_id=new String[] {"_id","title","content","dates","remind_date","color","calendar_event_id","type","creator"};
    private Random randomInt = new Random();
    private Timer randomNoteTimer;
    private TimerTask randomNoteTimerTask;
    private Notification notification;

    public static String randomTitle;
    public static String randomContent;

    private int userDelay;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=SyncService.this;
        httpUtils=new HttpUtils();
        Timer timer=new Timer();

        //检测网络是否可用
        final Handler handler=new Handler();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                if (isNetworkAvailable=isNetworkAvailable(context)) {
                    new SyncData(context).execute();
                }
            }
        };
        timer.schedule(timerTask,0,60000);

        //初始化推送铃声
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                voiceId = initSoundPool();
            }
        };
        new Thread(runnable).start();

        //每隔一段时间通知一个随机note（每次都可以设定下一次显示的时间）（timer timerTask类的对象只能用一次，所以每次用都要重新实例化，不能重复用）
        //TO-DO: 分析什么时候可以弹出，什么时候不能弹出
        //userDelay = 1000;
        //setRandomNotificationRepeat(userDelay);

        //每隔一段时间通知一个随机的note
        randomNoteTimer = new Timer();
        randomNoteTimerTask = new TimerTask() {
            @Override
            public void run() {
                MyDatabaseUtils myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
                ArrayList<MyHashmap> localData = myDatabaseUtils.getDataFromDatabase(MyDatabaseUtils.tableName);
                //本地数据不为空时弹出随机Note
                if (localData!=null&&localData.size()>0) {
                    ChooseRandomNote chooseRandomNote = new ChooseRandomNote();
                    chooseRandomNote.notifyRandomNote(localData);
                }
            }
        };
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        calendar.set(currentYear,currentMonth,currentDay,currentHourOfDay,currentMinute+1);
        Date date=new Date();
        date.setTime(calendar.getTimeInMillis());
        randomNoteTimer.schedule(randomNoteTimerTask,date, 24*60*60*1000);
    }

    private void setRandomNotificationRepeat(int delay){
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                MyDatabaseUtils myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
                ArrayList<MyHashmap> localData = myDatabaseUtils.getDataFromDatabase(MyDatabaseUtils.tableName);
                //本地数据不为空时弹出随机Note
                if (localData!=null&&localData.size()>0) {
                    ChooseRandomNote chooseRandomNote = new ChooseRandomNote();
                    chooseRandomNote.notifyRandomNote(localData);

                    //重新设定弹出时间
                    timer.cancel();
                    setRandomNotificationRepeat(userDelay);
                }
            }
        };
        timer.schedule(timerTask, delay);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        NotificationManager notificationManager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager!=null)
            notificationManager.cancel(101);
        System.out.println("同步服务停止，取消通知");
    }

    class SyncData extends AsyncTask<String,String,String>{
        private Context context;
        private boolean isNeedRefresh=false;

        private SyncData(Context context){
            this.context=context;
            //doInBackground();
        }

        @Override
        protected String doInBackground(String... strings) {
            System.out.println("do in background");
            try{
            ArrayList<MyHashmap> localData;
            ArrayList<MyHashmap> onlineData;

            String[] keys=new String[]{"tablename"};
            String[] values=new String[]{HttpUtils.onlineTableName};
            onlineData=httpUtils.readWithHttpUrlConnection(keys,values);

            MyDatabaseUtils myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
            localData=myDatabaseUtils.getDataFromDatabase(MyDatabaseUtils.tableName);

            if (httpUtils.readSuccessful.equals("1")&&onlineData!=null) {

                /**
                *2.0版同步方案
                 * 比较相同Id的创建时间，如果不一样就都参与同步
                */
                if (localData.size()!=0&&onlineData.size()!=0) {

                    //如果两个数据库都不为空
                    if (Integer.parseInt(onlineData.get(0).get("_id").toString()) > Integer.parseInt(localData.get(0).get("_id").toString())) {

                        //保护机制，防止用户清空数据后，在同步前直接添加条目，造成所有数据被删除
                        if (Integer.parseInt(onlineData.get(0).get("_id").toString()) - Integer.parseInt(localData.get(0).get("_id").toString()) >30){
                            return "maybeError";
                        }
                        //如果online中的_id更大，就选取从最大的_id开始
                        ArrayList<HashMap<String, Object>> tmpArrayList = new ArrayList<>();
                        int max_id=Integer.parseInt(onlineData.get(0).get("_id").toString());

                        int itemCountFromOnline=0;
                        //online和local两个Data里面的数据类型不一样，虽然外表都是Object，但是使用indexOf的时候还是有差别的，MyHashmap中要加toString来避免这个问题
                        for (int i = 0; (getDatesFromId(onlineData,max_id-i)!=getDatesFromId(localData,max_id-i)||(getDatesFromId(onlineData,max_id-i)==-1&&getDatesFromId(localData,max_id-i)==-1)); i++) {
                            if (getHashmapFromId(onlineData,max_id-i)!=null){
                                tmpArrayList.add(getHashmapFromId(onlineData,max_id-i));
                                httpUtils.deleteItemWithHttpUrlConnection(new String[]{"tablename", "_id"}, new Object[]{HttpUtils.onlineTableName, max_id-i});
                                itemCountFromOnline++;
                            }
                            if (getHashmapFromId(localData,max_id-i)!=null){
                                tmpArrayList.add(getHashmapFromId(localData,max_id-i));
                                myDatabaseUtils.deleteDataOnlyFromLocalDatabase(MyDatabaseUtils.tableName, "_id", max_id-i);
                            }
                        }
                        //向本地和网络数据库中添加tempArrayList
                        for (int i = 0; i < tmpArrayList.size(); i++) {
                            //早知道就把这玩意儿写成Hashmap了
                            //向网上数据库写入tmpArrayList
                            Object[] tmpValues = new Object[]{
                                    HttpUtils.onlineTableName,
                                    max_id+i,
                                    tmpArrayList.get(i).get("title"),
                                    tmpArrayList.get(i).get("content"),
                                    tmpArrayList.get(i).get("dates"),
                                    tmpArrayList.get(i).get("remind_date"),
                                    tmpArrayList.get(i).get("color"),
                                    tmpArrayList.get(i).get("calendar_event_id"),
                                    tmpArrayList.get(i).get("type"),
                                    tmpArrayList.get(i).get("creator")
                            };
                            httpUtils.createWithIdWithHttpUrlConnection(keysWithTableNameAnd_id, tmpValues);
                            //向本地数据库写入tmpArrayList
                            Object[] tmpValues1 = new Object[]{
                                    max_id+i,
                                    tmpArrayList.get(i).get("title"),
                                    tmpArrayList.get(i).get("content"),
                                    tmpArrayList.get(i).get("dates"),
                                    tmpArrayList.get(i).get("remind_date"),
                                    tmpArrayList.get(i).get("color"),
                                    tmpArrayList.get(i).get("calendar_event_id"),
                                    tmpArrayList.get(i).get("type"),
                                    tmpArrayList.get(i).get("creator")
                            };
                            myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName, keys_id, tmpValues1);
                            //添加其他人推送的事件
                            if (!tmpArrayList.get(i).get("creator").toString().equals(MyDatabaseUtils.tableName) && !tmpArrayList.get(i).get("creator").toString().equals("null") && Long.parseLong(tmpArrayList.get(i).get("remind_date").toString()) != 0) {
                                long calendar_event_id = CalendarMethod.getCalendarMethod().addCalendarEvent(context, tmpArrayList.get(i).get("title").toString(), tmpArrayList.get(i).get("content").toString(), Long.parseLong(tmpArrayList.get(i).get("remind_date").toString()));
                                myDatabaseUtils.updateDataToDatabase(MyDatabaseUtils.tableName, Integer.parseInt(tmpArrayList.get(i).get("_id").toString()), new String[]{"calendar_event_id"}, new Object[]{calendar_event_id});
                            }
                        }
                        if (itemCountFromOnline>0) {
                            isNeedRefresh = true;
                            soundPool.play(voiceId, 1, 1, 1, 0, 1);
                        }

                    } else if (Integer.parseInt(localData.get(0).get("_id").toString()) >= Integer.parseInt(onlineData.get(0).get("_id").toString())) {

                        //如果local中的_id更大，或者同样大，就选取从最大的_id开始
                        ArrayList<HashMap<String, Object>> tmpArrayList = new ArrayList<>();
                        int max_id=Integer.parseInt(localData.get(0).get("_id").toString());
                        int recreate_id=0;
                        int itemCountFromOnline=0;
                        System.out.println("max_id"+max_id);
                        for (int i = 0; ((getDatesFromId(onlineData,(max_id-i))!=getDatesFromId(localData,(max_id-i)))||(getDatesFromId(onlineData,(max_id-i))==-1&&getDatesFromId(localData,(max_id-i))==-1)); i++) {
                            if (getHashmapFromId(onlineData,max_id-i)!=null){
                                tmpArrayList.add(getHashmapFromId(onlineData,max_id-i));
                                httpUtils.deleteItemWithHttpUrlConnection(new String[]{"tablename", "_id"}, new Object[]{HttpUtils.onlineTableName, max_id-i});
                                itemCountFromOnline++;
                            }
                            if (getHashmapFromId(localData,max_id-i)!=null){
                                tmpArrayList.add(getHashmapFromId(localData,max_id-i));
                                myDatabaseUtils.deleteDataOnlyFromLocalDatabase(MyDatabaseUtils.tableName, "_id", max_id-i);
                                System.out.println("max_id");
                            }
                            recreate_id=max_id-i;
                        }
                        System.out.println("recreate_id"+recreate_id);
                        System.out.println(tmpArrayList);
                        //向本地和网络数据库中添加tempArrayList
                        for (int i = 0; i < tmpArrayList.size(); i++) {
                            //早知道就把这玩意儿写成Hashmap了
                            //向网上数据库写入tmpArrayList
                            Object[] tmpValues = new Object[]{
                                    HttpUtils.onlineTableName,
                                    max_id+i,
                                    tmpArrayList.get(i).get("title"),
                                    tmpArrayList.get(i).get("content"),
                                    tmpArrayList.get(i).get("dates"),
                                    tmpArrayList.get(i).get("remind_date"),
                                    tmpArrayList.get(i).get("color"),
                                    tmpArrayList.get(i).get("calendar_event_id"),
                                    tmpArrayList.get(i).get("type"),
                                    tmpArrayList.get(i).get("creator")
                            };
                            httpUtils.createWithIdWithHttpUrlConnection(keysWithTableNameAnd_id, tmpValues);
                            //向本地数据库写入tmpArrayList
                            Object[] tmpValues1 = new Object[]{
                                    max_id+i,
                                    tmpArrayList.get(i).get("title"),
                                    tmpArrayList.get(i).get("content"),
                                    tmpArrayList.get(i).get("dates"),
                                    tmpArrayList.get(i).get("remind_date"),
                                    tmpArrayList.get(i).get("color"),
                                    tmpArrayList.get(i).get("calendar_event_id"),
                                    tmpArrayList.get(i).get("type"),
                                    tmpArrayList.get(i).get("creator")
                            };
                            myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName, keys_id, tmpValues1);
                            //添加其他人推送的事件
                            if (!tmpArrayList.get(i).get("creator").toString().equals(MyDatabaseUtils.tableName) && !tmpArrayList.get(i).get("creator").toString().equals("null") && Long.parseLong(tmpArrayList.get(i).get("remind_date").toString()) != 0) {
                                long calendar_event_id = CalendarMethod.getCalendarMethod().addCalendarEvent(context, tmpArrayList.get(i).get("title").toString(), tmpArrayList.get(i).get("content").toString(), Long.parseLong(tmpArrayList.get(i).get("remind_date").toString()));
                                myDatabaseUtils.updateDataToDatabase(MyDatabaseUtils.tableName, Integer.parseInt(tmpArrayList.get(i).get("_id").toString()), new String[]{"calendar_event_id"}, new Object[]{calendar_event_id});
                            }
                        }
                        if (itemCountFromOnline>0) {
                            isNeedRefresh = true;
                            soundPool.play(voiceId, 1, 1, 1, 0, 1);
                        }
                    }
                }else if (localData.size()==0&&onlineData.size()!=0){
                    //如果本地数据库为空
                    for (int i = 0; i < onlineData.size(); i++) {
                        String[] localKeys = new String[]{"_id", "title", "content", "dates", "remind_date", "color", "calendar_event_id", "type", "creator"};
                        Object[] localValues = new Object[]{onlineData.get(i).get("_id"), onlineData.get(i).get("title"), onlineData.get(i).get("content"),
                                onlineData.get(i).get("dates"), onlineData.get(i).get("remind_date"), onlineData.get(i).get("color"), onlineData.get(i).get("calendar_event_id"), onlineData.get(i).get("type"), onlineData.get(i).get("creator")};
                        myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName, localKeys, localValues);

                        isNeedRefresh = true;
                        soundPool.play(voiceId, 1, 1, 1, 0, 1);

                        //添加其他人推送的事件
                        if (!onlineData.get(i).get("creator").toString().equals(MyDatabaseUtils.tableName) && !onlineData.get(i).get("creator").toString().equals("null") && Long.parseLong(onlineData.get(i).get("remind_date").toString()) != 0) {
                            long calendar_event_id = CalendarMethod.getCalendarMethod().addCalendarEvent(context, onlineData.get(i).get("title").toString(), onlineData.get(i).get("content").toString(), Long.parseLong(onlineData.get(i).get("remind_date").toString()));
                            myDatabaseUtils.updateDataToDatabase(MyDatabaseUtils.tableName, Integer.parseInt(onlineData.get(i).get("_id").toString()), new String[]{"calendar_event_id"}, new Object[]{calendar_event_id});
                        }
                    }
                }else if (localData.size()!=0&&onlineData.size()==0){
                    //如果网络数据库为空
                    for (int i = 0; i < localData.size(); i++) {
                        String[] onlineKeys = new String[]{"tablename", "_id", "title", "content", "dates", "remind_date", "color", "calendar_event_id", "type", "creator"};
                        Object[] onlineValues = new Object[]{HttpUtils.onlineTableName, localData.get(i).get("_id"), localData.get(i).get("title"), localData.get(i).get("content"),
                                localData.get(i).get("dates"), localData.get(i).get("remind_date"), localData.get(i).get("color"), localData.get(i).get("calendar_event_id"), localData.get(i).get("type"), localData.get(i).get("creator")};
                        httpUtils.createWithIdWithHttpUrlConnection(onlineKeys, onlineValues);
                    }
                }

                //将readSuccessful恢复为"0"
                httpUtils.readSuccessful="0";
            }else {
                Log.i(TAG, "doInBackground: read failed");
            }

            if (httpUtils.createSuccessful.equals("1")){
                httpUtils.createSuccessful="0";
            }else {
                Log.i(TAG, "onPostExecute: SyncData failed to create item");
            }


            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (isNeedRefresh) {
                Intent intent=new Intent();
                intent.setAction(MainActivity.REFRESH);
                context.sendBroadcast(intent);
                isNeedRefresh=false;
            }
        }
    }

    //随机弹出Note的通知
    private class ChooseRandomNote {
        private void notifyRandomNote(ArrayList<MyHashmap> data) {
            int[] ids = new int[data.size()];
            for (int i = 0; i < data.size(); i++) {
                ids[i] = Integer.parseInt(data.get(i).get("_id").toString());
            }
            try {
                MyHashmap randomHashmap = getHashmapFromId(data, ids[randomInt.nextInt(data.size())]);
                randomTitle = randomHashmap.get("title").toString();
                randomContent = randomHashmap.get("content").toString();

                setRandomNoteView(randomTitle,randomContent);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        private void setRandomNoteView(String title, String content){
            setNotification(title,content);
        }
        private void setNotification(final String title, final String content){
            Notification.InboxStyle inboxStyle=new Notification.InboxStyle();
            //inboxStyle.addLine("快速添加记录");

            Intent NS_intent=new Intent(context,NotificationService.class);
            NS_intent.setAction(NotificationService.START_NOTIFICATION_SERVICE);
            PendingIntent pendingIntent=PendingIntent.getService(context,0,NS_intent,PendingIntent.FLAG_UPDATE_CURRENT);

            //点击通知进入Content页面
            Intent CA_intent=new Intent(context,ContentActivity.class);
            CA_intent.setAction(ContentActivity.contentActivityAction);
            CA_intent.putExtra("title",title);
            CA_intent.putExtra("content",content);
            PendingIntent startContentActivityPendingIntent=PendingIntent.getActivity(context,0,CA_intent,PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteInput.Builder remoteInputBuilder=new RemoteInput.Builder(NotificationService.resultKey);
            RemoteInput remoteInput=remoteInputBuilder
                    .setLabel("记录")
                    .build();

            Notification.Action noteDown=new Notification.Action.Builder(R.mipmap.icon,"记录",pendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();

            Notification.Builder builder=new Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.status_bar_icon)
                    .setContentTitle(getSimpleText(title,content,50))
                    .setContentText(null)
                    .setStyle(inboxStyle)
                    .addAction(noteDown)
                    .setFullScreenIntent(startContentActivityPendingIntent,true)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
            notification=builder.build();
            NotificationManager notificationManager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);

            if (notificationManager!=null) {
                notificationManager.notify(101, notification);
                //保持前台
                startForeground(101,notification);
            }

            //设置10s悬浮通知自动消失
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    NotificationUtils notificationUtils=new NotificationUtils(context);
                    Intent intent = new Intent(context,ContentActivity.class);
                    intent.setAction(ContentActivity.contentActivityAction);
                    intent.putExtra("title", title);
                    intent.putExtra("content",content);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,101,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    notificationUtils.getNotification(101,getSimpleText(title,content,50),null,contentIntent,null);
                }
            };
            timer.schedule(timerTask,10000);
        }
        private String getSimpleText(String title, String content, int length){
            if (title!=null&&!title.equals("")&&!title.equals("null")){
                if (title.length()<length){
                    return title;
                }else if (title.contains("http")){
                    String titleSubString = title.substring(0,title.indexOf("http"));
                    if (titleSubString.length()>length){
                        return titleSubString.substring(0, length);
                    }else {
                        return titleSubString;
                    }
                }else {
                    return title.substring(0,length);
                }
            }else if (content!=null&&!content.equals("")&&!content.equals("null")){
                if (content.contains("http")){
                    int httpPosition = content.indexOf("http");
                    String contentSubString = content.substring(0,httpPosition);
                    if (contentSubString.length()>length) {
                        return contentSubString.substring(0,length);
                    }else {
                        return contentSubString;
                    }
                }else if (content.length()>length){
                    return content.substring(0,length);
                }else{
                    return content;
                }
            }else {
                return "null";
            }
        }
    }

    private long getDatesFromId(ArrayList<MyHashmap> arrayList,int _id){
        //先查到_id所在的indexId
        MyHashmap indexHashmap=new MyHashmap();
        indexHashmap.put("_id",_id);
        int indexId=arrayList.indexOf(indexHashmap);
        if (indexId==-1){
            return -1;
        }

        return Long.parseLong(arrayList.get(indexId).get("dates").toString());
    }
    private MyHashmap getHashmapFromId(ArrayList<MyHashmap> arrayList,int _id){
        //先查到_id所在的indexId
        MyHashmap indexHashmap=new MyHashmap();
        indexHashmap.put("_id",_id);
        int indexId=arrayList.indexOf(indexHashmap);
        if (indexId==-1){
            return null;
        }
        return arrayList.get(indexId);
    }

    //初始化同步时的铃声
    private int initSoundPool(){
        SoundPool.Builder builder=new SoundPool.Builder();
        builder.setMaxStreams(2);
        AudioAttributes.Builder attributesBuilder=new AudioAttributes.Builder();
        attributesBuilder.setLegacyStreamType(AudioManager.STREAM_NOTIFICATION);
        builder.setAudioAttributes(attributesBuilder.build());
        soundPool=builder.build();
        return soundPool.load(context,R.raw.water_drop,1);
    }
}
