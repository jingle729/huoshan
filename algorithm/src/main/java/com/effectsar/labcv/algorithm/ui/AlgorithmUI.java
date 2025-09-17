package com.effectsar.labcv.algorithm.ui;

import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;
import android.view.View;

import androidx.fragment.app.Fragment;;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.view.TipManager;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;

public interface AlgorithmUI<T> {
    void init(AlgorithmInfoProvider provider);

    void onEvent(AlgorithmTaskKey key, boolean flag);

    void onReceiveResult(T algorithmResult);

    AlgorithmItem getAlgorithmItem();

    IFragmentGenerator getFragmentGenerator();

    interface AlgorithmInfoProvider {
        TipManager getTipManager();
        Context getContext();
        String getString(@StringRes int id);
        int getPreviewWidth();
        int getPreviewHeight();
        boolean fitCenter();
        FragmentManager getFMManager();
        void runOnUiThread(Runnable runnable);
        <T extends View> T findViewById(@IdRes int id);
        void setBoardTarget(@IdRes int targetId);
    }

    public interface IFragmentGenerator {
        Fragment create();
        int title();
        AlgorithmTaskKey key();
    }
}
