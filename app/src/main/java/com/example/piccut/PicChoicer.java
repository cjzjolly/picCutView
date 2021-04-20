package com.example.piccut;

import android.animation.ValueAnimator;
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

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PicChoicer extends View {
    private List<Bitmap> mBmpList;
    /**
     * 最靠近中线的item条目
     **/
    private int mMostCloseItemPos = 0;
    private boolean mDebug = false;

    private class Item { //条目
        public Bitmap bitmap;
        public Rect rect;
    }

    private List<Item> mItemList;
    private int mWidth;
    private int mHeight;
    private float mDx, mDy;
    private ValueAnimator mTransFinishAnim;
    private boolean isInited = false;
    private int mCount = 5; //一行几个item
    private int mUnitSize;
    private int mMargin; //item之间的距离
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
    private Matrix mMatrix;
    private float mAvergeX = 0, mAvergeY = 0;
    private int mPrevPointCount = 0;


    public PicChoicer(Context context) {
        super(context);
        init();
    }

    public PicChoicer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PicChoicer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInited) {
            this.mMatrix = new Matrix();
            isInited = true;
        }
    }

    public void setImageList(List<Bitmap> list) {
        this.mBmpList = list;
        resetView();
    }

    /**
     * 缩放函数
     *
     * @param scale 本次缩放量
     * @param px    缩放中心x坐标
     * @param py    缩放中心y坐标
     **/
    public void scale(float scale, float px, float py) {

    }

    /**
     * 移动函数
     *
     * @param distanceX 本次移动距离x分量
     **/
    private void translate(float distanceX) {
        if (mWidth > 0 && mHeight > 0) {
            this.mDx += distanceX;
            int mostCloseVal = Integer.MAX_VALUE;
            mMostCloseItemPos = 0;
            for (int i = 0; i < mItemList.size(); i++) {
                Item item = mItemList.get(i);
                item.rect.left += (int) distanceX;
                item.rect.right += (int) distanceX;
                int itemCenterX = item.rect.left + item.rect.width() / 2;
                //找到item的中线距离view中线最短的item
                if (Math.abs(mWidth / 2 - itemCenterX) < Math.abs(mostCloseVal)) {
                    mostCloseVal = mWidth / 2 - itemCenterX;
                    mMostCloseItemPos = i;
                }
            }
            invalidate();
        }
    }

    private void translateFinish() {
        translate(0);
        if (mTransFinishAnim != null) {
            mTransFinishAnim.cancel();
        }
        Item item = mItemList.get(mMostCloseItemPos);
        int itemCenterX = (item.rect.left + item.rect.right) / 2;
        mTransFinishAnim = ValueAnimator.ofInt(itemCenterX, mWidth / 2);
        mTransFinishAnim.setDuration(100);
        mTransFinishAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            Integer prevX = null;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (prevX != null) {
                    int distanceX = (int) animation.getAnimatedValue() - prevX;
                    translate(distanceX);
                }
                prevX = new Integer((int) animation.getAnimatedValue());
            }
        });
        mTransFinishAnim.start();
    }

    private void resetView() {
        if (mWidth > 0 && mHeight > 0 && mBmpList != null) {
            this.mUnitSize = mWidth > mHeight ? mHeight / mCount : mWidth / mCount;
            this.mMargin = mUnitSize / 10;
            this.mItemList = new ArrayList<>(mBmpList.size());
            this.mDx = mWidth / 2;
            int totalSize = mUnitSize + mMargin;
            for (int i = 0; i < mBmpList.size(); i++) {
                Item item = new Item();
                item.bitmap = mBmpList.get(i);
                item.rect = new Rect(mWidth / 2 - mUnitSize / 2 + totalSize * i, mHeight / 2 - mUnitSize / 2, mWidth / 2 + mUnitSize / 2 + totalSize * i, mHeight / 2 + mUnitSize / 2);
                mItemList.add(item);
            }
        }
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
                //检测上次手指之间的距离mPrevDistance和这次的nowDistance之间的长度差，以这次/上次所谓缩放比例。如果缩放数据有效，则进行平均平滑化并且进行缩放
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
                } else if (event.getPointerCount() > 0) {
                    translate(event.getX(0) - mPrevCurrentCenter.x);
                    mPrevCurrentCenter.set(event.getX(0), event.getY(0));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getPointerCount() > 0) {
                    translateFinish();
                }
                //抬起，清理干净数据
                mAvergeX = 0;
                mAvergeY = 0;
                mTouchDistanceQueue.clear();
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mWidth = getMySize(widthMeasureSpec);
        this.mHeight = getMySize(heightMeasureSpec);
        resetView();
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
    protected void onDraw(Canvas canvas) {
        if (mWidth > 0 && mHeight > 0 && mItemList != null && mItemList.size() > 0) {
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3); //todo 要多粗啊
            p.setColor(Color.WHITE);
            p.setStrokeCap(Paint.Cap.SQUARE);
            Rect viewRect = new Rect(0, 0, mWidth, mHeight);
            for (int i = 0; i < mItemList.size(); i++) {
                Item item = mItemList.get(i);
                Rect rect = item.rect;
                //item超出范围就不渲染
                if (!(viewRect.intersects(rect.left, rect.top, rect.right, rect.bottom) || viewRect.contains(rect))) {
                    continue;
                }
                //渲染图片
                if (item.bitmap != null && !item.bitmap.isRecycled()) {
                    Bitmap bmp = item.bitmap;
                    int bmpW = bmp.getWidth();
                    int bmpH = bmp.getHeight();
                    Matrix matrix = new Matrix();
                    float scale = 1f;
                    if (bmpW >= bmpH) { //保持比例缩放，并适配窗口
                        scale = (float) item.rect.width() / bmpW;
                    } else {
                        scale = (float) item.rect.height() / bmpH;
                    }
                    matrix.postScale(scale, scale);
                    matrix.postTranslate(item.rect.left + (item.rect.width() - bmpW * scale) / 2, item.rect.top + (item.rect.height() - bmpH * scale) / 2); //居中显示
                    canvas.drawBitmap(item.bitmap, matrix, null);
                }
                if (i == mMostCloseItemPos) { //只有中线显示白色框框
                    canvas.drawRect(rect, p);
                }
                if (mDebug) {
                    Paint debugP = new Paint();
                    debugP.setStyle(Paint.Style.STROKE);
                    debugP.setColor(Color.RED);
                    debugP.setStrokeWidth(3); //todo 要多粗啊
                    debugP.setTextSize(rect.width() / 2);
                    canvas.drawText(i + "", rect.left + rect.width() / 2, rect.top + rect.height() / 2, debugP);
                    canvas.drawRect(viewRect, debugP);
                }
            }
        }
    }

}
