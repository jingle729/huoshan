package com.effectsar.labcv.algorithm.ui;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import android.view.LayoutInflater;


import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.view.TipManager;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.core.util.LogUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public abstract class BaseAlgorithmUI<T> implements AlgorithmUI<T> {
    private WeakReference<AlgorithmInfoProvider> mProvider;
    private boolean mNeedInit = true;

    @Override
    public void init(AlgorithmInfoProvider provider) {
        mProvider = new WeakReference<>(provider);

        if (mNeedInit) {
            mNeedInit = false;
            initView();
        }
    }

    void initView() {}

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {}

    @Override
    public AlgorithmItem getAlgorithmItem() {
        return null;
    }

    @Override
    public IFragmentGenerator getFragmentGenerator() {
        return null;
    }

    protected AlgorithmInfoProvider provider() {
        return mProvider.get();
    }

    protected void addLayout(@LayoutRes int layoutId, @IdRes int parentId) {
        LayoutInflater.from(provider().getContext())
                .inflate(layoutId, provider().findViewById(parentId), true);
    }

    protected TipManager tipManager() {
        if (!checkAvailable(provider())) return null;
        return provider().getTipManager();
    }

    protected void runOnUIThread(Runnable runnable) {
        if (!checkAvailable(provider())) return;
        provider().runOnUiThread(runnable);
    }

    protected boolean checkAvailable(Object... args) {
        for (Object o : args) {
            if (o == null) {
                LogUtils.e("unavailable with args " + Arrays.toString(args));
                return false;
            }
        }
        return true;
    }
}
