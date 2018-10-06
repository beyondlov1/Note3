package com.beyond.fly.note3.Utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by beyond on 2017/12/25.
 */

public class ImageUtils {
    private int calculateInSampleSize(BitmapFactory.Options options,int requestWidth,int requestHeight){
        int width=options.outWidth;
        int height=options.outHeight;
        int inSampleSize=1;

        while(width/inSampleSize>requestWidth||height/inSampleSize>requestHeight){
            inSampleSize*=2;
        }
        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromResourse(Resources resources,int resId, int requestWidth,int requestHeight){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeResource(resources,resId,options);
        options.inSampleSize=calculateInSampleSize(options,requestWidth,requestHeight);
        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeResource(resources,resId,options);
    }
}
