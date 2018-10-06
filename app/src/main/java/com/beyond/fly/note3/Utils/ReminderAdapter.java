package com.beyond.fly.note3.Utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.fly.note3.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by beyond on 17-12-10.
 */

public class ReminderAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<HashMap<String,Object>> data;
    private int[] selectedPosition=new int[]{-1,-1,-1};

    public ReminderAdapter(Context context, ArrayList<HashMap<String,Object>> data){
        this.context=context;
        this.data=data;
    }

    //接口，向Fragment3中传递数据
    public interface OnItemClickListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);
    }
    private OnItemClickListener myOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener myOnItemClickListener){
        this.myOnItemClickListener=myOnItemClickListener;
    }

    public void markSelectedPosition(int whichList, int position){
        selectedPosition[whichList]=position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        TextView textView10=((MyViewHolder)holder).textView;
        final View parent=((MyViewHolder)holder).parent;
        //匹配View和数据
        textView10.setText(data.get(position).get("number").toString());

        //设置点击接口，主要用来向Fragment3中传递position
        if (myOnItemClickListener!=null) {
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getLayoutPosition();
                    myOnItemClickListener.onItemClick(view, position);
                }
            });
        }

        //查看当前时间
        Date date=new Date(System.currentTimeMillis());
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);

        //初始化selectedPosition
        if (selectedPosition[0]==-1){
            selectedPosition[0]=0;
        }
        if (selectedPosition[1]==-1){
            selectedPosition[1]=calendar.get(Calendar.HOUR_OF_DAY);
        }
        if (selectedPosition[2]==-1){
            selectedPosition[2]=calendar.get(Calendar.MINUTE);
        }

        //设置item背景
        if (data.size()==5) {
            if (selectedPosition[0]==position){
                parent.setBackground(context.getDrawable(R.drawable.reminder_selected));
            }else {
                parent.setBackground(context.getDrawable(R.drawable.reminder_not_selected));
                if (calendar.get(Calendar.DAY_OF_MONTH) == (int)data.get(position).get("number")){
                    parent.setBackground(context.getDrawable(R.drawable.reminder_current_date));
                }
            }

        }else if (data.size()==24){
            if (selectedPosition[1]==position){
                parent.setBackground(context.getDrawable(R.drawable.reminder_selected));
            }else {
                parent.setBackground(context.getDrawable(R.drawable.reminder_not_selected));
                if (calendar.get(Calendar.HOUR_OF_DAY) == (int)data.get(position).get("number")){
                    parent.setBackground(context.getDrawable(R.drawable.reminder_current_date));
                }
            }
        }else if (data.size()==60){
            if (selectedPosition[2]==position){
                parent.setBackground(context.getDrawable(R.drawable.reminder_selected));
            }else {
                parent.setBackground(context.getDrawable(R.drawable.reminder_not_selected));
                if (calendar.get(Calendar.MINUTE) == (int)data.get(position).get("number")){
                    parent.setBackground(context.getDrawable(R.drawable.reminder_current_date));
                }
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup=(ViewGroup) LayoutInflater.from(context).inflate(R.layout.reminder_item,parent,false);
        return new MyViewHolder(viewGroup);
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        View parent;
        TextView textView;

        private MyViewHolder(View view){
            super(view);
            textView=view.findViewById(R.id.textView10);
            parent=view.findViewById(R.id.constraintLayout_reminder_item);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
