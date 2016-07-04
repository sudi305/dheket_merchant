package com.bgs.chat.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;

/**
 * Created by zhufre on 6/18/2016.
 */
public class RoundedBackgroundSpan extends ReplacementSpan
{
    private int mPadding = 10;
    private int mBackgroundColor;
    private int mTextColor;
    private float mTextSize;

    public RoundedBackgroundSpan(int backgroundColor, int textColor) {
        this(backgroundColor, textColor, 0);
    }

    public RoundedBackgroundSpan(int backgroundColor, int textColor, float textSize) {
        this(0, backgroundColor, textColor, 0);
    }

    public RoundedBackgroundSpan(int padding, int backgroundColor, int textColor, float textSize) {
        super();
        mBackgroundColor = backgroundColor;
        mTextColor = textColor;
        mTextSize = textSize;
        if ( padding > 0)
            mPadding = padding;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) (mPadding + paint.measureText(text.subSequence(start, end).toString()) + mPadding);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {
        float width = paint.measureText(text.subSequence(start, end).toString());
        RectF rect = new RectF(x, top+mPadding, x + width + mPadding, bottom);
        paint.setColor(mBackgroundColor);
        canvas.drawRoundRect(rect, mPadding, mPadding, paint);
        paint.setColor(mTextColor);

        if ( mTextSize > 0 ) paint.setTextSize(mTextSize);

        canvas.drawText(text, start, end, x+mPadding, y, paint);
    }
}
