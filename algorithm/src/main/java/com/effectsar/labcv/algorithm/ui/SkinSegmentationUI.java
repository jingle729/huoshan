package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.HeadSegAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkinSegmentationAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefHeadSegInfo;
import com.effectsar.labcv.effectsdk.BefSkinSegInfo;

public class SkinSegmentationUI extends BaseAlgorithmUI<BefSkinSegInfo> {
    @Override
    public void onReceiveResult(BefSkinSegInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(SkinSegmentationAlgorithmTask.SKIN_SEGMENTATION);
        item.setIcon(R.drawable.ic_skin_segmentation);
        item.setTitle(R.string.skin_segmentation_title);
        item.setDesc(R.string.skin_segmentation_desc);
        return item;
    }
}
