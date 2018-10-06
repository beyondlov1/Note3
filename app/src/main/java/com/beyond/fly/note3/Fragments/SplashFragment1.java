package com.beyond.fly.note3.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.fly.note3.R;

/**
 * Created by beyond on 2018/1/4.
 */

public class SplashFragment1 extends android.support.v4.app.Fragment {

    private String fragmentText;
    private int fragmentBackgroundColor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup=(ViewGroup)inflater.inflate(R.layout.splash_fragment_1,container,false);
        fragmentText=getArguments().getString("fragmentText");
        fragmentBackgroundColor=getArguments().getInt("fragmentBackgroundColor");
        initViews(viewGroup);
        return viewGroup;
    }
    private void initViews(ViewGroup viewGroup){
        TextView textView=viewGroup.findViewById(R.id.splash_fragment_text);
        textView.setText(fragmentText);

        View view=viewGroup.findViewById(R.id.splash_fragment_constraintLayout);
        view.setBackgroundColor(fragmentBackgroundColor);
    }
}
