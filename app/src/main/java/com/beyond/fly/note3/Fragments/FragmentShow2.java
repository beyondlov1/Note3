package com.beyond.fly.note3.Fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.fly.note3.ContentActivity;
import com.beyond.fly.note3.MainActivity;
import com.beyond.fly.note3.R;
import com.beyond.fly.note3.Utils.CalendarMethod;
import com.beyond.fly.note3.Utils.MyDatabaseUtils;
import com.beyond.fly.note3.Utils.MyFragment;
import com.beyond.fly.note3.Utils.MyHashmap;
import com.beyond.fly.note3.Utils.RecyclerViewAdapter;

import java.util.ArrayList;

/**
 * Created by beyond on 17-12-1.
 */

public class FragmentShow2 extends MyFragment {

    private ArrayList<MyHashmap> data;
    private RecyclerView recyclerView;
    private Context context;
    private MyDatabaseUtils myDatabaseUtils;
    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ((MainActivity.REFRESH).equals(intent.getAction())){
                refresh();
            }
        }
    };
    private int lastClickPosition=-1;

    //设置接口
    public interface OnItemLongClickListener {
        void OnItemLongClick(ArrayList<MyHashmap> data, int position);
    }
    private OnItemLongClickListener onItemLongClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        context=getActivity();
        myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
        //FragmentManager执行replace或者remove方法后data数据还在，所以要将data设置为空
        data=new ArrayList<>();
        IntentFilter intentFilter=new IntentFilter(MainActivity.REFRESH);
        context.registerReceiver(broadcastReceiver,intentFilter);

        ViewGroup viewGroup=(ViewGroup)inflater.inflate(R.layout.fragment_2,container,false);
        initViews(viewGroup);

        writeDataToRecyclerView();

        return viewGroup;
    }

    private void initViews(ViewGroup viewGroup){
        recyclerView=viewGroup.findViewById(R.id.recyclerView2);
    }

    private void writeDataToRecyclerView(){
        data=getData();
        final RecyclerViewAdapter recyclerViewAdapter=new RecyclerViewAdapter(context,data);

        recyclerView.setAdapter(recyclerViewAdapter);
        final StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        recyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {

                recyclerViewAdapter.setClickedPosition(position);
                if (lastClickPosition!=-1) {
                    recyclerViewAdapter.notifyItemChanged(lastClickPosition);
                }
                recyclerViewAdapter.notifyItemChanged(position);

                //将选中的item滚动到顶部
                int firstItem=recyclerView.getChildAt(0).findViewById(R.id.constraintLayout).getTop();
                int n=view.getTop();
                if (recyclerView.computeVerticalScrollExtent()+recyclerView.computeVerticalScrollOffset()<recyclerView.computeVerticalScrollRange()) {
                    //System.out.println(n);
                    staggeredGridLayoutManager.scrollToPosition(position);
                    //staggeredGridLayoutManager.scrollToPositionWithOffset(position, (n - firstItem));
                }else {
                    staggeredGridLayoutManager.scrollToPosition(position);
                }

                lastClickPosition=position;
            }

            @Override
            public void OnItemLongClick(View view, int position) {
                if (onItemLongClickListener !=null){
                    onItemLongClickListener.OnItemLongClick(data,position);
                }
            }
        });
        recyclerViewAdapter.setOnDelClickListener(new RecyclerViewAdapter.OnDelClickListener() {
            @Override
            public void OnClick(View view, int position) {
                myDatabaseUtils.deleteDataFromDatabase(MyDatabaseUtils.tableName,"_id",data.get(position).get("_id"));
                if ((long)data.get(position).get("calendar_event_id")!=0){
                    CalendarMethod.getCalendarMethod().deleteEvent(context,(long)data.get(position).get("calendar_event_id"));
                }
                refresh();
            }
        });
        recyclerViewAdapter.setOnSearchClickListener(new RecyclerViewAdapter.OnSearchClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent=new Intent(context,ContentActivity.class);
                intent.setAction(ContentActivity.contentActivityAction);
                intent.putExtra("title",data.get(position).get("title").toString());
                intent.putExtra("content",data.get(position).get("content").toString());
                context.startActivity(intent);
            }
        });

        /*
        layoutManager=new GridLayoutManager(context,2);
        int spanSize=1;

        //设置接口返回到MainActivity
        recyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                final int pos=position;
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (position==pos) {
                            return 2;
                        }else {
                            return 1;
                        }
                    }
                });
                //recyclerViewAdapter.notifyItemChanged(position);
                recyclerViewAdapter.notifyItemRangeChanged(0,recyclerViewAdapter.getItemCount());
                recyclerView.scrollToPosition(position);
            }

            @Override
            public void OnItemLongClick(View view, int position) {
                if (onItemLongClickListener !=null){
                    onItemLongClickListener.OnItemLongClick(data,position);
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        */
    }
    private ArrayList<MyHashmap> getData(){

        ArrayList<MyHashmap> data;

        data=myDatabaseUtils.getDataFromDatabase(MyDatabaseUtils.tableName);

        return data;
    }

    public void refresh(){
        writeDataToRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(broadcastReceiver);
    }
}
