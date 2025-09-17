package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.SkeletonAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefSkeletonInfo;

public class SkeletonUI extends BaseAlgorithmUI<BefSkeletonInfo> {

    @Override
    public void onReceiveResult(BefSkeletonInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem skeleton = new AlgorithmItem(SkeletonAlgorithmTask.SKELETON);
        skeleton.setIcon(R.drawable.ic_skeleton);
        skeleton.setTitle(R.string.skeleton_detect_title);
        skeleton.setDesc(R.string.skeleton_detect_desc);
        return skeleton;
    }
}
