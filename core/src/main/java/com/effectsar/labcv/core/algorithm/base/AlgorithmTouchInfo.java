package com.effectsar.labcv.core.algorithm.base;

/**
 * Author: gaojin.ivy
 * Time: 2025/6/30 21:35
 */

public class AlgorithmTouchInfo {

    public AlgorithmTouchInfo(float x, float y, int action) {
        this.x = x;
        this.y = y;
        this.action = action;
    }

    private final float x;
    private final float y;
    private final int action;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAction() {
        return action;
    }
}
