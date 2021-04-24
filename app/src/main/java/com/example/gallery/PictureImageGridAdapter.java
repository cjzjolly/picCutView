package com.example.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.piccut.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author：luck
 * @date：2016-12-30 12:02
 * @describe：PictureImageGridAdapter
 */
public class PictureImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnPhotoSelectChangedListener imageSelectChangedListener;
    private List<LocalMedia> data = new ArrayList<>();
    private List<LocalMedia> selectData = new ArrayList<>();
    private PictureSelectionConfig config;
    private int mPrevCheckedPos = 0;
    /**item位置和holder之间的关系记录，可以方便地实现单选**/
    private Map<Integer, ViewHolder> mPositionToViewHolderMap = new HashMap<>();

    public PictureImageGridAdapter(Context context, PictureSelectionConfig config) {
        this.context = context;
        this.config = config;
    }

    /**
     * 全量刷新
     *
     * @param data
     */
    public void bindData(List<LocalMedia> data) {
        this.data = data == null ? new ArrayList<>() : data;
        this.notifyDataSetChanged();
    }

    public List<LocalMedia> getData() {
        return data == null ? new ArrayList<>() : data;
    }

    public boolean isDataEmpty() {
        return data == null || data.size() == 0;
    }

    public void clear() {
        if (getSize() > 0) {
            data.clear();
        }
    }

    public int getSize() {
        return data == null ? 0 : data.size();
    }

    public LocalMedia getItem(int position) {
        return getSize() > 0 ? data.get(position) : null;
    }

    @Override
    public int getItemViewType(int position) {
        return PictureConfig.TYPE_PICTURE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choice_gallery_picture_image_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder contentHolder = (ViewHolder) holder;
        mPositionToViewHolderMap.put(position, contentHolder);  //该position的item已经新建
        final LocalMedia image = data.get(position);
        final String path = image.getPath();
        contentHolder.tvCheck.setVisibility(View.VISIBLE);
        contentHolder.btnCheck.setVisibility(View.VISIBLE);
        contentHolder.position = position;
        if (data.get(position).isChecked()) {
            contentHolder.tvCheck.setSelected(true);
        } else {
            contentHolder.tvCheck.setSelected(false);
        }
        if (PictureMimeType.isHasImage(image.getMimeType())) {
            if (image.loadLongImageStatus == PictureConfig.NORMAL) {
                image.isLongImage = MediaUtils.isLongImg(image);
                image.loadLongImageStatus = PictureConfig.LOADED;
            }
        } else {
            image.loadLongImageStatus = PictureConfig.NORMAL;
        }
        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine.loadGridImage(context, path, contentHolder.ivPicture);
        }

        contentHolder.btnCheck.setOnClickListener(v -> {
            //去除上次选中的对勾
            // If the original path does not exist or the path does exist but the file does not exist
            String newPath = image.getRealPath();
            if (!TextUtils.isEmpty(newPath) && !new File(newPath).exists()) {
                //ToastUtils.s(context, PictureMimeType.s(context, mimeType));
                return;
            }
            // The width and height of the image are reversed if there is rotation information
            MediaUtils.setOrientationAsynchronous(context, image, config.isAndroidQChangeWH, config.isAndroidQChangeVideoWH, null);
            //还没选中的弄成选中的状态
            changeCheckboxState(contentHolder, image.isChecked());
            contentHolder.tvCheck.setSelected(true);
            image.setChecked(true);
            if (imageSelectChangedListener != null) {
                imageSelectChangedListener.onPictureClick(image, position);
            }
            //设置为上次选中的条目
            if (mPrevCheckedPos != position) { //之前选中的弄成没选中的状态
                LocalMedia prevImage = data.get(mPrevCheckedPos);
                ViewHolder prevCheckedItem = mPositionToViewHolderMap.get(mPrevCheckedPos);
                changeCheckboxState(prevCheckedItem, prevImage.isChecked());
                prevCheckedItem.tvCheck.setSelected(false);  //这个对象和新建的对象已经不一样了
                prevCheckedItem.tvCheck.setSelected(false);
                prevImage.setChecked(false);
            }
            mPrevCheckedPos = position;
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPicture;
        TextView tvCheck;
        View contentView;
        View btnCheck;
        int position;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            ivPicture = itemView.findViewById(R.id.ivPicture);
            tvCheck = itemView.findViewById(R.id.tvCheck);
            btnCheck = itemView.findViewById(R.id.btnCheck);
            position = 0;
        }
    }

    /**
     * Update the selected status of the image
     *
     * @param contentHolder
     */

    @SuppressLint("StringFormatMatches")
    private void changeCheckboxState(ViewHolder contentHolder, boolean isChecked) { //动画一下
        if (isChecked) { //如果之前是选中的条目，执行这个函数时，会从这里消灭它
            AnimUtils.disZoom(contentHolder.ivPicture, config.zoomAnim);
        } else {
            // The radio
//            if (config.selectionMode == PictureConfig.SINGLE) {
//                singleRadioMediaImage();
//            }
            // If the width and height are 0, regain the width and height
            LocalMedia image = data.get(contentHolder.position);
            if (image.getWidth() == 0 || image.getHeight() == 0) {
                image.setOrientation(-1);
                if (PictureMimeType.isContent(image.getPath())) {
                    if (PictureMimeType.isHasVideo(image.getMimeType())) {
                        MediaUtils.getVideoSizeForUri(context, Uri.parse(image.getPath()), image);
                    } else if (PictureMimeType.isHasImage(image.getMimeType())) {
                        int[] size = MediaUtils.getImageSizeForUri(context, Uri.parse(image.getPath()));
                        image.setWidth(size[0]);
                        image.setHeight(size[1]);
                    }
                } else {
                    if (PictureMimeType.isHasVideo(image.getMimeType())) {
                        int[] size = MediaUtils.getVideoSizeForUrl(image.getPath());
                        image.setWidth(size[0]);
                        image.setHeight(size[1]);
                    } else if (PictureMimeType.isHasImage(image.getMimeType())) {
                        int[] size = MediaUtils.getImageSizeForUrl(image.getPath());
                        image.setWidth(size[0]);
                        image.setHeight(size[1]);
                    }
                }
            }
            AnimUtils.zoom(contentHolder.ivPicture, config.zoomAnim);
            contentHolder.tvCheck.startAnimation(AnimationUtils.loadAnimation(context, R.anim.picture_anim_modal_in));
        }
    }

    /**
     * Select the image and animate it
     *
     * @param holder
     * @param isChecked
     */
    public void selectImage(ViewHolder holder, boolean isChecked) {
        holder.tvCheck.setSelected(isChecked);
    }

    /**
     * Binding listener
     *
     * @param imageSelectChangedListener
     */
    public void setOnPhotoSelectChangedListener(OnPhotoSelectChangedListener
                                                        imageSelectChangedListener) {
        this.imageSelectChangedListener = imageSelectChangedListener;
    }
}

