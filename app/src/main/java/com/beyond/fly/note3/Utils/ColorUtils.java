package com.beyond.fly.note3.Utils;

import com.beyond.fly.note3.R;

/**
 * Created by beyond on 2018/1/1.
 */

public class ColorUtils {
    public static int getMyColor(long remindDate){
        if (remindDate==0){
            return R.color.colorWhite;
        }else {
            long currentDate = System.currentTimeMillis();
            long period = remindDate - currentDate;
            if (period <= 0) {
                return R.color.colorWhite;
            }else if ( period <= 10 * 60 * 1000) {
                return R.color.color_10min;
            } else if (period <= 15 * 60 * 1000) {
                return R.color.color_15min;
            } else if (period <= 30 * 60 * 1000) {
                return R.color.color_30min;
            } else if (period > 30 * 60 * 1000) {
                return R.color.color_60min;
            } else {
                return R.color.colorWhite;
            }
        }
    }
}
