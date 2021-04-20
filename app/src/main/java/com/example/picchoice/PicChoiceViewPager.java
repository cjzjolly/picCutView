package com.example.picchoice;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class PicChoiceViewPager extends ViewPager {
    public PicChoiceViewPager(@NonNull Context context) {
        super(context);
    }

    public PicChoiceViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {//禁止viewPager滑动事件，全部事件给子控件，防止子控件触摸事件卡住
        return false;
    }
}
