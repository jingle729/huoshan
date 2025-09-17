package com.effectsar.labcv.algorithm.ui;

import android.view.View;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.common.view.PropertyTextView;
import com.effectsar.labcv.core.algorithm.ConcentrateAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;


public class ConcentrationUI extends BaseAlgorithmUI<ConcentrateAlgorithmTask.BefConcentrationInfo> {

    private PropertyTextView ptv;

    @Override
    void initView() {
        super.initView();

        addLayout(R.layout.layout_c1_info, R.id.fl_algorithm_info);

        if (!checkAvailable(provider())) return;
        ptv = provider().findViewById(R.id.ptv_c1);
    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);

        ptv.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onReceiveResult(ConcentrateAlgorithmTask.BefConcentrationInfo algorithmResult) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (!checkAvailable(provider()) || algorithmResult == null) return;
                float proportion = algorithmResult.total > 0 ? algorithmResult.concentration * 1.f / algorithmResult.total : 0;
                ptv.setTitle(provider().getString(R.string.tab_concentration) + " :");
                ptv.setValue((int) (proportion * 100) + "%");
            }
        });
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(ConcentrateAlgorithmTask.CONCENTRATION);
        item.setIcon(R.drawable.ic_concentration);
        item.setTitle(R.string.tab_concentration);
        return item;
    }
}
