package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class BgMaskProgram extends MaskProgram {
    private float[] mMaskColor;
    private int mMaskColorLocation;

    public BgMaskProgram(Context context, int width, int height, float[] maskColor) {
        super(context, width, height, FRAGMENT_BG_MASK);

        mMaskColorLocation = GLES20.glGetUniformLocation(mProgram, "maskColor");
        mMaskColor = maskColor;
    }

    public void setMaskColor(float[] mMaskColor) {
        this.mMaskColor = mMaskColor;
    }

    @Override
    protected void onBindData() {
        GLES20.glUniform4f(mMaskColorLocation, mMaskColor[0], mMaskColor[1], mMaskColor[2], mMaskColor[3]);
    }
}
