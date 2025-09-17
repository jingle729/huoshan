package com.effectsar.labcv.algorithm.ui;

import androidx.fragment.app.Fragment;;

import com.effectsar.labcv.algorithm.fragment.FaceClusterFragment;
import com.effectsar.labcv.core.algorithm.FaceClusterAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;


public class FaceClusterUI extends BaseAlgorithmUI<Object> {

    @Override
    void initView() {
        super.initView();

        addLayout(R.layout.layout_face_cluster_board, R.id.fl_algorithm_info);
        provider().setBoardTarget(R.id.fl_face_cluster_board);
    }

    @Override
    public void onReceiveResult(Object algorithmResult) {

    }

    @Override
    public IFragmentGenerator getFragmentGenerator() {
        return new IFragmentGenerator() {
            @Override
            public Fragment create() {
                return new FaceClusterFragment();
            }

            @Override
            public int title() {
                return R.string.tab_face_cluster;
            }

            @Override
            public AlgorithmTaskKey key() {
                return FaceClusterAlgorithmTask.FACE_CLUSTER;
            }
        };
    }
}
