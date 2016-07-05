package com.bgs.dheket.general;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import com.bgs.common.DisplayUtils;
import com.bgs.dheket.App;

/**
 * Created by SND on 6/13/2016.
 */

public class Utility {

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        } else {
            return null;
        }
    }

    // convert from byte array to bitmap
    public static Bitmap getPhoto(byte[] image) {
        if (image!=null) return BitmapFactory.decodeByteArray(image, 0, image.length);
        else return null;
    }

    static NumberFormat formatter;

    public Utility(){

    }

    public static String changeFormatNumber(double originNumber){
        formatter = new DecimalFormat("#0.0");
        String doubleToString = String.valueOf(originNumber);
        String numberMod = "", setNumber = "", replace = "";
        double stringToDouble;
        String[] splitDouble = doubleToString.split("\\.");
        if (splitDouble.length!=0){
            numberMod = splitDouble[splitDouble.length-1];
        } else {
            setNumber = String.valueOf(originNumber);
        }
        if (numberMod.length()>3){
            formatter = new DecimalFormat("#0.000");
        } else if (numberMod.length()==2){
            formatter = new DecimalFormat("#0.00");
        } else if (numberMod.length()==1){
            if (numberMod.equals("0")){
                setNumber = String.valueOf(splitDouble[0]);
                return setNumber;
            } else {
                formatter = new DecimalFormat("#0.0");
            }
        }
        setNumber = String.valueOf(formatter.format(originNumber));
        replace = setNumber.replace(",", ".");
        stringToDouble = Double.parseDouble(replace);

        return String.valueOf(stringToDouble);
    }


    public static String getDeviceUniqueID(ContentResolver contentResolver){
        String device_unique_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            App.applicationHandler.post(runnable);
        } else {
            App.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static String andjustDistanceUnit(Double distance) {
        if (distance < 1){
            double formatDistance = distance*1000;
            return changeFormatNumber((int)formatDistance) + " M";
        } else {
            return changeFormatNumber(distance) + " Km";
        }
    }


    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        OutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = sourceFile.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
        return true;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            //FileLog.e("tmessages", e);
            return false;
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
        return true;
    }

}
