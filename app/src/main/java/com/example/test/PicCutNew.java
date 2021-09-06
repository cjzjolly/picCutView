package com.example.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class PicCutNew extends View {
    private int mWidth, mHeight;
    private Bitmap mBitmap;
    private boolean mIsBmpChanged = false;
    private Rect mCutRect = null;

    public PicCutNew(Context context) {
        super(context);
    }

    public PicCutNew(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PicCutNew(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        notifyBmpChange();
    }

    public void notifyBmpChange() {
        mIsBmpChanged = true;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            mHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsBmpChanged) {
            float wRatio = 0.9f;  //裁剪框宽度为控件宽度的9成
            int cutW = (int) (mWidth * wRatio); //控件实际宽度
            int cutH = (int) (mWidth * wRatio * 4 / 3); //默认H:W = 4:3
            if (mBitmap != null && !mBitmap.isRecycled()) {
                float bmpRatio = (float) mBitmap.getWidth() / mBitmap.getHeight();
                if (bmpRatio > 1.3f) { //如果图片比较长，使用正方形选择框
                    if (cutW >= cutH) {
                        cutW = cutH;
                    } else {
                        cutH = cutW;
                    }
                }
            }
            mCutRect = new Rect((mWidth - cutW) / 2, (mHeight - cutH) / 2, (mWidth - cutW) / 2 + cutW, (mHeight - cutH) / 2 + cutH);
            mIsBmpChanged = false;
        }
        if (mCutRect != null) {
            if (mBitmap != null && !mBitmap.isRecycled()) {
                Paint p = new Paint();
                p.setAlpha(128); //先使用50%透明度绘制
                canvas.drawBitmap(mBitmap, 0, 0, p);
                p.setAlpha(255);
                canvas.save(); //保存区域裁剪之前的状态
                canvas.clipRect(mCutRect); //只选择框内进行不透明渲染
                canvas.drawBitmap(mBitmap, 0, 0, p); //此时仅在裁剪框区域重新绘制不透明的位图
                canvas.restore(); //还原绘制区域
            }
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.WHITE);
            canvas.drawRect(mCutRect, p); //绘制裁剪框主体
            p.setStrokeWidth(9);
            p.setStrokeCap(Paint.Cap.SQUARE);
            float cornerLength = mCutRect.width() > mCutRect.height() ? mCutRect.width() * 0.05f : mCutRect.height() * 0.05f;
            //绘制4个边角
            //左上角
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.top, mCutRect.left + cornerLength, mCutRect.top, p);
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.top, mCutRect.left, mCutRect.top + cornerLength, p);
            //左下角
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.bottom, mCutRect.left + cornerLength, mCutRect.bottom, p);
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.bottom, mCutRect.left, mCutRect.bottom - cornerLength, p);
            //右上角
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.top, mCutRect.right - cornerLength, mCutRect.top, p);
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.top, mCutRect.right, mCutRect.top + cornerLength, p);
            //右下角
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.bottom, mCutRect.right - cornerLength, mCutRect.bottom, p);
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.bottom, mCutRect.right, mCutRect.bottom - cornerLength, p);
        }
    }
}
