package com.bgs.chat.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatLayout extends RelativeLayout {



    public ChatLayout(Context context) {
        super(context);
    }

    public ChatLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ChatLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final float adjustVal = (float) 12.667;
        if(getChildCount()<3)
            return;

        View v1 = getChildAt(0); //message tv
        View v2 = getChildAt(1); //image v or timetv //untuk replay tidak ada image
        View v3 = getChildAt(2); //time tv //untuk send

        int messageHeight = v1.getMeasuredHeight() + v3.getMeasuredHeight();
        int messageWidth = v1.getMeasuredWidth();
        int imageViewWidth = v2.getMeasuredWidth();
        int timeWidth = v3.getMeasuredWidth();

        //int layoutWidth = (int) (imageViewWidth + timeWidth + messageWidth + convertDpToPixel(adjustVal, getContext()));
        int infoWidth = imageViewWidth + timeWidth;
        int chatMessageWidth =  messageWidth > infoWidth ? messageWidth : infoWidth;
        int layoutWidth = (int) (chatMessageWidth + convertDpToPixel(adjustVal, getContext()));

        setMeasuredDimension(layoutWidth, messageHeight);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

}
