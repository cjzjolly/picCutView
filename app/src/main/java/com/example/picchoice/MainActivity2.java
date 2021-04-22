package com.example.picchoice;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.piccut.R;
import com.example.switcher.Switcher;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private ViewPager mViewPager;
    private Switcher mTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mViewPager = findViewById(R.id.vp_choicer_picker);
        mTab = findViewById(R.id.tab_for_choicer);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FragementSysCut());
        fragments.add(new FragementGalleryChoicer());
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);

        List<String> list = new ArrayList<>();
        list.add("系统截取");
        list.add("相册选取");
        mTab.setTabs(list);
        mTab.setTextSizeOnDP(14);
        mTab.setSelectListener((item) -> {
            mViewPager.setCurrentItem(item);
        });
    }
}