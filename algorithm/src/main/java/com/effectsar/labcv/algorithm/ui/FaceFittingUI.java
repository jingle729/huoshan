package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.FaceFittingAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.FaceFitting;

public class FaceFittingUI extends BaseAlgorithmUI<FaceFitting.FaceFittingResult> {
    @Override
    public void onReceiveResult(FaceFitting.FaceFittingResult algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(FaceFittingAlgorithmTask.FACE_FITTING);
        item.setIcon(R.drawable.ic_feature_facefitting);
        item.setTitle(R.string.feature_facefitting);
        return item;
    }
}
