package com.effectsar.labcv.algorithm.model;

import java.util.List;

public class AlgorithmItemGroup extends AlgorithmItem {
    private List<AlgorithmItem> items;
    private boolean scrollable;

    public AlgorithmItemGroup(List<AlgorithmItem> items) {
        this.items = items;
    }

    public AlgorithmItemGroup(List<AlgorithmItem> items, boolean scrollable) {
        this.items = items;
        this.scrollable = scrollable;
    }

    public List<AlgorithmItem> getItems() {
        return items;
    }

    public void setItems(List<AlgorithmItem> items) {
        this.items = items;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }
}
