package com.beyond.fly.note3;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.beyond.fly.note3.Utils.HttpUtils;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;

import java.lang.reflect.Method;

/**
 * Created by beyond on 2018/1/7.
 */

public class NotificationIntentService extends IntentService {
    //这种service在完成之后会自动关闭，所以换用普通service：NotificationService
    private Context context;
    public static String START_NOTIFICATION_SERVICE="com.beyond.action.NOTIFICATION_SERVICE";
    public static String resultKey="fast_note";
    public NotificationIntentService(){
        super("NoteDown");
        context=this;
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent!=null&&START_NOTIFICATION_SERVICE.equals(intent.getAction())){
            Bundle remoteContent=RemoteInput.getResultsFromIntent(intent);
            String content=remoteContent.getString(resultKey);
            long dates=System.currentTimeMillis();
            MyDatabaseUtils myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
            //下面这五个元素是必须的，估计他们是String,whatever
            myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,new String[]{"title","content","dates","type","creator"},new Object[]{content,"",dates,"remind",MyDatabaseUtils.tableName});
            Intent intent1=new Intent();
            intent1.setAction(MainActivity.REFRESH);
            sendBroadcast(intent1);
            NotificationManager notificationManager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager!=null)
            notificationManager.notify(101,MainActivity.notification);
            collapseStatusBar(context);
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startForeground(101,MainActivity.notification);
        return super.onStartCommand(intent, flags, startId);
    }

    //神乎其技啊，标红了也他妈可以运行，我靠
    public static final void collapseStatusBar(Context ctx) {
        Object sbservice = ctx.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method collapse;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                collapse = statusBarManager.getMethod("collapsePanels");
            } else {
                collapse = statusBarManager.getMethod("collapse");
            }
            collapse.invoke(sbservice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final void expandStatusBar(Context ctx) {
        Object sbservice = ctx.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand;
            if (Build.VERSION.SDK_INT >= 17) {
                expand = statusBarManager.getMethod("expandNotificationsPanel");
            } else {
                expand = statusBarManager.getMethod("expand");
            }
            expand.invoke(sbservice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
