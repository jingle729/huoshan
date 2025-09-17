package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.BachSkeletonAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkeletonAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefBachSkeletonInfo;
import com.effectsar.labcv.effectsdk.BefSkeletonInfo;

public class BachSkeletonUI extends BaseAlgorithmUI<BefBachSkeletonInfo> {

    @Override
    public void onReceiveResult(BefBachSkeletonInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem skeleton = new AlgorithmItem(BachSkeletonAlgorithmTask.BACH_SKELETON);
        skeleton.setIcon(R.drawable.ic_bach_skeleton);
        skeleton.setTitle(R.string.bach_skeleton_title);
        skeleton.setDesc(R.string.bach_skeleton_desc);
        return skeleton;
    }
}
