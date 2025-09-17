package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.model.AlgorithmItemGroup;
import com.effectsar.labcv.core.algorithm.SaliencyMattingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.SaliencyMatting;

import java.util.Arrays;
import java.util.List;

public class SaliencyMattingUI extends BaseAlgorithmUI<SaliencyMatting.MattingMask> {
    @Override
    public void onReceiveResult(SaliencyMatting.MattingMask algorithmResult) {

    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);
    }

    @Override
    public AlgorithmItem getAlgorithmItem(){
        AlgorithmItem saliency = new AlgorithmItem(SaliencyMattingAlgorithmTask.SALIENCY_MATTING);
        saliency.setIcon(R.drawable.ic_prtrait_matting);
        saliency.setKey(SaliencyMattingAlgorithmTask.SALIENCY_MATTING);
        saliency.setTitle(R.string.feature_saliency_matting);
        return saliency;
    }

}
