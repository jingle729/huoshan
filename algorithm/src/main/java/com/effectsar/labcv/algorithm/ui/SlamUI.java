package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.model.AlgorithmItemGroup;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.algorithm.SlamAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;

import java.util.Arrays;
import java.util.Collections;

import static com.effectsar.labcv.core.algorithm.SlamAlgorithmTask.SLAM_REGION_TRACKING;

import android.os.Handler;

public class SlamUI extends BaseAlgorithmUI<SlamAlgorithmTask.SlamRenderInfo> {
    @Override
    public void onReceiveResult(SlamAlgorithmTask.SlamRenderInfo algorithmResult) {

    }

    private  boolean isSlamFollow = false;

    @Override
    void initView() {
        super.initView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(provider().getString(com.effectsar.labcv.common.R.string.slam_begin));
            }
        },700);
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem slam = new AlgorithmItem(SlamAlgorithmTask.SLAM);

        slam.setIcon(R.drawable.ic_slam);
        slam.setTitle(R.string.slam_flat_lay);
        slam.setDesc(R.string.slam_version_plane);

        AlgorithmItem slamRegion = new AlgorithmItem(SLAM_REGION_TRACKING,
                Collections.singletonList(SlamAlgorithmTask.SLAM))
                .setDependencyToastId(R.string.slam_open_slam_first);
        slamRegion.setIcon(R.drawable.ic_slam_follow)
                .setTitle(R.string.slam_region)
                .setDesc(R.string.slam_version_region);


        AlgorithmItem slamWorld = new AlgorithmItem(SlamAlgorithmTask.SLAM_WORLD_CORD,
                Collections.singletonList(SlamAlgorithmTask.SLAM))
                .setDependencyToastId(R.string.slam_open_slam_first);
        slamWorld.setIcon(R.drawable.ic_slam_world_cord)
                .setTitle(R.string.slam_world)
                .setDesc(R.string.slam_world_coord);

        AlgorithmItem slamFeature = new AlgorithmItem(SlamAlgorithmTask.SLAM_FEATURE_POINTS,
                Collections.singletonList(SlamAlgorithmTask.SLAM))
                .setDependencyToastId(R.string.slam_open_slam_first);
        slamFeature.setIcon(R.drawable.ic_slam_feature_points)
                .setTitle(R.string.slam_world_feature_point)
                .setDesc(R.string.slam_feature_points);

        AlgorithmItemGroup slamGroup = new AlgorithmItemGroup(
                Arrays.asList(slam, slamRegion, slamWorld, slamFeature), true
        );
        slamGroup.setKey(SlamAlgorithmTask.SLAM);
        slamGroup.setTitle(R.string.slam_tab);

        return slamGroup;
    }


    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);
        if (key.getKey() == SLAM_REGION_TRACKING.getKey()) {
            if (flag == isSlamFollow) {
                return ;
            }
            isSlamFollow = flag;

//            if (flag) {
//                runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtils.show(provider().getString(com.effectsar.labcv.core.R.string.slam_follow_open));
//
//                    }
//                });
//            } else {
//                runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtils.show( provider().getString( com.effectsar.labcv.core.R.string.slam_follow_close));
//                    }
//                });
//            }

        }

    }
}
