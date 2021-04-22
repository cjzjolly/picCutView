package com.example.switcher;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity3 extends AppCompatActivity {

    private ViewPager mViewPager;
    private Switcher mSw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        mSw = findViewById(R.id.sw);
        List<String> list = new ArrayList<>();
        list.add("系统截取");
        list.add("相册选取");
        mSw.setTabs(list);
        mSw.setSelectListener((item) -> {
            Log.i("cjztest_item", "item:" + item);
        });
    }
}