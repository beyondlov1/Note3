package com.beyond.fly.note3.Utils;

import android.content.Context;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.jar.Attributes;

/**
 * Created by beyond on 2018/1/8.
 */

public class MyBehavior extends CoordinatorLayout.Behavior <View>{
    //滑动时让fab跟随变小，透明度变小

    private static float defaultDependenceyTop;
    public MyBehavior(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency.getTop()<300&&dependency.getTop()>defaultDependenceyTop)
            defaultDependenceyTop=dependency.getTop();
        return dependency instanceof FrameLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float alpha=(float) (dependency.getTop())/defaultDependenceyTop;
        alpha=alpha<1?alpha:1;
        child.setScaleX(alpha);
        child.setScaleY(alpha);
        child.setAlpha(alpha);
        return true;
    }
}
