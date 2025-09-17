package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.renderscript.Matrix4f;

import com.effectsar.labcv.core.algorithm.SlamAlgorithmTask;
import com.effectsar.labcv.effectsdk.BefSlamInfo;


public class SlamProgram extends ShaderProgram {

//    private final int mMvpLocation;
    private final float mZnear = 0.1f;
    private final float mZfar = 10000.0f;
    private Matrix4f mPerspectiveMat = null;
    private Matrix4f mViewMat = null;
    private Matrix4f mModelMat = null;


    private SlamWorldProgram mSlamWorldProgram;
    private MeshProgram mSlamObjectProgram;
    private PointProgram mFeaturePointProgram;

    protected SlamProgram(Context context, int width, int height) {
        super(context, null, null, width, height);

        mSlamWorldProgram = new SlamWorldProgram(context, width, height);
        mSlamObjectProgram = new MeshProgram(context, width, height);
        mFeaturePointProgram = new PointProgram(context, width, height);

    }

    public void init(SlamAlgorithmTask.SlamRenderInfo slamRenderInfo) {
        float[] perspective = MatrixUtil.intrinsicToPerspective(slamRenderInfo.slamInfo.intrinsic.fx, slamRenderInfo.slamInfo.intrinsic.fy,
                slamRenderInfo.slamInfo.intrinsic.cx, slamRenderInfo.slamInfo.intrinsic.cy, mWidth, mHeight, mZnear, mZfar);
        mPerspectiveMat = new Matrix4f(perspective);
        mSlamObjectProgram.initModel(slamRenderInfo.objectPath, slamRenderInfo.objectTexturePath);
        mSlamWorldProgram.setTexturePath(slamRenderInfo.planeTexturePath);
    }

    public void updateCameraPose(BefSlamInfo.SlamPose cameraPose) {
        if (mViewMat == null) {
            mViewMat = new Matrix4f();
        }
        float[] viewMat = new float[16];
        viewMat[0] = cameraPose.getR()[0];  viewMat[1] = cameraPose.getR()[1];  viewMat[2] = cameraPose.getR()[2]; viewMat[3] = cameraPose.getT()[0];
        viewMat[4] = cameraPose.getR()[3];  viewMat[5] = cameraPose.getR()[4];  viewMat[6] = cameraPose.getR()[5]; viewMat[7] = cameraPose.getT()[1];
        viewMat[8] = cameraPose.getR()[6];  viewMat[9] = cameraPose.getR()[7];  viewMat[10] = cameraPose.getR()[8]; viewMat[11] = cameraPose.getT()[2];
        viewMat[12] = 0.0f; viewMat[13] = 0.0f; viewMat[14] = 0.0f; viewMat[15] = 1.0f;
        MatrixUtil.matInverse(viewMat);
        mViewMat =  new Matrix4f(viewMat);

    }

    public void updateWorldModel(BefSlamInfo.SlamPose modelPose) {
        float[] viewMat = new float[16];
        viewMat[0] = modelPose.getR()[0];  viewMat[1] = modelPose.getR()[1];  viewMat[2] = modelPose.getR()[2]; viewMat[3] = modelPose.getT()[0];
        viewMat[4] = modelPose.getR()[3];  viewMat[5] = modelPose.getR()[4];  viewMat[6] = modelPose.getR()[5]; viewMat[7] = modelPose.getT()[1];
        viewMat[8] = modelPose.getR()[6];  viewMat[9] = modelPose.getR()[7];  viewMat[10] = modelPose.getR()[8]; viewMat[11] = modelPose.getT()[2];
        viewMat[12] = 0.0f; viewMat[13] = 0.0f; viewMat[14] = 0.0f; viewMat[15] = 1.0f;
        mModelMat = new Matrix4f(viewMat);;
//        GLES20.glUniformMatrix4fv(mWorldModelLocation, 1, false, pose.getArray(), 0);
    }

    public void drawObject() {
        mSlamObjectProgram.draw(mPerspectiveMat, mViewMat, mModelMat);
    }

    public void drawFeaturePoints(BefSlamInfo slamInfo) {
        if (mFeaturePointProgram != null) {
            PointF point = new PointF();
            for (int i = 0; i < slamInfo.featurePoints.length ; i++) {
                point.x = slamInfo.featurePoints[i].x;
                point.y = slamInfo.featurePoints[i].y;
                mFeaturePointProgram.draw(point, Color.YELLOW,5);
            }
        }
    }

    public void drawWorldCord() {
        mSlamWorldProgram.draw(mPerspectiveMat, mViewMat, mModelMat);
    }
    public void drawModel(int texture, boolean isDrawObj, boolean isDrawWorldCord) {
        if (isDrawObj) {
            drawObject();
        }
        if (isDrawWorldCord) {
            drawWorldCord();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mFeaturePointProgram != null) {
            mFeaturePointProgram.release();
            mFeaturePointProgram = null;
        }
        if (mSlamObjectProgram != null) {
            mSlamObjectProgram.release();
            mSlamObjectProgram = null;
        }
        if (mSlamWorldProgram != null) {
            mSlamWorldProgram.release();
            mSlamWorldProgram = null;
        }
    }

    public static class MatrixUtil {
        static public float[] intrinsicToPerspective(float fx, float fy, float cx, float cy, float width, float height,
                                              float near, float far) {
            float a = -(far + near) / (far - near);
            float b = -(2.0f * far * near) / (far - near);

            float perspective[] = new float[] {
                2.0f * fx / width,  0.0f,                   1.0f - 2.0f * cx / width,   0.f,
                0.0f,               (2.0f * fy) / height,   2.0f * cy / height  - 1.0f, 0.f,
                0.0f,               0.0f,                   a,                          b,
                0.0f,               0.0f,                   -1.0f,                      0.0f
            };
            return perspective;
        }
        static public void matInverse(float mat[]){
            float m00 = mat[0], m01 = mat[1], m02 = mat[2], m03 = mat[3];
            float m10 = mat[4], m11 = mat[5], m12 = mat[6], m13 = mat[7];
            float m20 = mat[8], m21 = mat[9], m22 = mat[10], m23 = mat[11];
            float m30 = mat[12], m31 = mat[13], m32 = mat[14], m33 = mat[15];

            float v0 = m20 * m31 - m21 * m30;
            float v1 = m20 * m32 - m22 * m30;
            float v2 = m20 * m33 - m23 * m30;
            float v3 = m21 * m32 - m22 * m31;
            float v4 = m21 * m33 - m23 * m31;
            float v5 = m22 * m33 - m23 * m32;

            float t00 = +(v5 * m11 - v4 * m12 + v3 * m13);
            float t10 = -(v5 * m10 - v2 * m12 + v1 * m13);
            float t20 = +(v4 * m10 - v2 * m11 + v0 * m13);
            float t30 = -(v3 * m10 - v1 * m11 + v0 * m12);

            float Det = t00 * m00 + t10 * m01 + t20 * m02 + t30 * m03;
            float invDet = 1 / Det;

            float d00 = t00 * invDet;
            float d10 = t10 * invDet;
            float d20 = t20 * invDet;
            float d30 = t30 * invDet;

            float d01 = -(v5 * m01 - v4 * m02 + v3 * m03) * invDet;
            float d11 = +(v5 * m00 - v2 * m02 + v1 * m03) * invDet;
            float d21 = -(v4 * m00 - v2 * m01 + v0 * m03) * invDet;
            float d31 = +(v3 * m00 - v1 * m01 + v0 * m02) * invDet;

            v0 = m10 * m31 - m11 * m30;
            v1 = m10 * m32 - m12 * m30;
            v2 = m10 * m33 - m13 * m30;
            v3 = m11 * m32 - m12 * m31;
            v4 = m11 * m33 - m13 * m31;
            v5 = m12 * m33 - m13 * m32;

            float d02 = +(v5 * m01 - v4 * m02 + v3 * m03) * invDet;
            float d12 = -(v5 * m00 - v2 * m02 + v1 * m03) * invDet;
            float d22 = +(v4 * m00 - v2 * m01 + v0 * m03) * invDet;
            float d32 = -(v3 * m00 - v1 * m01 + v0 * m02) * invDet;

            v0 = m21 * m10 - m20 * m11;
            v1 = m22 * m10 - m20 * m12;
            v2 = m23 * m10 - m20 * m13;
            v3 = m22 * m11 - m21 * m12;
            v4 = m23 * m11 - m21 * m13;
            v5 = m23 * m12 - m22 * m13;

            float d03 = -(v5 * m01 - v4 * m02 + v3 * m03) * invDet;
            float d13 = +(v5 * m00 - v2 * m02 + v1 * m03) * invDet;
            float d23 = -(v4 * m00 - v2 * m01 + v0 * m03) * invDet;
            float d33 = +(v3 * m00 - v1 * m01 + v0 * m02) * invDet;

            mat[0] = d00; mat[1] = d01; mat[2] = d02; mat[3] = d03;
            mat[4] = d10; mat[5] = d11; mat[6] = d12; mat[7] = d13;
            mat[8] = d20; mat[9] = d21; mat[10]= d22; mat[11]= d23;
            mat[12]= d30; mat[13]= d31; mat[14]= d32; mat[15]= d33;

        }


    }



    private static final float[] position = {
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,

            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,

            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,

            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
            0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
            0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,

            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
            0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,

            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f
    };
}
