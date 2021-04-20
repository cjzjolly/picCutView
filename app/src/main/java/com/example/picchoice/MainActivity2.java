package com.example.picchoice;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends FragmentActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        List<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(new FragementSysCut());
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        mViewPager = findViewById(R.id.vp_choicer_picker);
        mViewPager.setAdapter(adapter);

    }
}