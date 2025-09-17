package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.common.view.PropertyTextView;
import com.effectsar.labcv.core.algorithm.LicenseCakeAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefLicenseCakeInfo;

public class LicenseCakeUI extends BaseAlgorithmUI<BefLicenseCakeInfo> {
    private static final String TAG = "LicenseCakeUI";
   
    private int mFrameNum = 0;

    private BefLicenseCakeInfo.LicenseCakeInfo mBefLicenseCakeInfo;

    private PropertyTextView ptv;

    @Override
    void initView() {
        super.initView();
    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);

    }

    @Override
    public void onReceiveResult(BefLicenseCakeInfo befDynamicGestureInfo) {
//        Log.d(TAG, "onReceiveResult: " + befDynamicGestureInfo.toString());
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        return (AlgorithmItem) new AlgorithmItem(LicenseCakeAlgorithmTask.LICENSE_CAKE)
                .setIcon(R.drawable.ic_dynamic_gesture)
                .setTitle(R.string.license_cake);
    }
}
