package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.PortraitMattingAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.PortraitMatting;

public class PortraitMattingUI extends BaseAlgorithmUI<PortraitMatting.MattingMask> {
    @Override
    public void onReceiveResult(PortraitMatting.MattingMask algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(PortraitMattingAlgorithmTask.PORTRAIT_MATTING);
        item.setIcon(R.drawable.ic_prtrait_matting);
        item.setTitle(R.string.portait_matting_title);
        item.setDesc(R.string.portait_matting_desc);
        return item;
    }
}
