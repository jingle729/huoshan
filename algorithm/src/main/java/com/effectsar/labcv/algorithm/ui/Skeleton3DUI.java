package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.Skeleton3DAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefSkeleton3DInfo;

public class Skeleton3DUI extends BaseAlgorithmUI<BefSkeleton3DInfo>
{
    @Override
    public void onReceiveResult(BefSkeleton3DInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(Skeleton3DAlgorithmTask.SKELETON3D);
        item.setIcon(R.drawable.ic_skeleton3d);
        item.setTitle(R.string.feature_skeleton_3d);
        item.setDesc(R.string.skeleton3d_detect_desc);
        return  item;
    }
}