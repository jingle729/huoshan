package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.HeadSegAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefHeadSegInfo;

public class HeadSegUI extends BaseAlgorithmUI<BefHeadSegInfo> {
    @Override
    public void onReceiveResult(BefHeadSegInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(HeadSegAlgorithmTask.HEAD_SEGMENT);
        item.setIcon(R.drawable.ic_head_seg);
        item.setTitle(R.string.head_segment_title);
        item.setDesc(R.string.head_segment_desc);
        return item;
    }
}
