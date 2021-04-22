package com.example.switcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Switcher extends View {
    private boolean mInited = false;
    /**
     * 标签条目
     **/
    private List<String> mTabs = null;

    private int mWidth;
    private int mHeight;
    /**
     * 条目宽度
     **/
    private int mItemWidth;
    private List<Item> mItemList;
    private boolean mDebug = false;
    /**
     * 文字大小
     **/
    private float mTextSize;

    private int mDefaultBgColor = 0x00000000;
    private int mDefaultTextColor = 0x55FFFFFF;
    private int mClickedBgColor = 0x00000000;
    private int mClickedTextColor = 0xFFFFFFFF;
    /**
     * 点击起始位置
     **/
    private PointF mStartPoint = null;
    private PointF mEndPoint = null;

    public void selectTab(int position) {
        if (mItemList == null) {
            return;
        }
        for(int i = 0;  i < mItemList.size(); i++) {
            mItemList.get(i).mIsChecked = position == i ? true : false;
        }
        invalidate();
    }

    private class Item {
        public String mTitle;
        public Rect mRect;
        public boolean mIsChecked;
        public int mClickedTextColor;
        public int mDefaultTextColor;
        public int mClickedBgColor;
        public int mDefaultBgColor;
    }

    //选中的item回调
    public interface SelectListener {
        void select(int position);
    }

    private SelectListener mSelectListener = null;

    public Switcher(Context context) {
        super(context);
        init();
    }

    public Switcher(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Switcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!mInited) {
            this.mTextSize = convertDpToPixel(14, getContext());
            mInited = true;
        }
    }

    public void setTabs(List<String> mTabs) {
        this.mTabs = mTabs;
        resetView();
    }

    public void setSelectListener(SelectListener selectListener) {
        this.mSelectListener = selectListener;
    }

    public void setTextSize(int px) {
        this.mTextSize = px;
        resetView();
    }

    public void setTextSizeOnDP(int dp) {
        this.mTextSize = convertDpToPixel(dp, getContext());
        resetView();
    }

    /**
     * 颜色设置
     **/
    public void setColor(int defaultBgColor, int clickedBgColor, int defaultTextColor, int clickedTextColor) {
        this.mDefaultBgColor = defaultBgColor;
        this.mClickedBgColor = clickedBgColor;
        this.mDefaultTextColor = defaultTextColor;
        this.mClickedTextColor = clickedTextColor;
    }

    private void resetView() {
        if (mWidth > 0 && mHeight > 0 && mTabs != null && mTabs.size() > 0) {
            this.mItemList = new ArrayList<>(mTabs.size());
            this.mItemWidth = mWidth / mTabs.size();
            for (int i = 0; i < mTabs.size(); i++) {
                Item item = new Item();
                item.mTitle = mTabs.get(i);
                item.mClickedBgColor = mClickedBgColor;
                item.mDefaultBgColor = mDefaultBgColor;
                item.mClickedTextColor = mClickedTextColor;
                item.mDefaultTextColor = mDefaultTextColor;
                item.mRect = new Rect(mItemWidth * i, 0, mItemWidth * (i + 1), mHeight);
                mItemList.add(item);
            }
            mItemList.get(0).mIsChecked = true; //默认选中第一项
            invalidate();
        }
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

    private void onClick(PointF start, PointF end) {
        for (int i = 0; i < mItemList.size(); i++) {
            Item item = mItemList.get(i);
            if (item.mRect.contains((int) start.x, (int) start.y, (int) end.x, (int) end.y)) {
                item.mIsChecked = true;
                if (mSelectListener != null) {
                    mSelectListener.select(i);
                }
            } else {
                item.mIsChecked = false;
            }
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMySize(widthMeasureSpec);
        int h = getMySize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            this.mWidth = getMySize(widthMeasureSpec);
            this.mHeight = getMySize(heightMeasureSpec);
            resetView();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartPoint = new PointF(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mEndPoint = new PointF(event.getX(), event.getY());
                if (mStartPoint != null && mEndPoint != null) {
                    onClick(mStartPoint, mEndPoint);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWidth > 0 && mHeight > 0 && mItemList != null && mItemList.size() > 0) {
            //文字渲染paint
            Paint textPaint = new Paint();
            textPaint.setTextSize(mTextSize);
            textPaint.setColor(mDefaultTextColor);
            textPaint.setAntiAlias(true);
            textPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            Paint bgPaint = new Paint();
            bgPaint.setStyle(Paint.Style.FILL);

            for (int i = 0; i < mItemList.size(); i++) {
                Item item = mItemList.get(i);
                Rect rect = item.mRect;
                if (mDebug) {
                    Paint p = new Paint();
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(convertDpToPixel(2f, getContext()));
                    p.setStrokeCap(Paint.Cap.ROUND);
                    p.setColor(Color.WHITE);
                    p.setStrokeCap(Paint.Cap.SQUARE);
                    p.setAntiAlias(true);
                    canvas.drawRect(rect, p);
                }
                if (item.mTitle != null) {
                    int textColor = item.mIsChecked ? item.mClickedTextColor : item.mDefaultTextColor;
                    int bgColor = item.mIsChecked ? item.mClickedBgColor : item.mDefaultBgColor;
                    //渲染背景
                    bgPaint.setColor(bgColor);
                    canvas.drawRect(item.mRect, bgPaint);
                    //渲染文字:
                    //测量文字长度
                    textPaint.setColor(textColor);
                    String title = item.mTitle;
                    int titleWidth = (int) (textPaint.measureText(title) + 0.5f);
                    int titleHeight = (int) mTextSize;
                    int threePointsWidth = (int) (textPaint.measureText("...") + 0.5f);
                    boolean isOverWidth = false;
                    //超长度裁剪:
                    while (titleWidth > rect.width() * 0.9f) {
                        title = title.substring(0, title.length() - 1);
                        titleWidth = (int) (textPaint.measureText(title) + threePointsWidth + 0.5f); //预计加了省略号之后的长度
                        isOverWidth = true;
                    }
                    if (isOverWidth) {
                        title += "...";
                    }
                    //居中输出
                    canvas.drawText(title, rect.left + (rect.width() - titleWidth) / 2, rect.top + (rect.height() - titleHeight) / 2, textPaint);
                    if (item.mIsChecked) { //仅选中的条目有下划线
                        //绘制选中线条
                        //在rect中居中显示
                        Paint selectPaint = new Paint();
                        selectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        selectPaint.setColor(0xFF2196F3);
                        selectPaint.setStrokeWidth(convertDpToPixel(2, getContext()));
                        int lineW = (int) (rect.width() * 0.085f);
                        int padding = (rect.width() - lineW) / 2;
                        canvas.drawLine(rect.left + padding, mHeight * 0.886f, rect.left + padding + lineW, mHeight * 0.886f, selectPaint);
                    }
                }
            }
        }
    }
}
