package com.example.piccut;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PicCutView extends View {
    private PointF mPrevCurrentCenter = null;
    private float mPrevDistance = Float.MIN_VALUE;
    private float mTotalScale = 1f;
    /**
     * resetView初始化图片时的大小，不变
     **/
    private float mInitScale = 1f;
    /**
     * 触摸点点距队列
     **/
    private Queue<Float> mTouchDistanceQueue = new LinkedBlockingQueue<>();
    private Bitmap mBmp;
    private Matrix mMatrix;
    private float mAvergeX = 0, mAvergeY = 0;
    private int mPrevPointCount = 0;

    private float mdX = 0f, mdY = 0f;
    private int mWidth, mHeight;
    private Rect mCutRect = null;
    /**
     * 默认缩放最小比例为初始化时的一半
     **/
    private float mScaleMin = 0.5f;
    /**
     * 默认缩放最大比例为初始化时的2倍
     **/
    private float mScaleMax = 2.0f;

    private void init() {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
    }

    public PicCutView(Context context) {
        super(context);
        init();
    }

    public PicCutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PicCutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPic(Bitmap bmp) {
        mBmp = bmp;
        resetView();
    }

    /**
     * 设置最小能缩放到初始化比例的多少比例
     *
     * @param scaleMin 最小能缩放到初始化比例的多少
     **/
    public void setScaleMin(float scaleMin) {
        this.mScaleMin = scaleMin;
    }

    /**
     * 设置最大能缩放到初始化比例的多少比例
     *
     * @param scaleMax 最大能缩放到初始化比例的多少
     **/
    public void setScaleMax(float scaleMax) {
        this.mScaleMax = scaleMax;
    }

    /**
     * 每加载一张图片都要重初始化缩放等所有参数，因为每张图片的长宽参数都不一样
     **/
    private void resetView() {
        if (mBmp != null && !mBmp.isRecycled() && mWidth > 0 && mHeight > 0) {
            int bmpH = mBmp.getHeight();
            int bmpW = mBmp.getWidth();
            //复原参数
            mMatrix = new Matrix();
            mTotalScale = 1f;
            mdX = mdY = 0;
            //图片中间部分放在控件上
            float scale;
            //图片缩放到view的宽高可以容纳的成都
            if (bmpW >= bmpH) {
                scale = (float) mWidth / bmpW;
                mdY = mHeight / 2 - bmpH * scale / 2; //移动到view的中间位置
            } else {
                scale = (float) mHeight / bmpH;
                mdX = mWidth / 2 - bmpW * scale / 2; //移动到view的中间位置
            }
            //先以左上角0，0点缩放到目标比例
            mMatrix.postScale(scale, scale, 0, 0);
            //再放到中线位置
            mMatrix.postTranslate(mdX, mdY); //等效于mMatrix.postScale(scale, scale, mHeight / 2, mWidth / 2)（以中线为缩放中心缩放）
            mTotalScale = scale;
            mInitScale = scale;
            invalidate();
        }
    }

    /**
     * 缩放函数
     *
     * @param scale 本次缩放量
     * @param px    缩放中心x坐标
     * @param py    缩放中心y坐标
     **/
    public void scale(float scale, float px, float py) {
        float relatedTotalScale = mTotalScale / mInitScale; //因为resetView时为了让图片刚好可以被屏幕包裹，本身就做过缩放，所以mTotalScale不为1，但为了方便接下来缩放比例换算，把初始化后的比例看作1。
        if (scale < 1f && relatedTotalScale * scale < mScaleMin) { //如果正在试图缩小，但计算出缩小后的比例值会小于最小限制比例，则取消缩放
            return;
        }
        if (scale >= 1f && relatedTotalScale * scale > mScaleMax) {
            return;
        }
        if (mMatrix != null) {
            mMatrix.postScale(scale, scale, px, py);
            mTotalScale *= scale;
            invalidate();
        }
        Log.i("缩放", String.format("百分比：%f", mTotalScale));
    }

    /**
     * 移动函数
     *
     * @param distanceX 本次移动距离x分量
     * @param distanceY 本次移动距离y分量
     **/
    private void translate(float distanceX, float distanceY) {
        if (mMatrix != null) {
            mMatrix.postTranslate(distanceX, distanceY);
            mdX += distanceX;
            mdY += distanceY;
            invalidate();
        }
        Log.i("移动", String.format("x位移：%f， y位移：%f", distanceX, distanceY));
    }

    /**
     * 裁剪图片
     **/
    public Bitmap cutPic() {
        if (mBmp != null && !mBmp.isRecycled() && mCutRect != null) {
            int width = mCutRect.width();
            int height = mCutRect.height();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Matrix tempMatrix = new Matrix(mMatrix);
            tempMatrix.postTranslate(-mCutRect.left, -mCutRect.top);  //例如我要截取图片可见部分的右半部分，等同于我选择框不动，图片向左移动相应距离。所以left这个对左边的距离等同于图片要左移的距离
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(mBmp, tempMatrix, null);
            return bitmap;
        }
        return null;
    }

    /**
     * 设置图片裁剪范围
     **/
    public void setCutPicRect(Rect rect) {
        this.mCutRect = new Rect(rect);
    }

    /* 获取测量大小*/
    private int getMySize(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        result = specSize;//确切大小,所以将得到的尺寸给view
        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevDistance = 0;
                mPrevPointCount = event.getPointerCount();
                //算出移动中心坐标、点间距离
                for (int i = 0; i < event.getPointerCount(); i++) {
                    mAvergeX += event.getX(i);
                    mAvergeY += event.getY(i);
                    if (i + 1 < event.getPointerCount()) {
                        mPrevDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                mAvergeX /= event.getPointerCount();
                mAvergeY /= event.getPointerCount();
                if (mPrevCurrentCenter == null) {
                    mPrevCurrentCenter = new PointF(mAvergeX, mAvergeY);
                } else {
                    mPrevCurrentCenter.set(mAvergeX, mAvergeY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mAvergeX = 0;
                mAvergeY = 0;
                float nowDistance = 0;
                //算出移动中心坐标、点间距离
                for (int i = 0; i < event.getPointerCount(); i++) {
                    mAvergeX += event.getX(i);
                    mAvergeY += event.getY(i);
                    if (i + 1 < event.getPointerCount()) {
                        nowDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                //现在的点间距离 除以 上次点间距离 这次得到缩放比例
                mAvergeX /= event.getPointerCount();
                mAvergeY /= event.getPointerCount();
                if ((mPrevPointCount != event.getPointerCount()) || event.getPointerCount() <= 1 || mPrevPointCount <= 1) { //触摸点数突然改变 或者 触摸点不超过2，不允许缩放
                    mPrevDistance = nowDistance = 0;
                }
                //如果缩放数据有效，则进行平均平滑化并且进行缩放
                if (mPrevDistance > 0 && nowDistance > 0) {
                    mTouchDistanceQueue.add(nowDistance / mPrevDistance);
                    if (mTouchDistanceQueue.size() >= 6) {
                        Float point[] = new Float[mTouchDistanceQueue.size()];
                        mTouchDistanceQueue.toArray(point);
                        float avergDistance = 0;
                        for (int i = 0; i < point.length; i++) {
                            avergDistance += point[i];
                        }
                        avergDistance /= point.length;
                        scale((float) Math.sqrt(avergDistance), mAvergeX, mAvergeY);
                        while (mTouchDistanceQueue.size() > 6) {
                            mTouchDistanceQueue.poll();
                        }
                    }
                }
                mPrevPointCount = event.getPointerCount();
                mPrevDistance = nowDistance;
                //当前坐标 - 上次坐标 = 偏移值，然后进行位置偏移
                if (mPrevCurrentCenter == null) {
                    mPrevCurrentCenter = new PointF(mAvergeX, mAvergeY);
                } else {
                    translate(mAvergeX - mPrevCurrentCenter.x, mAvergeY - mPrevCurrentCenter.y);
                    mPrevCurrentCenter.set(mAvergeX, mAvergeY);
                }
                break;
            case MotionEvent.ACTION_UP:
                //抬起，清理干净数据
                mAvergeX = 0;
                mAvergeY = 0;
                mTouchDistanceQueue.clear();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBmp != null && !mBmp.isRecycled()) {
            canvas.drawBitmap(mBmp, mMatrix, null);
        }
        if (mCutRect != null) {
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.RED);
            canvas.drawRect(mCutRect, p);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mWidth = getMySize(widthMeasureSpec);
        this.mHeight = getMySize(heightMeasureSpec);
        resetView();
    }
}