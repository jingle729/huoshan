package com.effectsar.labcv.algorithm.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem;
import com.effectsar.labcv.algorithm.adapter.FaceClusterAdapter;
import com.effectsar.labcv.algorithm.task.facecluster.FaceClusterMgr;
import com.effectsar.labcv.common.utils.CommonUtils;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.cv.R;
import com.volcengine.ck.LocalAlbumActivityContract;
import com.volcengine.ck.album.AlbumEntrance;
import com.volcengine.ck.album.base.AlbumConfig;
import com.volcengine.ck.album.utils.AlbumExtKt;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.utils.EOUtils;

import java.util.ArrayList;
import java.util.List;

//import androidx.fragment.app.Fragment;;

public class FaceClusterFragment extends Fragment implements FaceClusterMgr.ClusterCallback, FaceClusterAdapter.OnItemClickListener {
    private static final int REQUEST_CODE_CHOOSE = 10;
    private View mIvClear;
    private View mIvAdd;
    private RelativeLayout mRlCluster;
    private RecyclerView mRvFaceList;
    private Button mBtnStart;
    private View mBtnRet;
    private ProgressBar mProgressBar;

    private FaceClusterMgr mFaceClusterMgr;

    private List<List<String>> mClusterResultList;
    private List<String> mChoosePicture;
    private FaceClusterAdapter mAdapter;

    protected final LocalAlbumActivityContract albumActivityContract = new LocalAlbumActivityContract();
    private ActivityResultLauncher<AlbumConfig> albumLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumLauncher = registerForActivityResult(albumActivityContract, result -> {
            if (!result.isEmpty()) {
                processSelectedMedia(result);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.face_cluster_layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRlCluster = view.findViewById(R.id.rl_cluster);
        mBtnStart = view.findViewById(R.id.btn_cluster_start);
        mRvFaceList = view.findViewById(R.id.rv_cluster_list);
        mIvClear = view.findViewById(R.id.ll_cluster_clear);
        mIvAdd = view.findViewById(R.id.ll_cluster_add);
        mBtnRet = view.findViewById(R.id.btn_cluster_ret);
        mProgressBar = view.findViewById(R.id.progress);

        mIvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    LogUtils.e("too fast click");
                    return;
                }

                startChoosePic();
            }
        });

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()){
                    LogUtils.e("too fast click");
                    return;
                }
                startCluster();
            }
        });

        mIvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()){
                    LogUtils.e("too fast click");
                    return;
                }

                cleanData();
            }
        });

        mBtnRet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()){
                    LogUtils.e("too fast click");
                    return;
                }

                mAdapter.resetCluster();
                v.setVisibility(View.GONE);
            }
        });

        LinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvFaceList.setLayoutManager(layoutManager);
        mAdapter = new FaceClusterAdapter(getActivity());
        mRvFaceList.setAdapter(mAdapter);
        mFaceClusterMgr = new FaceClusterMgr(getActivity(), this );
    }

    private void startCluster() {
        LogUtils.d("cluster fragment start cluster");
        mBtnStart.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        mFaceClusterMgr.cluster(mChoosePicture);
    }

    private void cleanData(){
        mFaceClusterMgr.clean();
        mIvAdd.setVisibility(View.VISIBLE);
        mRlCluster.setVisibility(View.GONE);
        mChoosePicture = null;
        mClusterResultList = null;
        mAdapter.clear();
    }

    private void setData(){
        mRlCluster.setVisibility(View.VISIBLE);
        mAdapter.setChooseList(mChoosePicture);
        mIvAdd.setVisibility(View.GONE);
        mRvFaceList.setVisibility(View.VISIBLE);
        mBtnStart.setEnabled(true);
        mBtnStart.setVisibility(View.VISIBLE);

        mProgressBar.setVisibility(View.GONE);
    }

    private void startChoosePic(){
        EOUtils.INSTANCE.getPermission().checkPermissions(requireActivity(), Scene.ALBUM, () -> {
            AlbumConfig config = new AlbumConfig();
            config.setAllEnable(false);
            config.setVideoEnable(false);
            config.setImageEnable(true);
            config.setMaxSelectCount(40);
            config.setShowGif(false);
            albumLauncher.launch(config);
            return null;
        }, strings -> {
            AlbumEntrance.INSTANCE.showAlbumPermissionTips(requireActivity());
            return null;
        });
    }

    private void processSelectedMedia(List<? extends IMaterialItem> select){
        mChoosePicture = new ArrayList<>(select.size());
        for (int i = 0;i < select.size();i++){
            mChoosePicture.add(AlbumExtKt.getAbsolutePath(select.get(i)));
        }
        setData();
    }

    @Override
    public void onClusterCallback(List<List<String>> result, int clusterNums) {
        mClusterResultList = result;
        mAdapter.setClusterResultList(mClusterResultList, clusterNums);
        mAdapter.setOpenCluserListener(this);
        mBtnStart.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClusterProcess(int process) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFaceClusterMgr.release();
    }

    @Override
    public void onOpenCluster() {
        mBtnRet.setVisibility(View.VISIBLE);
    }

    /**
     * fix: java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
     */
    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }
}
