package com.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.picchoice.PicChoiceActivity;
import com.example.piccut.PicCutActivity;
import com.example.piccut.R;

public class Start extends Activity {
    private Button mBtnGotoPicChoice;
    private Button mBtnGotoPicCut;
    private View.OnClickListener mOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_goto_picChoice:
                    startActivity(new Intent(Start.this, PicChoiceActivity.class));
                    break;
                case R.id.btn_goto_picCut:
                    startActivity(new Intent(Start.this, PicCutActivity.class));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtnGotoPicChoice = findViewById(R.id.btn_goto_picChoice);
        mBtnGotoPicCut = findViewById(R.id.btn_goto_picCut);
        mBtnGotoPicChoice.setOnClickListener(mOnclickListener);
        mBtnGotoPicCut.setOnClickListener(mOnclickListener);
    }
}
