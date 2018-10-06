package com.beyond.fly.note3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.beyond.fly.note3.Fragments.SplashFragment1;

import java.util.ArrayList;

/**
 * Created by beyond on 2018/1/4.
 */

public class SplashActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments=new ArrayList<>();
    private int currentPage;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        //获取当前版本号
        int currentVersion=0;
        PackageInfo packageInfo=null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersion=packageInfo.versionCode;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        //获取保存的版本号
        SharedPreferences sharedPreferences=getSharedPreferences("beyond",MODE_PRIVATE);
        int lastVersion=sharedPreferences.getInt("version",0);
        //验证是否第一次启动
        if (currentVersion>lastVersion) {
            testCalendarPermission();
            fullScreen();
            setContentView(R.layout.splash_activity);
            initViews();
            sharedPreferences.edit().putInt("version",currentVersion).apply();
        }else {
            finish();
            //startMainActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startMainActivity();
    }

    class MyFragmentStateViewPagerAdapter extends FragmentStatePagerAdapter{
        private ArrayList<Fragment> fragments;
        private MyFragmentStateViewPagerAdapter(android.support.v4.app.FragmentManager fragmentManager, ArrayList<Fragment> fragments){
            super(fragmentManager);
            this.fragments=fragments;
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private void initViews(){
        viewPager=findViewById(R.id.splash_view_pager);
        android.support.v4.app.FragmentManager fragmentManager=getSupportFragmentManager();
        initFragments();
        MyFragmentStateViewPagerAdapter myFragmentStateViewPagerAdapter=new MyFragmentStateViewPagerAdapter(fragmentManager,fragments);
        viewPager.setAdapter(myFragmentStateViewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage=position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_CALENDAR)!= PackageManager.PERMISSION_GRANTED
                        &&ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED
                        &&ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashActivity.this,new String []{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS},0);
                }else {
                    finish();
                }
                return false;
            }
        });
    }

    private void fullScreen(){
        //设置全屏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
    }

    private void startMainActivity(){
        Intent intent=new Intent(context,MainActivity.class);
        intent.setAction(MainActivity.START);
        startActivity(intent);
    }

    private void initFragments(){
        SplashFragment1 fragment1=new SplashFragment1();
        Bundle bundle=new Bundle();
        bundle.putString("fragmentText","Note");
        bundle.putInt("fragmentBackgroundColor", getResources().getColor(R.color.colorBrown));
        fragment1.setArguments(bundle);

        fragments.add(fragment1);
    }

    private void testCalendarPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)!= PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
            //获取多个权限要在String[]中添加多个权限
            ActivityCompat.requestPermissions(this,new String []{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS},1);
            ActivityCompat.requestPermissions(this,new String []{Manifest.permission.READ_CALENDAR},1);
        }
    }

}
