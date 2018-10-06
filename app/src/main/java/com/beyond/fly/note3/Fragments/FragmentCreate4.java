package com.beyond.fly.note3.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.fly.note3.MainActivity;
import com.beyond.fly.note3.R;
import com.beyond.fly.note3.Utils.CalendarMethod;
import com.beyond.fly.note3.Utils.ColorUtils;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;
import com.beyond.fly.note3.Utils.MyFragment;

/**
 * Created by beyond on 17-12-5.
 */

public class FragmentCreate4 extends MyFragment implements View.OnClickListener{

    private String[] keys;
    private Object[] values;
    private EditText editText;
    private EditText editText3;
    private Button button;
    private TextView textView2,textView3,textView4,textView5,textView6;

    private Context context;

    private MyDatabaseUtils myDatabaseUtils;

    //接口，跳转到Fragment3
    public interface OnMoreClickListener{
        void onClick(View view,Intent intent);
    }
    private OnMoreClickListener onMoreClickListener;
    public void setOnMoreClickListener(OnMoreClickListener onMoreClickListener){
        this.onMoreClickListener=onMoreClickListener;
    }

    //接口，跳转到Fragment2
    public interface OnNormalClickListener{
        void OnClick(View view);
    }
    private OnNormalClickListener onNormalClickListener;
    public void setOnNormalClickListener(OnNormalClickListener onNormalClickListener){
        this.onNormalClickListener=onNormalClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup=(ViewGroup) inflater.inflate(R.layout.create_activity,container,false);
        context=getActivity();
        myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
        initViews(viewGroup);
        showKeyboard(editText);
        return viewGroup;
    }

    private void initViews(ViewGroup viewGroup) {
        editText = viewGroup.findViewById(R.id.editText);
        editText.requestFocus();
        editText3 = viewGroup.findViewById(R.id.editText3);
        button = viewGroup.findViewById(R.id.button4);

        textView2=viewGroup.findViewById(R.id.textView2);
        textView3=viewGroup.findViewById(R.id.textView3);
        textView4=viewGroup.findViewById(R.id.textView4);
        textView5=viewGroup.findViewById(R.id.textView5);
        textView6=viewGroup.findViewById(R.id.textView6);

        //更改 drawable/corner.xml
        //GradientDrawable gradientDrawable=(GradientDrawable)textView3.getBackground();
        //gradientDrawable.setColor(getResources().getColor(R.color.color_15min));

        textView2.setText(R.string.time_2);
        textView3.setText(R.string.time_3);
        textView4.setText(R.string.time_4);
        textView5.setText(R.string.time_5);
        textView6.setText(R.string.time_6);

        GradientDrawable normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setColor(Color.WHITE);
        normalGradientDrawable.setStroke(1,context.getResources().getColor(R.color.color_10min));
        normalGradientDrawable.setCornerRadius(40);
        GradientDrawable pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.color_10min));
        pressedGradientDrawable.setCornerRadius(40);
        StateListDrawable stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        textView2.setBackground(stateListDrawable);

        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setColor(Color.WHITE);
        normalGradientDrawable.setStroke(1,context.getResources().getColor(R.color.color_15min));
        normalGradientDrawable.setCornerRadius(40);
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.color_15min));
        pressedGradientDrawable.setCornerRadius(40);
        stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        textView3.setBackground(stateListDrawable);

        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setColor(Color.WHITE);
        normalGradientDrawable.setStroke(1,context.getResources().getColor(R.color.color_30min));
        normalGradientDrawable.setCornerRadius(40);
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.color_30min));
        pressedGradientDrawable.setCornerRadius(40);
        stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        textView4.setBackground(stateListDrawable);

        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setColor(Color.WHITE);
        normalGradientDrawable.setStroke(1,context.getResources().getColor(R.color.color_60min));
        normalGradientDrawable.setCornerRadius(40);
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.color_60min));
        pressedGradientDrawable.setCornerRadius(40);
        stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        textView5.setBackground(stateListDrawable);

        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setColor(Color.WHITE);
        normalGradientDrawable.setStroke(1,context.getResources().getColor(R.color.colorSaddleBrown));
        normalGradientDrawable.setCornerRadius(40);
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.colorSaddleBrown));
        pressedGradientDrawable.setCornerRadius(40);
        stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        textView6.setBackground(stateListDrawable);

        textView2.setOnClickListener(this);
        textView3.setOnClickListener(this);
        textView4.setOnClickListener(this);
        textView5.setOnClickListener(this);
        textView6.setOnClickListener(this);

        normalGradientDrawable=new GradientDrawable();
        normalGradientDrawable.setColor(Color.WHITE);
        normalGradientDrawable.setStroke(2,context.getResources().getColor(R.color.colorSaddleBrown));
        normalGradientDrawable.setCornerRadius(100);
        pressedGradientDrawable=new GradientDrawable();
        pressedGradientDrawable.setColor(context.getResources().getColor(R.color.colorSaddleBrown));
        pressedGradientDrawable.setCornerRadius(100);
        stateListDrawable=new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressedGradientDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled},normalGradientDrawable);
        button.setText(R.string.add);
        button.setHeight(40);
        button.setBackground(stateListDrawable);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getMyTitle();
                String content = getMyContent();

                long currentDate=System.currentTimeMillis();

                keys = new String[] {"title","content","dates","type","creator"};
                values = new Object[] {title,content,currentDate,"remind",getCreator()};
                myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,keys, values);

                editText.setText(null);
                editText3.setText(null);

                Intent intent=new Intent(MainActivity.REFRESH);
                context.sendBroadcast(intent);

                if (onNormalClickListener!=null){
                    onNormalClickListener.OnClick(view);
                }

            }
        });

    }

    private void showKeyboard(final View view){

        //要设定延迟，延迟可以是0，不然弹不出来
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager=(InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager!=null){
                    view.requestFocus();
                    inputMethodManager.showSoftInput(view,0);
                }
            }
        },0);
    }
    private void hideKeyboard(final View view){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager=(InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager!=null){
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
            }
        },0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textView2:
                //接口
                if (onNormalClickListener!=null){
                    onNormalClickListener.OnClick(view);
                }

                addToCalendarAndDatabaseAndRefresh(10);
                Toast.makeText(context,textView2.getText(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.textView3:
                //接口
                if (onNormalClickListener!=null){
                    onNormalClickListener.OnClick(view);
                }

                addToCalendarAndDatabaseAndRefresh(15);
                Toast.makeText(context,textView3.getText(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.textView4:
                //接口
                if (onNormalClickListener!=null){
                    onNormalClickListener.OnClick(view);
                }

                addToCalendarAndDatabaseAndRefresh(30);
                Toast.makeText(context,textView4.getText(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.textView5:
                //接口
                if (onNormalClickListener!=null){
                    onNormalClickListener.OnClick(view);
                }

                addToCalendarAndDatabaseAndRefresh(60);
                Toast.makeText(context,textView5.getText(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.textView6:
                if (onMoreClickListener!=null){
                    Intent intent=new Intent(context,MainActivity.class);
                    intent.setAction(MainActivity.REMINDER_CREATE);
                    intent.putExtra("title",getMyTitle());
                    intent.putExtra("content",getMyContent());
                    intent.putExtra("dates",getCurrentTimeMillis());
                    intent.putExtra("remind_date",0);
                    intent.putExtra("type","remind");
                    intent.putExtra("creator",getCreator());
                    onMoreClickListener.onClick(view,intent);

                    editText.setText(null);
                    editText3.setText(null);
                }
                break;
        }
    }

    private void addToCalendarAndDatabaseAndRefresh(final int minute){
        //添加日历提醒并获得EventId
        long calendarEventId=CalendarMethod.getCalendarMethod().addCalendarEvent(context,getMyTitle(),getMyContent(),
                getMyRemindTimeMills(minute));

        keys = new String[] {"title","content","dates","remind_date","color","calendar_event_id","type","creator"};
        values = new Object[] {getMyTitle(),getMyContent(),getCurrentTimeMillis(), getMyRemindTimeMills(minute)
                ,getMyColor(minute),calendarEventId,getType(),getCreator()};
        myDatabaseUtils.insertDataToDatabase( MyDatabaseUtils.tableName,keys, values);

        Intent intent=new Intent(MainActivity.REFRESH);
        context.sendBroadcast(intent);

        editText.setText(null);
        editText3.setText(null);
    }

    //getData
    private String getMyTitle(){
        return editText.getText().toString();
    }
    private String getMyContent(){
        return editText3.getText().toString();
    }
    private long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }
    private int getMyColor(int minute){
        return ColorUtils.getMyColor(minute*60*1000);
    }
    private long getMyRemindTimeMills(int minute){
        return System.currentTimeMillis()+minute*60*1000;
    }
    private String getType(){
        return "remind";
    }
    private String getCreator(){return MyDatabaseUtils.tableName;}
}
