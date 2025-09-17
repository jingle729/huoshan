package com.effectsar.labcv.algorithm.model;


import com.effectsar.labcv.common.model.ButtonItem;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;

import java.util.List;

public class AlgorithmItem extends ButtonItem {

    private List<AlgorithmTaskKey> dependency;
    private int dependencyToastId;

    private AlgorithmTaskKey key;

    public AlgorithmItem() {}

    public AlgorithmItem(AlgorithmTaskKey key) {
        this.key = key;
    }

    public AlgorithmItem(AlgorithmTaskKey key, List<AlgorithmTaskKey> dependency) {
        this.dependency = dependency;
        this.key = key;
    }

    public AlgorithmTaskKey getKey() {
        return key;
    }

    public void setKey(AlgorithmTaskKey key) {
        this.key = key;
    }

    public List<AlgorithmTaskKey> getDependency() {
        return dependency;
    }

    public void setDependency(List<AlgorithmTaskKey> dependency) {
        this.dependency = dependency;
    }

    public int getDependencyToastId() {
        return dependencyToastId;
    }

    public AlgorithmItem setDependencyToastId(int dependencyToastId) {
        this.dependencyToastId = dependencyToastId;
        return this;
    }
}
