package com.bgs.dheket.general;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

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

    NumberFormat formatter;
    String doubleToString;
    double originNumber;

    public Utility(){

    }

    public String changeFormatNumber(double originNumber){
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
}
