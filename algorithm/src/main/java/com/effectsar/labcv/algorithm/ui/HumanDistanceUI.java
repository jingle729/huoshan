package com.effectsar.labcv.algorithm.ui;

import android.content.Context;
import android.content.res.Configuration;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.view.HumanDistanceTip;
import com.effectsar.labcv.algorithm.view.ResultTip;
import com.effectsar.labcv.algorithm.view.TipManager;
import com.effectsar.labcv.core.algorithm.HumanDistanceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefDistanceInfo;

public class HumanDistanceUI extends BaseAlgorithmUI<BefDistanceInfo> {

    @Override
    void initView() {
        super.initView();
        if (!checkAvailable(tipManager())) return;
        tipManager().registerGenerator(HumanDistanceAlgorithmTask.HUMAN_DISTANCE,
                new TipManager.ResultTipGenerator<BefDistanceInfo.BefDistance>() {
                    @Override
                    public ResultTip<BefDistanceInfo.BefDistance> create(Context context) {
                        return new HumanDistanceTip(context);
                    }
                });
    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);

        if (!checkAvailable(tipManager())) return;
        tipManager().enableOrRemove(key, flag);
    }

    @Override
    public void onReceiveResult(BefDistanceInfo distanceInfo) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (!checkAvailable(provider(),
                        provider().getTipManager(), provider().getContext())) return;
                if (distanceInfo == null)return;
                if (provider().getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    tipManager().updateInfo(HumanDistanceAlgorithmTask.HUMAN_DISTANCE, distanceInfo.getBefDistance());
                } else {
                    tipManager().updateInfo(HumanDistanceAlgorithmTask.HUMAN_DISTANCE, distanceInfo.getBefDistance());
                }
            }
        });
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem humanDistance = new AlgorithmItem(HumanDistanceAlgorithmTask.HUMAN_DISTANCE);
        humanDistance.setIcon(R.drawable.ic_distance);
        humanDistance.setTitle(R.string.setting_human_dist);
        humanDistance.setDesc(R.string.setting_human_dist_desc);
        return humanDistance;
    }
}
