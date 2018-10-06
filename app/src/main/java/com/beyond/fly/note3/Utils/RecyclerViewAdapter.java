package com.beyond.fly.note3.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.fly.note3.R;
import com.beyond.fly.note3.ShareActivity;

import java.util.ArrayList;

/**
 * Created by beyond on 17-12-1.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<MyHashmap> data;
    private String creatorName;
    private StaggeredGridLayoutManager.LayoutParams layoutParams;
    private int clickedPosition=-1;
    public void setClickedPosition(int position){
        clickedPosition=position;
    }

    public RecyclerViewAdapter(Context context,ArrayList<MyHashmap> data){
        this.context=context;
        this.data=data;
    }

    //设置接口
    public interface OnItemClickListener{
        void OnItemClick(View view,int position);
        void OnItemLongClick(View view,int position);
    }
    private OnItemClickListener myOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener myOnItemClickListener){
        this.myOnItemClickListener=myOnItemClickListener;
    }

    //设置删除按钮接口
    public interface OnDelClickListener{
        void OnClick(View view,int position);
    }
    private OnDelClickListener onDelClickListener;
    public void setOnDelClickListener(OnDelClickListener onDelClickListener){
        this.onDelClickListener=onDelClickListener;
    }

    //设置搜索按钮接口
    public interface OnSearchClickListener{
        void onClick(View view,int position);
    }
    private OnSearchClickListener onSearchClickListener;
    public void setOnSearchClickListener(OnSearchClickListener onSearchClickListener){
        this.onSearchClickListener=onSearchClickListener;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder,int position) {
        final View constraintLayout=((MyViewHolder)holder).constraintLayout;
        TextView textView=((MyViewHolder)holder).textView;
        TextView textView11=((MyViewHolder)holder).textView11;
        TextView textView13=((MyViewHolder)holder).textView13;
        TextView textView18=((MyViewHolder)holder).textView18;
        final String htmlText;

        //合并显示title和content,未启用被下面的覆盖了
        if (!(data.get(holder.getAdapterPosition()).get("title").toString().equals("")||data.get(holder.getAdapterPosition()).get("title").toString().equals("null")) && (data.get(holder.getAdapterPosition()).get("content").toString().equals("")||data.get(holder.getAdapterPosition()).get("content").toString().equals("null"))){
            htmlText="<b><font color='black'>"+data.get(holder.getAdapterPosition()).get("title").toString()+"</font></b>";
        }else if ((data.get(holder.getAdapterPosition()).get("title").toString().equals("")||data.get(holder.getAdapterPosition()).get("title").equals("null")) && !(data.get(holder.getAdapterPosition()).get("content").toString().equals("")||data.get(holder.getAdapterPosition()).get("content").equals("null"))){
            htmlText=data.get(holder.getAdapterPosition()).get("content").toString();
        }else if ((data.get(holder.getAdapterPosition()).get("title").toString().equals("")||data.get(holder.getAdapterPosition()).get("title").equals("null")) && (data.get(holder.getAdapterPosition()).get("content").toString().equals("")||data.get(holder.getAdapterPosition()).get("content").equals("null"))) {
            htmlText="BLANK";
        }else {
            htmlText = "<b><font color='black'>" + data.get(holder.getAdapterPosition()).get("title").toString() + "</font></b><br>" +
                        data.get(holder.getAdapterPosition()).get("content").toString();
        }
        textView.setText(Html.fromHtml(htmlText));

        //在用
        textView.setAutoLinkMask(0);
        textView.setTextColor(Color.BLACK);
        if (!(data.get(holder.getAdapterPosition()).get("title").toString().equals("")||data.get(holder.getAdapterPosition()).get("title").toString().equals("null"))){
            //只显示title
            textView.setText(data.get(holder.getAdapterPosition()).get("title").toString());
        }else if (!(data.get(holder.getAdapterPosition()).get("content").toString().equals("")||data.get(holder.getAdapterPosition()).get("content").toString().equals("null"))){

            if (data.get(holder.getAdapterPosition()).get("content").toString().contains("http")) {
                int httpPosition = data.get(holder.getAdapterPosition()).get("content").toString().indexOf("http");
                if (httpPosition == 0||httpPosition==1 ) {
                    textView.setText(data.get(holder.getAdapterPosition()).get("content").toString());
                } else {
                    textView.setText(data.get(holder.getAdapterPosition()).get("content").toString().substring(0,httpPosition));
                }
            }else {
                textView.setText(data.get(holder.getAdapterPosition()).get("content").toString());
            }
        }else {
            textView.setText("BLANK");
        }

        //瀑布流，跨行显示
        layoutParams=(StaggeredGridLayoutManager.LayoutParams)((MyViewHolder) holder).itemView.getLayoutParams();
        if (holder.getAdapterPosition()==clickedPosition||data.get(holder.getAdapterPosition()).get("content").toString().length()>300){
            layoutParams.setFullSpan(true);
            textView11.setVisibility(View.VISIBLE);
            textView13.setVisibility(View.VISIBLE);
            textView18.setVisibility(View.VISIBLE);

            //合并显示title和content
            textView.setAutoLinkMask(Linkify.WEB_URLS);
            textView.setText(Html.fromHtml(htmlText));

            //显示分享者
            if (!data.get(holder.getLayoutPosition()).get("creator").toString().equals(HttpUtils.onlineTableName)) {
                if (creatorName==null) {
                    //首次点击获得CreatorName
                    new GetCreatorName(data.get(holder.getLayoutPosition()).get("creator").toString(), clickedPosition).execute();
                } else {
                    //网上获得CreatorName后显示
                    textView18.setText("From:" + creatorName);
                    creatorName = null;
                }
            }else {
                textView18.setText("Share");
            }
        }else {
            layoutParams.setFullSpan(false);
            textView11.setVisibility(View.GONE);
            textView13.setVisibility(View.GONE);
            textView18.setVisibility(View.GONE);
        }

        //setColor
        GradientDrawable gradientDrawable=new GradientDrawable();
        gradientDrawable.setCornerRadius(25);
        if (data.get(holder.getAdapterPosition()).get("creator").toString().equals(MyDatabaseUtils.tableName)) {
            if (data.get(holder.getAdapterPosition()).get("type").toString().equals("remind")) {
                gradientDrawable.setStroke(1, context.getResources().getColor(R.color.colorSaddleBrown));
            } else if (data.get(holder.getAdapterPosition()).get("type").toString().equals("share")) {
                gradientDrawable.setStroke(1, Color.GRAY);
            }
        }else {
            gradientDrawable.setStroke(2, context.getResources().getColor(R.color.color_10min));
        }
        long selectedDate=Long.parseLong(data.get(holder.getAdapterPosition()).get("remind_date").toString());
        //下面这句不能删，不然只渐变会没有立体阴影
        gradientDrawable.setColor(context.getResources().getColor(ColorUtils.getMyColor(selectedDate)));
        //下面的注释掉是因为不好看
        //gradientDrawable.setStroke(1,Color.GRAY,8,2);
        //gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        //gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
        //gradientDrawable.setColors(new int[]{context.getResources().getColor(ColorUtils.getMyColor(selectedDate)),Color.WHITE});
        constraintLayout.setBackground(gradientDrawable);
        constraintLayout.setElevation(4);

        //长按跳转到Reminder
        //adapter中直接发送intent但是有bug：长按之后控制MainActivity.instance.finish，然后再点击就会崩溃，改用接口方法
        /*
        constraintLayout.setLongClickable(true);
        constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                transDataToReminderAndJump(position);
                return false;
            }
        });
        */

        //删除键
        textView11.setPadding(20,0,20,0);
        textView11.setBackground(context.getDrawable(R.drawable.selector_1));
        textView11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getLayoutPosition();
                onDelClickListener.OnClick(view,position);
            }
        });

        //搜索键
        textView13.setPadding(20,0,20,0);
        textView13.setBackground(context.getDrawable(R.drawable.selector_1));
        textView13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getLayoutPosition();
                onSearchClickListener.onClick(view,position);
            }
        });
        //分享键
        textView18.setPadding(20,0,20,0);
        textView18.setBackground(context.getDrawable(R.drawable.selector_1));
        textView18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, ShareActivity.class);
                intent.setAction(ShareActivity.SHARE_ACTIVITY);
                intent.putExtra("title",data.get(holder.getAdapterPosition()).get("title").toString());
                intent.putExtra("content",data.get(holder.getAdapterPosition()).get("content").toString());
                intent.putExtra("dates",(long)data.get(holder.getAdapterPosition()).get("dates"));
                intent.putExtra("remind_date",(long)data.get(holder.getAdapterPosition()).get("remind_date"));
                intent.putExtra("_id",(int)data.get(holder.getAdapterPosition()).get("_id"));
                intent.putExtra("calendar_event_id",(long)data.get(holder.getAdapterPosition()).get("calendar_event_id"));
                intent.putExtra("type",data.get(holder.getAdapterPosition()).get("type").toString());
                intent.putExtra("color",(int)data.get(holder.getAdapterPosition()).get("color"));
                intent.putExtra("creator",data.get(holder.getAdapterPosition()).get("creator").toString());
                context.startActivity(intent);
            }
        });

        //设置长按接口返回到fragment2的RecyclerAdapter,之后fragment2再设置接口返回到MainActivity
        if (myOnItemClickListener!=null){
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getLayoutPosition();
                    myOnItemClickListener.OnItemClick(view,position);
                    //alertDetail(context,Html.fromHtml(htmlText));
                }
            });
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position=holder.getLayoutPosition();
                    myOnItemClickListener.OnItemLongClick(view,position);
                    return true;
                }
            });

            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getLayoutPosition();
                    myOnItemClickListener.OnItemClick(view,position);

                    //alertDetail(context,Html.fromHtml(htmlText));
                }
            });
            constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position=holder.getLayoutPosition();
                    myOnItemClickListener.OnItemLongClick(view,position);
                    return true;
                }
            });
        }
    }

    class GetCreatorName extends AsyncTask<String,String,String>{
        private ArrayList<MyHashmap> creators;
        private String creator;
        private int position;
        public GetCreatorName(String creator,int position){
            this.creator=creator;
            this.position=position;
        }
        @Override
        protected String doInBackground(String... strings) {
            HttpUtils httpUtils=new HttpUtils();
            creators=httpUtils.readFriendDataWithHttpUrlConnection("title",new String[]{creator});

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (creators.size()!=0){
                ContactUtils contactUtils=new ContactUtils();
                if (creators.get(0).get("content").toString().length()==11) {
                    creatorName = contactUtils.getContactsName(context, creators.get(0).get("content").toString());
                }else {
                    creatorName="";
                }
                notifyItemChanged(position);
            }
        }
    }

    public StaggeredGridLayoutManager.LayoutParams getStagGridLayoutParams(){
        return layoutParams;
    }
    private void alertDetail(Context context,Spanned detail){
        AlertDialog.Builder builder=new AlertDialog.Builder(context,R.style.NoBackgroundDialog);
        View dialogView=LayoutInflater.from(context).inflate(R.layout.note_detail,null);
        TextView textView12=dialogView.findViewById(R.id.textView12);
        textView12.setText(detail);
        builder.setView(dialogView)
                .setCancelable(true);
        builder.create().show();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.recycler_view_item,parent,false);
        return new MyViewHolder(view);
    }
    class MyViewHolder extends RecyclerView.ViewHolder{

        View constraintLayout;
        TextView textView;
        TextView textView11;
        TextView textView13;
        TextView textView18;

        private MyViewHolder(View view){
            super(view);
            constraintLayout=view.findViewById(R.id.constraintLayout);
            textView=view.findViewById(R.id.textView);
            textView11=view.findViewById(R.id.textView11);
            textView13=view.findViewById(R.id.textView13);
            textView18=view.findViewById(R.id.textView18);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
