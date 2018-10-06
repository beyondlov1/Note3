package com.beyond.fly.note3;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.beyond.fly.note3.Utils.MyDatabaseUtils;
import com.beyond.fly.note3.Utils.NotificationUtils;

import static com.beyond.fly.note3.NotificationIntentService.collapseStatusBar;

/**
 * Created by beyond on 2018/1/9.
 */

public class NotificationService extends Service {
    public static String START_NOTIFICATION_SERVICE="com.beyond.action.NOTIFICATION_SERVICE";
    public static String resultKey="fast_note";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context=this;
        if (intent!=null&&START_NOTIFICATION_SERVICE.equals(intent.getAction())){
            Bundle remoteContent= RemoteInput.getResultsFromIntent(intent);
            String content=remoteContent.getString(resultKey);
            long dates=System.currentTimeMillis();
            MyDatabaseUtils myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
            //下面这五个元素是必须的，估计他们是String,whatever
            myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,new String[]{"title","content","dates","type","creator"},new Object[]{content,"",dates,"remind",MyDatabaseUtils.tableName});
            Intent intent1=new Intent();
            intent1.setAction(MainActivity.REFRESH);
            sendBroadcast(intent1);
            NotificationManager notificationManager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager!=null){
                //ToDo: 展示RandomNote通知
                NotificationUtils notificationUtils=new NotificationUtils(context);
                notificationUtils.getToContentActivityNotification(101, SyncService.randomTitle,SyncService.randomContent, null);
            }
            collapseStatusBar(context);
        }
        this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopForeground(true);
        System.out.println("notificationServiceStopped");
    }
}
