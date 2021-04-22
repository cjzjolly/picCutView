package com.example.picchoice;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.gallery.FragementGalleryChoicer;
import com.example.gallery.PictureConfig;
import com.example.piccut.R;
import com.example.switcher.Switcher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private ViewPager mViewPager;
    private Switcher mTab;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mViewPager = findViewById(R.id.vp_choicer_picker);
        mTab = findViewById(R.id.tab_for_choicer);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
            } else {
                init();
            }
        }
    }

    private void init() {
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
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTab.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("cjztest", "MainActivity2.request...");
        switch (requestCode) {
            case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
                // Store Permissions
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                }
                break;
        }
    }

    protected void showPermissionsDialog(boolean isCamera, String errorMsg) {
        if (isFinishing()) {
            return;
        }
//        final PictureCustomDialog dialog =
//                new PictureCustomDialog(getContext(), R.layout.picture_wind_base_dialog);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
//        Button btn_commit = dialog.findViewById(R.id.btn_commit);
//        btn_commit.setText(getString(R.string.picture_go_setting));
//        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
//        TextView tv_content = dialog.findViewById(R.id.tv_content);
//        tvTitle.setText(getString(R.string.picture_prompt));
//        tv_content.setText(errorMsg);
//        btn_cancel.setOnClickListener(v -> {
//            if (!isFinishing()) {
//                dialog.dismiss();
//            }
//            if (!isCamera) {
//                if (PictureSelectionConfig.listener != null) {
//                    PictureSelectionConfig.listener.onCancel();
//                }
//                exit();
//            }
//        });
//        btn_commit.setOnClickListener(v -> {
//            if (!isFinishing()) {
//                dialog.dismiss();
//            }
//            PermissionChecker.launchAppDetailsSettings(getContext());
//            isEnterSetting = true;
//        });
//        dialog.show();
        Dialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Dialog)
                .setTitle("tips")
                .setMessage("camera_peemission_tip")
                .setPositiveButton("open", (dialog2, which) -> {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                })
                .setNegativeButton("cancel", (dialog3, which) -> {
                    dialog3.dismiss();
                })
                .create();
        dialog.show();
    }
}