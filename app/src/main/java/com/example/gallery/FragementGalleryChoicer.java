package com.example.gallery;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.piccut.R;

import java.util.List;

public class FragementGalleryChoicer extends Fragment implements OnRecyclerViewPreloadMoreListener, OnPhotoSelectChangedListener<LocalMedia> {
    private View mRootView;
    private RecyclerPreloadView mRecyclerView;
    protected PictureImageGridAdapter mAdapter;
    private boolean mIsPageStrategy = true;
    /**
     * if there more
     */
    protected boolean isHasMore = true;
    /**
     * page
     */
    protected int mPage = 1;

    private int mBucketId = -1;
    protected int oldCurrentListSize;
    protected PictureSelectionConfig config;

    /**每一行多少个**/
    private int mSpanCount = 3;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.choice_gallery, container, false);
        config = PictureSelectionConfig.getInstance();
        config.initDefaultValue();
        mRecyclerView = mRootView.findViewById(R.id.rv_content);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(mSpanCount, ScreenUtils.dip2px(getContext(), 2), false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), mSpanCount));
        if (!mIsPageStrategy) {
            mRecyclerView.setHasFixedSize(true);
        } else {
            mRecyclerView.setReachBottomRow(RecyclerPreloadView.BOTTOM_PRELOAD);
            mRecyclerView.setOnRecyclerViewPreloadListener(this);
        }
        RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
        if (itemAnimator != null) {
            ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(false);
            mRecyclerView.setItemAnimator(null);
        }
        loadAllMediaData();
        mAdapter = new PictureImageGridAdapter(getContext(), config);
        mAdapter.setOnPhotoSelectChangedListener(this);
        mRecyclerView.setAdapter(mAdapter);

        return mRootView;
    }

    /**图片被点击**/
    @Override
    public void onPictureClick(LocalMedia media, int position) {
        Log.i("cjztest", "FragementGalleryChoicer.onPictureClick:" + position);
    }

    @Override
    public void onRecyclerViewPreloadMore() {
        loadMoreData();
    }

    /**
     * isSame
     *
     * @param newMedia
     * @return
     */
    private boolean isLocalMediaSame(LocalMedia newMedia) {
        LocalMedia oldMedia = mAdapter.getItem(0);
        if (oldMedia == null || newMedia == null) {
            return false;
        }
        if (oldMedia.getPath().equals(newMedia.getPath())) {
            return true;
        }
        // if Content:// type,determines whether the suffix id is consistent, mainly to solve the following two types of problems
        // content://media/external/images/media/5844
        // content://media/external/file/5844
        if (PictureMimeType.isContent(newMedia.getPath())
                && PictureMimeType.isContent(oldMedia.getPath())) {
            if (!TextUtils.isEmpty(newMedia.getPath()) && !TextUtils.isEmpty(oldMedia.getPath())) {
                String newId = newMedia.getPath().substring(newMedia.getPath().lastIndexOf("/") + 1);
                String oldId = oldMedia.getPath().substring(oldMedia.getPath().lastIndexOf("/") + 1);
                if (newId.equals(oldId)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Page Model
     *
     * @param folders
     */
    private void initPageModel(List<LocalMediaFolder> folders) {
        if (folders != null) {
//            folderWindow.bindFolder(folders);
            mPage = 1;
//            LocalMediaFolder folder = folderWindow.getFolder(0);
//            mTvPictureTitle.setTag(R.id.view_count_tag, folder != null ? folder.getImageNum() : 0);
//            mTvPictureTitle.setTag(R.id.view_index_tag, 0);
//            long bucketId = folder != null ? folder.getBucketId() : -1;
            long bucketId = mBucketId;
            mRecyclerView.setEnabledLoadMore(true);
            LocalMediaPageLoader.getInstance(getContext()).loadPageMediaData(bucketId, mPage,
                    (OnQueryDataResultListener<LocalMedia>) (data, currentPage, isHasMore) -> {
                        for (LocalMedia m : data) {
                            Log.i("cjztest", "FragementGalleryChoicer.initPageModel数据:" + m.getPath());
                        }
                        if (!isDetached()) {
                            if (mAdapter != null) {
                                this.isHasMore = true;
                                // IsHasMore being true means that there's still data, but data being 0 might be a filter that's turned on and that doesn't happen to fit on the whole page
                                if (isHasMore && data.size() == 0) {
                                    onRecyclerViewPreloadMore();
                                    return;
                                }
                                int currentSize = mAdapter.getSize();
                                int resultSize = data.size();
                                oldCurrentListSize = oldCurrentListSize + currentSize;
                                if (resultSize >= currentSize) {
                                    // This situation is mainly caused by the use of camera memory, the Activity is recycled
                                    if (currentSize > 0 && currentSize < resultSize && oldCurrentListSize != resultSize) {
                                        if (isLocalMediaSame(data.get(0))) {
                                            mAdapter.bindData(data);
                                        } else {
                                            mAdapter.getData().addAll(data);
                                        }
                                    } else {
                                        mAdapter.bindData(data);
                                    }
                                }

                            }
                        }
                    });
        }
    }

    /**
     * get LocalMedia s
     */
    protected void readLocalMedia() {
        if (/*config.isPageStrategy*/mIsPageStrategy) {
            LocalMediaPageLoader.getInstance(getContext()).loadAllMedia(
                    (OnQueryDataResultListener<LocalMediaFolder>) (data, currentPage, isHasMore) -> {
                        for (LocalMediaFolder m : data) {
                            Log.i("cjztest", "FragementGalleryChoicer.initPageModel数据:" + m.getName());
                        }
                        if (!isDetached()) {
                            this.isHasMore = true;
                            initPageModel(data);
//                            synchronousCover();
                        }
                    });
        }
//        else {
//            PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<List<LocalMediaFolder>>() {
//
//                @Override
//                public List<LocalMediaFolder> doInBackground() {
//                    return new LocalMediaLoader(getContext()).loadAllMedia();
//                }
//
//                @Override
//                public void onSuccess(List<LocalMediaFolder> folders) {
//                    initStandardModel(folders);
//                }
//            });
//        }
    }

    /**
     * load All Data
     */
    private void loadAllMediaData() {
        if (PermissionChecker
                .checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) &&
                PermissionChecker
                        .checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            readLocalMedia();
        } else {
            PermissionChecker.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
        }
    }

    /**
     * load more data
     */
    private void loadMoreData() { //读取数据
        if (mAdapter != null) {
            if (isHasMore) {
                mPage++;
//                long bucketId = ValueOf.toLong(mTvPictureTitle.getTag(R.id.view_tag));
                long bucketId = mBucketId;
                LocalMediaPageLoader.getInstance(getContext()).loadPageMediaData(bucketId, mPage, 60,
                        (OnQueryDataResultListener<LocalMedia>) (result, currentPage, isHasMore) -> {
                            if (!isDetached()) {
                                this.isHasMore = isHasMore;
                                if (isHasMore) {
                                    int size = result.size();
                                    if (size > 0) {
                                        int positionStart = mAdapter.getSize();
                                        mAdapter.getData().addAll(result);
                                        int itemCount = mAdapter.getItemCount();
                                        mAdapter.notifyItemRangeChanged(positionStart, itemCount);
                                    } else {
                                        onRecyclerViewPreloadMore();
                                    }
                                    if (size < PictureConfig.MIN_PAGE_SIZE) {
                                        mRecyclerView.onScrolled(mRecyclerView.getScrollX(), mRecyclerView.getScrollY());
                                    }
                                }
                            }
                        });
            }
        }
    }

}
