package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.renderscript.Matrix4f;

import com.effectsar.labcv.common.utils.BeMatrix4f;
import com.effectsar.labcv.common.utils.BeQuaternion;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.SkinMeshUtils;
import com.effectsar.labcv.core.opengl.GlUtil;
import com.effectsar.labcv.core.util.LogUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SkinMeshProgram extends ShaderProgram{
    private ShortBuffer mIndicesBuffer;
    private Map<String, BeMatrix4f> mInverseBindMatrix;
    private FloatBuffer mVertexBuffer;
    private int mDiffuseTexture;
    private ArrayList<String> mBones;

    protected SkinMeshProgram(Context context, String vertex, String frag, int width, int height) {
        super(context, vertex, frag, width, height);
    }

    private static Map<String, BeMatrix4f>
    readInverseBindMatrix(String file, ArrayList<String> bones) {
        HashMap<String, BeMatrix4f> ret = new HashMap<String, BeMatrix4f>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] lines = line.split(" ");
                assert (lines.length >= 16);
                String bone = lines[0];
                BeMatrix4f value = new BeMatrix4f();
                for (int i = 1; i < lines.length; i++) {
                    value.set((i-1) / 4, (i-1) % 4, Float.valueOf(lines[i]));
                }
                bones.add(bone);
                ret.put(bone, value);
            }
        } catch (Exception e) {
            return null;
        }
        return ret;
    }

    private static ShortBuffer
    readIndices(String file) {
        ArrayList<Short> array = new ArrayList<Short>();
        ShortBuffer ret = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] indicesStr = line.split(" ");
                for (String s: indicesStr) {
                    array.add(Short.parseShort(s));
                }
            }
            ret = ByteBuffer.allocateDirect(array.size() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            ret.position(0);
            for (Short s: array) {
                ret.put(s.shortValue());
            }
            ret.position(0);
        } catch (Exception e) {
            return null;
        }
        return ret;
    }

    private static FloatBuffer
    readVertices(String file) {
        ArrayList<Float> vertex = new ArrayList<Float>();
        FloatBuffer ret = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] verticesStr = line.split(" ");
                int index = 0;
                for (String s: verticesStr) {
                    vertex.add(Float.parseFloat(s));
                }
            }
            ret = ByteBuffer.allocateDirect(vertex.size() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            ret.position(0);
            for (Float s: vertex) {
                ret.put(s.floatValue());
            }
            ret.position(0);

        } catch (Exception e) {
            return null;
        }
        return ret;
    }


    public void setRenderResourceFile(String vertexFile, String indicesFile, String inverseBindMatrixFile, String diffuseFile) {
        mVertexBuffer = readVertices(vertexFile);
        mIndicesBuffer = readIndices(indicesFile);

        mBones = new ArrayList<>();
        mInverseBindMatrix = readInverseBindMatrix(inverseBindMatrixFile, mBones);

        Bitmap bmp =  BitmapUtils.decodeBitmapFromFile(diffuseFile, 0 ,0);
        mDiffuseTexture = GlUtil.createImageTexture(bmp);
        if (mDiffuseTexture != -1) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDiffuseTexture);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        assert (mVertexBuffer != null && mIndicesBuffer != null && mInverseBindMatrix != null && mDiffuseTexture != -1);
    }


    public ShortBuffer getIndicesBuffer() {
        return mIndicesBuffer;
    }

    public Map<String, BeMatrix4f> getInverseBindMatrix() {
        return mInverseBindMatrix;
    }

    public FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    public int getDiffuseTexture() {
        return mDiffuseTexture;
    }

    public ArrayList<String> getBones() {
        return mBones;
    }

}