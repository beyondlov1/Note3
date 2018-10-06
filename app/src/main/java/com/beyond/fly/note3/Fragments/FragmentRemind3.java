package com.beyond.fly.note3.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.beyond.fly.note3.MainActivity;
import com.beyond.fly.note3.R;
import com.beyond.fly.note3.Utils.CalendarMethod;
import com.beyond.fly.note3.Utils.ColorUtils;
import com.beyond.fly.note3.Utils.HttpUtils;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;
import com.beyond.fly.note3.Utils.MyFragment;
import com.beyond.fly.note3.Utils.ReminderAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by beyond on 17-12-9.
 */

public class FragmentRemind3 extends MyFragment {

    private RecyclerView[] recyclerViews=new RecyclerView[3];
    private Button button;
    private ArrayList<ArrayList<HashMap<String,Object>>> datas;
    private ReminderAdapter[] reminderAdapters=new ReminderAdapter[3];
    private Context context;
    private HttpUtils httpUtils;
    private Calendar calendar=Calendar.getInstance();
    private int selectedYear,selectedMonth,selectedDayOfMonth,selectedHourOfDay,selectedMinute,selectedSecond;

    private MyDatabaseUtils myDatabaseUtils;
    private String[] keysWithoutId = new String[] {"title","content","dates","remind_date","color","calendar_event_id","type","creator"};
    private Object[] values;
    private String NULL="com.beyond.action.NULL";
    private Intent intent=new Intent("com.beyond.action.NULL");
    private int lastClickPosition[]=new int[]{-1,-1,-1};

    //设置button背景
    private GradientDrawable normalGradientDrawable;
    private GradientDrawable pressedGradientDrawable;
    private StateListDrawable stateListDrawable;

    //接口
    public interface OnButtonClickListener{
        void onClick(View view);
    }
    private OnButtonClickListener myOnButtonClickListener;
    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener){
        myOnButtonClickListener=onButtonClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        datas=new ArrayList<>();
        context=getActivity();

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_3_set_remind_time, container, false);
        initViews(viewGroup);

        httpUtils=new HttpUtils();
        myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
        initSelectedTime();
        initLastSelectedPosition();
        writeDataToRecyclerView();
        return viewGroup;
    }
    private ArrayList<ArrayList<HashMap<String,Object>>> getDatas(){
        ArrayList<ArrayList<HashMap<String,Object>>> datas=new ArrayList<>();
        ArrayList<HashMap<String,Object>> data=new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        for (int i=0;i<5;i++){
            long myDate=System.currentTimeMillis()+i*24*60*60*1000;
            Date date= new Date(myDate);
            calendar.setTime(date);

            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("number",calendar.get(Calendar.DAY_OF_MONTH));
            data.add(hashMap);
        }
        datas.add(data);

        data=new ArrayList<>();
        for (int i=0;i<24;i++){
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("number",i);
            data.add(hashMap);
        }
        datas.add(data);

        data=new ArrayList<>();
        for (int i=0;i<60;i++){
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("number",i);
            data.add(hashMap);
        }
        datas.add(data);

        return datas;
    }

    private void initViews(ViewGroup viewGroup){
        recyclerViews[0]=viewGroup.findViewById(R.id.recyclerView3);
        recyclerViews[1]=viewGroup.findViewById(R.id.recyclerView4);
        recyclerViews[2]=viewGroup.findViewById(R.id.recyclerView5);
        GradientDrawable gradientDrawable=new GradientDrawable();
        if (ColorUtils.getMyColor(intent.getLongExtra("remind_date", 0))!=R.color.colorWhite) {
            gradientDrawable.setStroke(3, context.getResources().getColor(ColorUtils.getMyColor(intent.getLongExtra("remind_date", 0))));
        }else {
            gradientDrawable.setStroke(3, Color.GRAY);
        }
        for (int i=0;i<3;i++){
            recyclerViews[i].setBackground(gradientDrawable);
        }

        initButtonBackground();
        button=viewGroup.findViewById(R.id.button_remind);
        button.setText(R.string.remind);
        button.setBackground(normalGradientDrawable);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Handler handler=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what==0) {
                            Toast.makeText(context, "update", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        if (intent.getAction().equals(MainActivity.REMINDER_CREATE)) {
                            addToCalendarAndWriteToDatabase();
                            Intent intent=new Intent(MainActivity.REFRESH);
                            context.sendBroadcast(intent);
                        }else if (intent.getAction().equals(MainActivity.REMINDER_UPDATE)) {

                            updateToCalendarAndUpdateToDatabase();
                            Intent intent=new Intent(MainActivity.REFRESH);
                            context.sendBroadcast(intent);

                            Message message=new Message();
                            message.what=0;
                            handler.sendMessage(message);

                        }else if (intent.getAction().equals(NULL)){
                            fastAddToCalendarAndWriteToDatabase();
                        }
                    }
                };
                handler.post(runnable);

                if (myOnButtonClickListener!=null){
                    myOnButtonClickListener.onClick(view);
                }
            }
        });

        GradientDrawable button6Background=new GradientDrawable();
        button6Background.setCornerRadius(100);
        button6Background.setColor(context.getResources().getColor(R.color.cardview_light_background));
        button6Background.setStroke(2,context.getResources().getColor(R.color.colorSaddleBrown));
        Button button6=viewGroup.findViewById(R.id.button6);
        if (getMyCalendarEventId()==0||getMyCalendarEventId()==-1){
            button6.setVisibility(View.GONE );
        }
        button6.setBackground(button6Background);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler=new Handler();
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        if (getMyCalendarEventId()!=0) {
                            CalendarMethod.getCalendarMethod().deleteEvent(context, getMyCalendarEventId());

                            values = new Object[] {getMyTitle(),getMyContent(),getCurrentTimeMillis(), getSelectedTimeMillis(),getMyColor(),0
                                    ,getType(),getCreator()};
                            myDatabaseUtils.updateDataToDatabase(MyDatabaseUtils.tableName, getMyId(),keysWithoutId, values);
                            Intent intent=new Intent(MainActivity.REFRESH);
                            context.sendBroadcast(intent);

                            new CreateOnline(keysWithoutId,values).execute();
                        }
                    }
                };
                handler.post(runnable);

                //接口，转到Fragment2
                if (myOnButtonClickListener!=null){
                    myOnButtonClickListener.onClick(view);
                }
            }
        });

        Button button5=viewGroup.findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.getAction().equals(MainActivity.REMINDER_CREATE)) {
                    long calendarEventId= CalendarMethod.getCalendarMethod().addCalendarEvent(context,getMyTitle(),getMyContent(),
                            getSelectedTimeMillis());

                    values = new Object[] {getMyTitle(),getMyContent(),getCurrentTimeMillis(), getSelectedTimeMillis(),getMyColor(),calendarEventId
                            ,getType(),getCreator()};
                    myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName, keysWithoutId, values);
                    Intent intent=new Intent(MainActivity.REFRESH);
                    context.sendBroadcast(intent);

                    new UpdateOnline(keysWithoutId,values).execute();
                }
            }
        });
    }

    private void initButtonBackground(){
        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setCornerRadius(100);
        normalGradientDrawable.setColor(context.getResources().getColor(R.color.cardview_light_background));
        normalGradientDrawable.setStroke(2,context.getResources().getColor(R.color.colorSaddleBrown));
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setCornerRadius(100);
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.colorSaddleBrown));
    }
    private void setButtonBackground(int color){
        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setCornerRadius(100);
        normalGradientDrawable.setColor(context.getResources().getColor(R.color.cardview_light_background));
        normalGradientDrawable.setStroke(2,context.getResources().getColor(color));
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setCornerRadius(100);
        pressedGradientDrawable.setColor(context.getResources().getColor(color));
        stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        button.setBackground(stateListDrawable);
    }

    //在别人的online table上添加
    class CreateOnline extends AsyncTask<String,String,String>{
        private String[] keys;
        private Object[] values;
        private CreateOnline(String[] keys,Object[] values){
            this.keys=keys;
            this.values=values;
        }
        @Override
        protected String doInBackground(String... strings) {
            String[] newKeys=new String[10];
            Object[] newValues=new Object[10];
            for (int i = 0; i <keys.length ; i++) {
                newKeys[i]=keys[i];
                newValues[i]=values[i];
            }

            newKeys[9]="tablename";
            newValues[9]="note863604031123826";//my father's phoneId
            httpUtils.createWithHttpUrlConnection(newKeys,newValues);
            return null;
        }
    }

    class UpdateOnline extends AsyncTask<String,String,String>{
        private String[] keys;
        private Object[] values;
        private UpdateOnline(String[] keys,Object[] values){
            this.keys=keys;
            this.values=values;
        }
        @Override
        protected String doInBackground(String... strings) {
            String[] newKeys=new String[10];
            Object[] newValues=new Object[10];
            for (int i = 0; i <keys.length ; i++) {
                newKeys[i]=keys[i];
                newValues[i]=values[i];
            }

            newKeys[9]="tablename";
            newValues[9]=HttpUtils.onlineTableName;
            httpUtils.updateItemWithHttpUrlConnection(newKeys,newValues);
            return null;
        }
    }


    private void writeDataToRecyclerView(){
        datas=getDatas();
        for (int i=0;i<3;i++) {
            reminderAdapters[i] = new ReminderAdapter(context, datas.get(i));
            recyclerViews[i].setAdapter(reminderAdapters[i]);
        }
        recyclerViews[0].setLayoutManager(new GridLayoutManager(context,5));
        recyclerViews[1].setLayoutManager(new GridLayoutManager(context,8));
        recyclerViews[2].setLayoutManager(new GridLayoutManager(context,10));

        reminderAdapters[0].setOnItemClickListener(new ReminderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedDayOfMonth=(int)datas.get(0).get(position).get("number");

                //设置点击某一条目后上一条目恢复原来状态
                //原理是：点击后将Position传递给Adapter中的SelectedPosition，然后刷新，通过判断selectedPosition合Position是否相等绘制背景
                if (lastClickPosition[0]!=-1){
                    reminderAdapters[0].notifyItemChanged(lastClickPosition[0]);
                }
                reminderAdapters[0].markSelectedPosition(0,position);
                reminderAdapters[0].notifyDataSetChanged();
                lastClickPosition[0]=position;

                //设置button背景
                setButtonBackground(ColorUtils.getMyColor(getSelectedTimeMillis()));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        reminderAdapters[1].setOnItemClickListener(new ReminderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedHourOfDay=(int)datas.get(1).get(position).get("number");

                //设置点击某一条目后上一条目恢复原来状态
                if (lastClickPosition[1]!=-1){
                    reminderAdapters[1].notifyItemChanged(lastClickPosition[1]);
                }
                reminderAdapters[1].markSelectedPosition(1,position);
                reminderAdapters[1].notifyItemChanged(position);
                lastClickPosition[1]=position;

                //设置button背景
                setButtonBackground(ColorUtils.getMyColor(getSelectedTimeMillis()));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        reminderAdapters[2].setOnItemClickListener(new ReminderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedMinute=(int)datas.get(2).get(position).get("number");

                //设置点击某一条目后上一条目恢复原来状态
                if (lastClickPosition[2]!=-1) {
                    reminderAdapters[2].notifyItemChanged(lastClickPosition[2]);
                }
                reminderAdapters[2].markSelectedPosition(2,position);
                reminderAdapters[2].notifyItemChanged(position);
                lastClickPosition[2]=position;

                //设置button背景
                setButtonBackground(ColorUtils.getMyColor(getSelectedTimeMillis()));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void initSelectedTime(){
        Date date=new Date(System.currentTimeMillis());
        calendar.setTime(date);

        selectedYear=calendar.get(Calendar.YEAR);
        selectedMonth=calendar.get(Calendar.MONTH);
        selectedDayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        selectedHourOfDay=calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute=calendar.get(Calendar.MINUTE);
        selectedSecond=calendar.get(Calendar.SECOND);

    }
    private void initLastSelectedPosition(){
        lastClickPosition[0]=selectedDayOfMonth;
        lastClickPosition[1]=selectedHourOfDay;
        lastClickPosition[2]=selectedMinute;
    }

    public void initIntent(Intent intent){
        this.intent=intent;
    }

    private void fastAddToCalendarAndWriteToDatabase(){
        long calendarEventId= CalendarMethod.getCalendarMethod().addCalendarEvent(context,null,null,
                getSelectedTimeMillis());

        values = new Object[] {null,null,getCurrentTimeMillis(), getSelectedTimeMillis()
                ,getMyColor(),calendarEventId,getType(),getCreator()};
        myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,keysWithoutId, values);
    }
    private void addToCalendarAndWriteToDatabase(){
        long calendarEventId= CalendarMethod.getCalendarMethod().addCalendarEvent(context,getMyTitle(),getMyContent(),
                getSelectedTimeMillis());

        values = new Object[] {getMyTitle(),getMyContent(),getCurrentTimeMillis(), getSelectedTimeMillis(),getMyColor(),calendarEventId
                ,getType(),getCreator()};
        myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName, keysWithoutId, values);
    }
    private void updateToCalendarAndUpdateToDatabase(){
        long calendarEventId;
        if (getMyCalendarEventId()==0){
            calendarEventId= CalendarMethod.getCalendarMethod().addCalendarEvent(context,getMyTitle(),getMyContent(), getSelectedTimeMillis());
        }else {
            CalendarMethod.getCalendarMethod().updateEventTime(context, getMyCalendarEventId(), getSelectedTimeMillis(), getSelectedTimeMillis() + 60 * 60 * 1000);
            calendarEventId=getMyCalendarEventId();
        }
        values = new Object[] {getMyTitle(),getMyContent(),getCurrentTimeMillis(), getSelectedTimeMillis(),getMyColor(),calendarEventId
                ,getType(),getCreator()};
        myDatabaseUtils.updateDataToDatabase(MyDatabaseUtils.tableName,getMyId(),keysWithoutId, values);
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
    private long getSelectedTimeMillis(){
        Date currentDate=new Date(System.currentTimeMillis());
        calendar.setTime(currentDate);
        if (selectedDayOfMonth<calendar.get(Calendar.DAY_OF_MONTH)){
            calendar.setTimeInMillis(System.currentTimeMillis()+30*24*60*60*1000L);
            selectedMonth=calendar.get(Calendar.MONTH);
            if (selectedMonth==0){
                calendar.setTimeInMillis(System.currentTimeMillis()+12*30*24*60*60*1000L);
                selectedYear=calendar.get(Calendar.YEAR);
            }
        }
        calendar.set(selectedYear,selectedMonth,selectedDayOfMonth,selectedHourOfDay,selectedMinute,selectedSecond);
        return calendar.getTimeInMillis();
    }
    private int getMyColor(){
        return ColorUtils.getMyColor(getSelectedTimeMillis());
    }
    private long getMyCalendarEventId(){
        return intent.getLongExtra("calendar_event_id",-1);
    }
    private String getType(){return "remind";}
    private String getCreator(){return intent.getStringExtra("creator");}
}
