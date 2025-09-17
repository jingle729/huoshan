package com.effectsar.labcv.algorithm.ui;

import android.content.Context;
import android.content.res.Configuration;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.view.PetFaceInfoTip;
import com.effectsar.labcv.algorithm.view.ResultTip;
import com.effectsar.labcv.algorithm.view.TipManager;
import com.effectsar.labcv.core.algorithm.PetFaceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefPetFaceInfo;

public class PetFaceUI extends BaseAlgorithmUI<BefPetFaceInfo> {

    @Override
    void initView() {
        if (!checkAvailable(tipManager())) return;
        tipManager().registerGenerator(PetFaceAlgorithmTask.PET_FACE,
                new TipManager.ResultTipGenerator<BefPetFaceInfo.PetFace>() {
                    @Override
                    public ResultTip<BefPetFaceInfo.PetFace> create(Context context) {
                        return new PetFaceInfoTip(context);
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
    public void onReceiveResult(BefPetFaceInfo befPetFaceInfo) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (!checkAvailable(provider(), provider().getContext(), tipManager())) return;
                if (befPetFaceInfo == null)return;
                if (provider().getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    tipManager().updateInfo(PetFaceAlgorithmTask.PET_FACE, befPetFaceInfo.getFace90());
                } else {
                    tipManager().updateInfo(PetFaceAlgorithmTask.PET_FACE, befPetFaceInfo.getFace90());
                }
            }
        });
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        AlgorithmItem petFace = new AlgorithmItem(PetFaceAlgorithmTask.PET_FACE);
        petFace.setIcon(R.drawable.ic_pet);
        petFace.setTitle(R.string.pet_face_title);
        petFace.setDesc(R.string.pet_face_desc);
        return petFace;
    }
}
