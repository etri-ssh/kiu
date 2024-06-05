package com.example.ccp.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

public class Convert {
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    public static int pxToDp(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) px / density);
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Common.log("rotateImage angle : " + angle);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
            matrix, true);
    }
}