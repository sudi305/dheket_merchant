package com.bgs.chat.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

/**
 * Created by zhufre on 6/18/2016.
 */
public class CircleBackgroundSpan extends ReplacementSpan
{
    private int mPadding = 4;
    private int mBackgroundColor;
    private int mTextColor;
    private int mBorderColor;
    private float mTextSize;
    private int mTop;

    public CircleBackgroundSpan(int backgroundColor, int borderColor, int textColor) {
        this(backgroundColor, borderColor, textColor, 0);
    }

    public CircleBackgroundSpan(int backgroundColor, int borderColor, int textColor, float textSize) {
        this(backgroundColor, borderColor, textColor, 0, 0, 0);
    }

    public CircleBackgroundSpan(int backgroundColor, int borderColor, int textColor, float textSize, int mPadding) {
        this(backgroundColor, borderColor, textColor, 0, 0, mPadding);
    }

    public CircleBackgroundSpan(int backgroundColor, int borderColor, int textColor, float textSize, int top, int padding) {
        super();
        mBackgroundColor = backgroundColor;
        mBorderColor = borderColor;
        mBorderColor = mBorderColor;
        mTextColor = textColor;
        mTextSize = textSize;
        mTop = top;
        if ( padding > 0) mPadding = padding;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) (mPadding + paint.measureText(text.subSequence(start, end).toString()) + mPadding);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {

        TextPaint textPaint = new TextPaint();
        textPaint.setFakeBoldText(true);
        textPaint.setColor(mTextColor);
        if ( mTextSize > 0 ) textPaint.setTextSize(mTextSize);
        float width = textPaint.measureText(text.subSequence(start, end).toString());

        //make even
        width += mPadding;
        width -= (width % 2);
        bottom = (int)width; //bottom==width -> fully circle
        //x -= mPadding;
        x -= (x % 2);

        textPaint.setTextAlign(Paint.Align.CENTER);
        float textHeight = textPaint.descent() - textPaint.ascent();
        float textOffset = (textHeight / 2) - textPaint.descent();

        RectF bounds = new RectF(x, top - mTop, x + width, bottom - mTop);
        paint.setColor(mBackgroundColor);
        canvas.drawOval(bounds, paint);
        if ( mBorderColor != -1 ) {
            paint.setStrokeWidth(2f);
            paint.setColor(mBorderColor);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawOval(bounds, paint);
        }
        canvas.drawText(text, start, end, bounds.centerX(), bounds.centerY() + textOffset, textPaint);
    }
}
