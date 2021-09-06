package com.example.gallery;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    protected boolean mIsHasMore = true;
    /**
     * page
     */
    protected int mPage = 1;

    private int mBucketId = -1;
    protected int oldCurrentListSize;
    protected PictureSelectionConfig config;

    /**每一行多少个**/
    private int mSpanCount = 3;
    private FolderPopWindow mFolderWindow;
    private ImageView ivArrow;
    /**相册标题**/
    private TextView mTvPictureTitle;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.choice_gallery, container, false);
        config = PictureSelectionConfig.getInstance();
        config.initDefaultValue();
        mRecyclerView = mRootView.findViewById(R.id.rv_content);
        ivArrow = mRootView.findViewById(R.id.iv_arrow);
        mTvPictureTitle = mRootView.findViewById(R.id.tv_title);
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
        mFolderWindow = new FolderPopWindow(getContext());
        mFolderWindow.setOnAlbumItemClickListener(new OnAlbumItemClickListener() { //相册选择
            @Override
            public void onItemClick(int position, boolean isCameraFolder, long bucketId, String folderName, List<LocalMedia> data) {
                if (bucketId == -1) {
                    mTvPictureTitle.setText("所有图片");   //设置标题
                } else {
                    mTvPictureTitle.setText(folderName);   //设置标题
                    Log.i("cjztest", "bucketid:" + bucketId);
                }
                long currentBucketId = ValueOf.toLong(mTvPictureTitle.getTag()); //了解当前的数据篮子标记
                //给不同的标题设置不同的数据篮子标记
                mTvPictureTitle.setTag(mFolderWindow.getFolder(position) != null
                        ? mFolderWindow.getFolder(position).getImageNum() : 0);
                if (config.isPageStrategy) {
                    if (currentBucketId != bucketId) {
                        setLastCacheFolderData();
                        boolean isCurrentCacheFolderData = isCurrentCacheFolderData(position);
                        if (!isCurrentCacheFolderData) {
                            mPage = 1;
//                            showPleaseDialog(); //显示loading
                            LocalMediaPageLoader.getInstance(getContext()).loadPageMediaData(bucketId, mPage,
                                    (OnQueryDataResultListener<LocalMedia>) (result, currentPage, isHasMore) -> {
                                        FragementGalleryChoicer.this.mIsHasMore = isHasMore;
                                        if (!FragementGalleryChoicer.this.getActivity().isFinishing() && !FragementGalleryChoicer.this.isDetached()) {
                                            if (result.size() == 0) {
                                                mAdapter.clear();
                                            }
                                            mAdapter.bindData(result);
                                            mRecyclerView.onScrolled(0, 0);
                                            mRecyclerView.smoothScrollToPosition(0);
//                                            dismissDialog();  //取消loading
                                        }
                                    });
                        }
                    }
                } else {
                    mAdapter.bindData(data);
                    mRecyclerView.smoothScrollToPosition(0);
                }
                mTvPictureTitle.setTag(bucketId);
                mFolderWindow.dismiss();
            }
        });
        ivArrow.setOnClickListener(v->{
            if (mFolderWindow.isShowing()) {
                mFolderWindow.dismiss();
            } else {
                if (!mFolderWindow.isEmpty()) {
                    mFolderWindow.showAsDropDown(ivArrow);
                    if (!config.isSingleDirectReturn) {
                        List<LocalMedia> selectedImages = mAdapter.getSelectedData();
                        mFolderWindow.updateFolderCheckStatus(selectedImages);
                    }
                }
            }
        });
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
     * Before switching directories, set the current directory cache
     */
    private void setLastCacheFolderData() {
        int oldPosition = ValueOf.toInt(mTvPictureTitle.getTag());
        LocalMediaFolder lastFolder = mFolderWindow.getFolder(oldPosition);
        if (lastFolder != null && mAdapter.getData() != null) {
            lastFolder.setData(mAdapter.getData());
            lastFolder.setCurrentDataPage(mPage);
            lastFolder.setHasMore(mIsHasMore);
        }
    }

    /**
     * Does the current album have a cache
     *
     * @param position
     */
    private boolean isCurrentCacheFolderData(int position) {
        mTvPictureTitle.setTag(position);
        LocalMediaFolder currentFolder = mFolderWindow.getFolder(position);
        if (currentFolder != null
                && currentFolder.getData() != null
                && currentFolder.getData().size() > 0) {
            mAdapter.bindData(currentFolder.getData());
            mPage = currentFolder.getCurrentDataPage();
            mIsHasMore = currentFolder.isHasMore();
            mRecyclerView.smoothScrollToPosition(0);

            return true;
        }
        return false;
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
            for (LocalMediaFolder folder : folders) {
                Log.i("cjztest", "FragmentGalleryChoicer.initPageModel__文件夹:" + folder.getName());
            }
            mFolderWindow.bindFolder(folders);
            mPage = 1;
            LocalMediaFolder folder = mFolderWindow.getFolder(0);
//            mTvPictureTitle.setTag(folder != null ? folder.getImageNum() : 0);
            mTvPictureTitle.setTag(0);
            long bucketId = folder != null ? folder.getBucketId() : -1;
//            long bucketId = mBucketId;
            mRecyclerView.setEnabledLoadMore(true);
            LocalMediaPageLoader.getInstance(getContext()).loadPageMediaData(bucketId, mPage,
                    (OnQueryDataResultListener<LocalMedia>) (data, currentPage, isHasMore) -> {
                        for (LocalMedia m : data) {
                            Log.i("cjztest", "FragementGalleryChoicer.initPageModel数据:" + m.getPath());
                        }
                        if (!isDetached()) {
                            if (mAdapter != null) {
                                this.mIsHasMore = true;
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
                            this.mIsHasMore = true;
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
            if (mIsHasMore) {
                mPage++;
                long bucketId = ValueOf.toLong(mTvPictureTitle.getTag());
//                long bucketId = mBucketId;
                LocalMediaPageLoader.getInstance(getContext()).loadPageMediaData(bucketId, mPage, 60,
                        (OnQueryDataResultListener<LocalMedia>) (result, currentPage, isHasMore) -> {
                            if (!isDetached()) {
                                this.mIsHasMore = isHasMore;
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
