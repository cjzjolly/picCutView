package com.example.piccut;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private PicCutView mPicCutView;
    private Button mBtnCut;
    private ImageView mIvCut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPicCutView = findViewById(R.id.pic_cut_view);
        mBtnCut = findViewById(R.id.btn_cut);
        mIvCut = findViewById(R.id.iv_cut);

        mBtnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvCut.setImageBitmap(mPicCutView.cutPic());
            }
        });

        mPicCutView.setPic(BitmapFactory.decodeResource(getResources(), R.drawable.test), new Rect(100, 500, 1080 - 100, 2110 - 500));
    }
}