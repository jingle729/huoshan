package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.AvaBoostAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefAvaBoostInfo;

public class AvaBoostUI extends BaseAlgorithmUI<BefAvaBoostInfo>
{
    @Override
    public void onReceiveResult(BefAvaBoostInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(AvaBoostAlgorithmTask.AVABOOST);
        item.setIcon(R.drawable.ic_feature_emotion_driven_item);
        item.setTitle(R.string.feature_emotion_driven);
        item.setDesc(R.string.feature_emotion_driven);
        return item;
    }
}
