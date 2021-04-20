package com.example.piccut;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends Activity {

    private PicChoicer mPicChoicer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mPicChoicer = findViewById(R.id.pic_choicer);
        List<Bitmap> imgList = new ArrayList<Bitmap>();
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        for (int i = 0; i < 30; i++) {
            imgList.add(bmp);
        }
        mPicChoicer.setImageList(imgList);
    }
}