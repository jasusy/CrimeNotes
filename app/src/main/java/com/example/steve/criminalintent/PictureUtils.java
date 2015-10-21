package com.example.steve.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

/**
 * Created by Steve on 10/17/2015.
 */
public class PictureUtils {

    public static Bitmap getScaleBitmap(String path, int destWith, int destHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //get bitmap from local image with path and options
        BitmapFactory.decodeFile(path, options);

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        //figure out how much to scale down by, the rate
        int inSampleSize = 1;
        if (srcHeight > destHeight && srcWidth > destWith){
            if(srcHeight < srcWidth){
                inSampleSize = srcWidth / destWith;
            } else {
                inSampleSize = srcHeight / destHeight;
            }
        }

        //read image with new rate
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path, options);

    }

    public static Bitmap getScaleBitmap(String path, Activity activity){
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaleBitmap(path, size.x, size.y);
    }
}
