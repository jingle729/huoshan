package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.model.AlgorithmItemGroup;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.algorithm.ObjectTrackingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.ObjectTracking;

import java.util.Arrays;
import java.util.Collections;

public class  ObjectTrackingUI extends BaseAlgorithmUI<ObjectTrackingAlgorithmTask.ObjectTrackingRenderInfo>
{
    private  boolean mBoxInited = false;
    @Override
    public void onReceiveResult(ObjectTrackingAlgorithmTask.ObjectTrackingRenderInfo algorithmResult) {
        if (algorithmResult != null) {
            mBoxInited = algorithmResult.boxInited;
        }
    }

    @Override
    public void init(AlgorithmInfoProvider provider) {
        super.init(provider);
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(provider().getString(com.effectsar.labcv.common.R.string.object_tracking_open));
            }
        });
    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);
        if (key.getKey() == ObjectTrackingAlgorithmTask.OBJECT_TRACKING.getKey()) {
            if (flag == true) {
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(provider().getString(com.effectsar.labcv.common.R.string.object_tracking_open));
                    }
                });
            }
        } else if (key.getKey() == ObjectTrackingAlgorithmTask.OBJECT_TRACKING_START.getKey()) {
            if (flag == true && !mBoxInited) {
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(provider().getString(com.effectsar.labcv.common.R.string.object_tracking_no_box));
                    }
                });
            }
        }
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem object_tracking = new AlgorithmItem(ObjectTrackingAlgorithmTask.OBJECT_TRACKING);

        object_tracking.setIcon(R.drawable.ic_slam);
        object_tracking.setTitle(R.string.object_tracking);
        object_tracking.setDesc(R.string.object_tracking);

        AlgorithmItem tracking_start =  new AlgorithmItem(ObjectTrackingAlgorithmTask.OBJECT_TRACKING_START,
                Collections.singletonList(ObjectTrackingAlgorithmTask.OBJECT_TRACKING)).setDependencyToastId(R.string.object_tracking_first);

        tracking_start.setIcon(R.drawable.ic_slam_follow);
        tracking_start.setTitle(R.string.object_tracking_start);
        tracking_start.setDesc(R.string.object_tracking_start);


        AlgorithmItemGroup objectTrackingGroup = new AlgorithmItemGroup(
                Arrays.asList(object_tracking, tracking_start), true
        );
        objectTrackingGroup.setKey(ObjectTrackingAlgorithmTask.OBJECT_TRACKING);
        objectTrackingGroup.setTitle(R.string.object_tracking);

        return objectTrackingGroup;
    }


}