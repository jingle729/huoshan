package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.core.algorithm.HairParserAlgorithmTask;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.HairParser;

public class HairParserUI extends BaseAlgorithmUI<HairParser.HairMask> {
    @Override
    public void onReceiveResult(HairParser.HairMask algorithmResult) {

    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem item = new AlgorithmItem(HairParserAlgorithmTask.HAIR_PARSER);
        item.setIcon(R.drawable.ic_hair_parser);
        item.setTitle(R.string.segment_hair_title);
        item.setDesc(R.string.segment_hair_desc);
        return item;
    }
}
