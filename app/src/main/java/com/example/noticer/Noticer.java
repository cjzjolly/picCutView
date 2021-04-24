package com.example.noticer;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public class Noticer extends View { //todo 找不到素材，暂时用个view快速顶着
    private int mWidth;
    private int mHeight;
    private int mMsgCount = 0;

    public Noticer(Context context) {
        super(context);
    }

    public Noticer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Noticer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMySize(widthMeasureSpec);
        int h = getMySize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            this.mWidth = getMySize(widthMeasureSpec);
            this.mHeight = getMySize(heightMeasureSpec);
//            resetView();
            invalidate();
        }
    }

    public void setMsgCount(int msgCount) {
        this.mMsgCount = msgCount;
        invalidate();
    }

    /* 获取测量大小*/
    private int getMySize(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        result = specSize;//确切大小,所以将得到的尺寸给view
        return result;
    }

    private float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    protected void onDraw(Canvas canvas) {
        int size = (int) convertDpToPixel(3, getContext());
        if (mWidth > 0 && mHeight > 0) {
            Path path = new Path();
            path.moveTo(0, mHeight * 0.2f);
            path.lineTo(0, mHeight * 0.8f);
            path.lineTo(mWidth * 0.2f, mHeight * 0.8f);
            path.lineTo(mWidth * 0.7f, mHeight);
            path.lineTo(mWidth * 0.7f, 0);
            path.lineTo(mWidth * 0.2f, mHeight * 0.2f);
            path.lineTo(0, mHeight * 0.2f);
            canvas.save();
            canvas.scale(0.7f, 0.7f, mWidth / 2, mHeight / 2);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(size);
            paint.setColor(0xFF000000);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
            canvas.restore();
            if (mMsgCount > 0) {
                paint.setColor(0xFFFF0000);
                paint.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(mWidth - mWidth / 5,  mWidth / 5, mWidth / 5, paint);

                Paint textPaint = new Paint();
                textPaint.setColor(0xFFFFFFFF);
                textPaint.setTextSize(mWidth / 2);
                textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                float textSize = textPaint.measureText(mMsgCount + "");

                canvas.drawRoundRect(mWidth - textSize * 1.2f,  0, mWidth,   textPaint.measureText("1") * 1.5f, 100, 100, paint);

                textPaint.setTextSize(textPaint.getTextSize() * 0.8f);
                canvas.drawText(mMsgCount + "", mWidth - textSize, textPaint.measureText("1") * 1.5f, textPaint);

            }
        }
    }
}
