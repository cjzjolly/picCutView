package com.example.picchoice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;

public class FragementSysCut extends Fragment {
    private View mRootView;
    private PicChoicer mPicChoicer;
    private ImageView mIvContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.pic_choicer, container, false);
        mPicChoicer = mRootView.findViewById(R.id.pic_choice_view);
        mIvContent = mRootView.findViewById(R.id.iv_content);
        List<Bitmap> imgList = new ArrayList<Bitmap>();
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.successicon);
        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                imgList.add(bmp);
            } else {
                imgList.add(bmp2);
            }
        }
        mPicChoicer.setImageList(imgList);
        mPicChoicer.setSelectListener(new PicChoicer.SelectListener() {
            @Override
            public void select(int position) {
                Log.i("cjztest", "shit:" + position);
                mIvContent.setImageBitmap(imgList.get(position));
            }
        });
        return mRootView;
    }
}
