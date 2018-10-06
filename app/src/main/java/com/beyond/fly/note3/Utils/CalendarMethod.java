package com.beyond.fly.note3.Utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by beyond on 17-12-8.
 */

public class CalendarMethod {

    private static String calenderURL = "content://com.android.calendar/calendars";
    private static String calenderEventURL = "content://com.android.calendar/events";
    private static String calenderReminderURL = "content://com.android.calendar/reminders";

    //检查是否有账户
    private static long checkAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(calenderURL), null, null, null, null);
        try {

            if (userCursor == null) {
                //没有账户
                return -1;
            }else {
                //有账户返回第一个账户ID
                int count = userCursor.getCount();
                if (count > 0) {
                    //有账户
                    userCursor.moveToFirst();
                    return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
                } else {
                    //账户数为负数或0
                    return -1;
                }
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    //添加test账户所需的静态参数
    private static String CALENDARS_NAME = "test";
    private static String CALENDARS_ACCOUNT_NAME = "test@gmail.com";
    private static String CALENDARS_ACCOUNT_TYPE = "com.android.exchange";
    private static String CALENDARS_DISPLAY_NAME = "测试账户";

    private static CalendarMethod calendarMethod;
    public synchronized static CalendarMethod getCalendarMethod(){
        if (calendarMethod==null){
            calendarMethod=new CalendarMethod();
        }
        return calendarMethod;
    }

    //添加test账户
    private long addAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);
        Uri calendarUri = Uri.parse(calenderURL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();
        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }

    //初始化账户
    private long initAccount(Context context){
        long accountId;
        if (checkAccount(context)<0){
            accountId=addAccount(context);
        }else {
            accountId=checkAccount(context);
        }
        return accountId;
    }

    //添加事件
    public long addCalendarEvent(Context context,String title, String description, long beginTime){
        // 获取日历账户的id
        long calId = initAccount(context);
        if (calId < 0) {
            // 获取账户id失败直接返回，添加日历事件失败
            return -1;
        }else {
            Calendar mCalendar = Calendar.getInstance();
            ContentValues event = new ContentValues();

            //设置开始时间
            mCalendar.setTimeInMillis(beginTime);
            long start = mCalendar.getTime().getTime();

            //设置终止时间
            mCalendar.setTimeInMillis(start + 60 * 60 * 1000);
            long end = mCalendar.getTime().getTime();

            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.CALENDAR_ID, calId);
            event.put(CalendarContract.Events.DTSTART, start);
            event.put(CalendarContract.Events.DTEND, end);
            event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
            event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Beijing");  //这个是时区，必须有，

            //添加事件
            Uri newEvent = context.getContentResolver().insert(Uri.parse(calenderEventURL), event);

            if (newEvent == null) {
                // 添加日历事件失败直接返回
                return -1;
            }

            //事件提醒的设定
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
            long calendarEventId = ContentUris.parseId(newEvent);

            // 提前10分钟有提醒
            values.put(CalendarContract.Reminders.MINUTES, 0);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = context.getContentResolver().insert(Uri.parse(calenderReminderURL), values);
            if (uri == null) {
                // 添加闹钟提醒失败直接返回
                return -1;
            }
            return calendarEventId;
        }
    }

    //删除事件
    public void deleteEvent(Context context,long calendarEventId){
        Uri deleteUri=ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,calendarEventId);
        context.getContentResolver().delete(deleteUri,null,null);
    }

    //修改事件
    public void updateEvent(Context context, long calendarEventId, String title, String description){
        Uri updateUri=ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarEventId);
        ContentValues newEvent=new ContentValues();
        newEvent.put(CalendarContract.Events.TITLE, title);
        newEvent.put(CalendarContract.Events.DESCRIPTION, description);
        context.getContentResolver().update(updateUri,newEvent,null,null);
    }

    //修改事件时间
    public void updateEventTime(Context context, long calendarEventId, long start, long end){
        Uri updateUri=ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarEventId);
        ContentValues newEvent=new ContentValues();
        newEvent.put(CalendarContract.Events.DTSTART, start);
        newEvent.put(CalendarContract.Events.DTEND, end);
        context.getContentResolver().update(updateUri,newEvent,null,null);
    }


    public long convertTimeToMillis(int year, int month, int day, int hour, int minute, int second) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(year, month, day, hour, minute);
        return mCalendar.getTime().getTime();
    }

    public String getFormattedCurrentTime(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
}
