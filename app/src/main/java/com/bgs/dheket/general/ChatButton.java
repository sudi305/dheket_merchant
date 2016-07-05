package com.bgs.dheket.general;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.RemoteViews;

/**
 * Created by zhufre on 7/4/2016.
 */
@RemoteViews.RemoteView
public class ChatButton extends ImageButton {

    public ChatButton(Context context) {
        super(context);
    }

    public ChatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        TextPaint textPaint = new TextPaint();
        textPaint.setFakeBoldText(true);
        canvas.drawText("test", 0, 0, textPaint);
    }


}
