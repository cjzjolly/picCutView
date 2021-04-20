package com.example.piccut;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    /**
     * 白框是否跟随选中条目，false为固定于view中线位置
     **/
    private boolean mIsCenterRectFollwed = false;
    /**
     * 按长边居中图片
     **/
    private boolean mAlignCenterByLongEdge = false;

    private class Item { //条目
        public Bitmap bitmap;
        public Rect rect;
    }

    //中线选中图片的回调
    public interface SelectListener {
        void select(int position);
    }

    private SelectListener mSelectListener = null;

    private List<Item> mItemList;
    private int mWidth;
    private int mHeight;
    private ValueAnimator mTransFinishAnim;
    private boolean isInited = false;
    private int mCount = 5; //一行几个item
    private int mUnitSize;
    private int mMargin; //item之间的距离
    private PointF mPrevCurrentCenter = null;
    private float mPrevDistance = Float.MIN_VALUE;
    /**
     * 触摸点点距队列
     **/
    private Queue<Float> mTouchDistanceQueue = new LinkedBlockingQueue<>();
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
            isInited = true;
        }
    }

    public void setImageList(List<Bitmap> list) {
        this.mBmpList = list;
        resetView();
    }

    /**
     * 移动函数
     *
     * @param distanceX 本次移动距离x分量
     **/
    private void translate(float distanceX) {
        if (mWidth > 0 && mHeight > 0) {
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
        mTransFinishAnim.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mSelectListener != null) {
                    mSelectListener.select(mMostCloseItemPos);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mTransFinishAnim.start();
    }

    private void resetView() {
        if (mWidth > 0 && mHeight > 0 && mBmpList != null) {
            this.mUnitSize = mWidth > mHeight ? mHeight / mCount : mWidth / mCount;
            this.mMargin = mUnitSize / 10;
            this.mItemList = new ArrayList<>(mBmpList.size());
            int totalSize = mUnitSize + mMargin;
            for (int i = 0; i < mBmpList.size(); i++) {
                Item item = new Item();
                item.bitmap = mBmpList.get(i);
                item.rect = new Rect(mWidth / 2 - mUnitSize / 2 + totalSize * i, mHeight / 2 - mUnitSize / 2, mWidth / 2 + mUnitSize / 2 + totalSize * i, mHeight / 2 + mUnitSize / 2);
                mItemList.add(item);
            }
        }
    }

    public void setSelectListener(SelectListener mSelectListener) {
        this.mSelectListener = mSelectListener;
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

    public float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWidth > 0 && mHeight > 0 && mItemList != null && mItemList.size() > 0) {
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(convertDpToPixel(2f, getContext()));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.WHITE);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setAntiAlias(true);
            Rect viewRect = new Rect(0, 0, mWidth, mHeight);
            float cornetR = convertDpToPixel(3f, getContext());
            int padding = (int) convertDpToPixel(1f, getContext());
            for (int i = 0; i < mItemList.size(); i++) {
                Item item = mItemList.get(i);
                Rect rect = item.rect;
                //item超出范围就不渲染，节约资源
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
                    if (mAlignCenterByLongEdge) {
                        if (bmpW >= bmpH) { //保持比例缩放，并适配窗口(按最长边缩放)
                            scale = (float) item.rect.width() / bmpW;
                        } else {
                            scale = (float) item.rect.height() / bmpH;
                        }
                        matrix.postScale(scale, scale); //缩放
                        matrix.postTranslate(item.rect.left + (item.rect.width() - bmpW * scale) / 2, item.rect.top + (item.rect.height() - bmpH * scale) / 2); //移动到框内，居中显示
                        canvas.drawBitmap(item.bitmap, matrix, null);
                    } else {
                        if (bmpW <= bmpH) { //保持比例缩放，并适配窗口(按最短边缩放)
                            scale = (float) item.rect.width() / bmpW;
                        } else {
                            scale = (float) item.rect.height() / bmpH;
                        }
                        matrix.postScale(scale, scale); //缩放
                        matrix.postTranslate(item.rect.left + (item.rect.width() - bmpW * scale) / 2, item.rect.top + (item.rect.height() - bmpH * scale) / 2); //移动到框内，居中显示
                        canvas.save();
                        canvas.clipRect(item.rect); //裁剪多余部分
                        canvas.drawBitmap(item.bitmap, matrix, null);
                        canvas.restore();
                    }
                }
                if (mIsCenterRectFollwed) {
                    if (i == mMostCloseItemPos) { //只有中线划过的条目显示白色框框
                        canvas.drawRect(rect, p);
                    }
                } else {
                    RectF centerRect = new RectF(mWidth / 2 - mUnitSize / 2 - padding,
                            mHeight / 2 - mUnitSize / 2 - padding,
                            mWidth / 2 + mUnitSize / 2 + padding,
                            mHeight / 2 + mUnitSize / 2 + padding);
                    canvas.drawRoundRect(centerRect, cornetR, cornetR, p);

                }
                if (mDebug) {
                    Paint debugP = new Paint();
                    debugP.setStyle(Paint.Style.STROKE);
                    debugP.setColor(Color.RED);
                    debugP.setStrokeWidth(convertDpToPixel(2f, getContext()));
                    debugP.setTextSize(rect.width() / 2);
                    canvas.drawText(i + "", rect.left + rect.width() / 2, rect.top + rect.height() / 2, debugP);
                    canvas.drawRect(viewRect, debugP);
                }
            }
        }
    }

}
