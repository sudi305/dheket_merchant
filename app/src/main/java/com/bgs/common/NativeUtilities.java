package com.bgs.common;

import android.graphics.Bitmap;

/**
 * Created by madhur on 3/1/15.
 */
public class NativeUtilities {

    public native static void loadBitmap(String path, Bitmap bitmap, int scale, int width, int height, int stride);


}
