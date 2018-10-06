package com.beyond.fly.note3.Utils;

import android.app.Fragment;

/**
 * Created by beyond on 17-12-17.
 */

public class MyFragment extends Fragment {
    private boolean myIsVisable;

    @Override
    public void onStart() {
        super.onStart();
        myIsVisable=true;
    }

    @Override
    public void onStop() {
        super.onStop();
        myIsVisable=false;
    }

    public boolean isMyIsVisable(){
        return myIsVisable;
    }
}
