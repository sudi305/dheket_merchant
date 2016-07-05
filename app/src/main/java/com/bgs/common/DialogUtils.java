package com.bgs.common;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.bgs.dheket.merchant.R;


/**
 * Created by zhufre on 6/23/2016.
 */
public class DialogUtils {
    public static Dialog LoadingSpinner(Context context){
        Dialog pd = new Dialog(context, android.R.style.Theme_Black);
        View view = LayoutInflater.from(context).inflate(R.layout.spiner_dialog, null);
        pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pd.getWindow().setBackgroundDrawableResource(R.color.black_overlay);
        pd.setContentView(view);
        return pd;
    }
}
