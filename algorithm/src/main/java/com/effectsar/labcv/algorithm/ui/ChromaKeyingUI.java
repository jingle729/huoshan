package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.model.AlgorithmItemGroup;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.algorithm.ChromaKeyingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefChromaKeyingInfo;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created on 5/13/21 3:12 PM
 */
public class ChromaKeyingUI extends BaseAlgorithmUI<BefChromaKeyingInfo> {
    @Override
    public void init(AlgorithmInfoProvider provider) {
        super.init(provider);
    }

    @Override
    public void onReceiveResult(BefChromaKeyingInfo algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(ChromaKeyingAlgorithmTask.CHROMA_KEYING);
        item.setIcon(R.drawable.ic_chroma_keying);
        item.setTitle(R.string.chroma_keying_title);
        item.setDesc(R.string.chroma_keying_desc);

        AlgorithmItem itemSoft = new AlgorithmItem(ChromaKeyingAlgorithmTask.CHROMA_KEYING_SOFT,
                Collections.singletonList(ChromaKeyingAlgorithmTask.CHROMA_KEYING))
                .setDependencyToastId(R.string.open_chromakeying_first);
        itemSoft.setIcon(R.drawable.ic_chroma_keying);
        itemSoft.setTitle(R.string.feature_chroma_keying_soft);
        itemSoft.setDesc(0);

        AlgorithmItemGroup chromaKeyingGroup = new AlgorithmItemGroup(
                Arrays.asList(item, itemSoft), true
        );
        chromaKeyingGroup.setKey(ChromaKeyingAlgorithmTask.CHROMA_KEYING);
        chromaKeyingGroup.setTitle(R.string.chroma_keying_title);
        return chromaKeyingGroup;
    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);
        if (key.getKey().equals(ChromaKeyingAlgorithmTask.CHROMA_KEYING_SOFT.getKey())) {
            if (flag) {
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(provider().getString(R.string.feature_chroma_keying_soft));

                    }
                });
            }

        }

    }
}
