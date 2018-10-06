package com.beyond.fly.note3.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;

import com.beyond.fly.note3.ContentActivity;
import com.beyond.fly.note3.NotificationService;
import com.beyond.fly.note3.R;

/**
 * Created by beyond on 2018/2/6.
 */

public class NotificationUtils {
    private Context context;
    public NotificationUtils(Context context){
        this.context=context;
    }
    public Notification getNotification(int id, String title, String content, PendingIntent contentIntent, PendingIntent fullScreenIntent){
        Notification.InboxStyle inboxStyle=new Notification.InboxStyle();
        //inboxStyle.addLine("快速添加记录");

        Intent intent=new Intent(context,NotificationService.class);
        intent.setAction(NotificationService.START_NOTIFICATION_SERVICE);
        PendingIntent pendingIntent=PendingIntent.getService(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput.Builder remoteInputBuilder=new RemoteInput.Builder(NotificationService.resultKey);
        RemoteInput remoteInput=remoteInputBuilder
                .setLabel("记录")
                .build();

        Notification.Action noteDown=new Notification.Action.Builder(R.mipmap.icon,"记录",pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        Notification.Builder builder=new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.status_bar_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(inboxStyle)
                .addAction(noteDown)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        if (fullScreenIntent!=null){
            builder.setFullScreenIntent(fullScreenIntent,true);
        }

        if (contentIntent!=null){
            builder.setContentIntent(contentIntent);
        }

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager!=null){
            notificationManager.notify(101,notification);
        }
        return builder.build();
    }

    public Notification getToContentActivityNotification(int id, String title, String content, PendingIntent fullScreenIntent){
        Intent intent = new Intent(context, ContentActivity.class);
        intent.setAction(ContentActivity.contentActivityAction);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return getNotification(id, getSimpleText(title,content,50), null, pendingIntent, fullScreenIntent);
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
