package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.core.opengl.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class FaceMeshProgram extends ShaderProgram {

    private final int mAttributePosition;
    private final int mAttributeNormal;
    private final int mMvpLocation;
    private final int mTextureCordLocation;
    private final int mTextureUniform;

    private int objTexture = -1;

    private FloatBuffer localCoordinates = null;
    private FloatBuffer meshNormals = null;
    private IntBuffer vertexIndices = null;
    private ShortBuffer shortIndices = null;
    private FloatBuffer textureCoordinates = null;

    protected FaceMeshProgram(Context context, int width, int height) {
        super(context, vertexShader, FragmentShader, width, height);

        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mAttributeNormal = GLES20.glGetAttribLocation(mProgram, "normal");
        mTextureCordLocation = GLES20.glGetAttribLocation(mProgram, "textureCord");
        mMvpLocation = GLES20.glGetUniformLocation(mProgram, "u_mvp");

        mTextureUniform = GLES20.glGetUniformLocation(mProgram, "inputTexture");

    }

    public void initModel(String modelPath, String texturePath) {
        if (objTexture != -1 && GLES20.glIsTexture(objTexture)) {
            GLES20.glDeleteTextures(1, new int[]{objTexture}, 0);
        }

        if (texturePath != null) {
            Bitmap bmp =  BitmapUtils.decodeBitmapFromFile(texturePath, 1024 ,1024);
            objTexture  = GlUtil.createImageTexture(bmp);
            if (objTexture != -1) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objTexture);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }
        }
    }

    public void draw(float []mvp, float[] vertex, float []normal, float[] uv, short[] indices) {
        if (mvp == null || vertex == null || normal == null || uv == null) return ;

        GlUtil.checkGlError("glEnable");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        if (localCoordinates == null) {
            localCoordinates = ByteBuffer.allocateDirect(vertex.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        localCoordinates.position(0);
        localCoordinates.put(vertex);
        localCoordinates.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 3, GLES20.GL_FLOAT, false, 0, localCoordinates);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        if (meshNormals == null) {
            meshNormals = ByteBuffer.allocateDirect(vertex.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
//        meshNormals.position(0);
//        meshNormals.put(normal);
//        meshNormals.position(0);
//        GLES20.glVertexAttribPointer(mAttributeNormal, 3, GLES20.GL_FLOAT, false, 0, meshNormals);
//        GLES20.glEnableVertexAttribArray(mAttributeNormal);

        if (textureCoordinates == null) {
            textureCoordinates = ByteBuffer.allocateDirect(uv.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        textureCoordinates.position(0);
        textureCoordinates.put(uv);
        textureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCordLocation, 2, GLES20.GL_FLOAT, false, 0, textureCoordinates);
        GLES20.glEnableVertexAttribArray(mTextureCordLocation);

        useProgram();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objTexture);
        GLES20.glUniform1i(mTextureUniform, 0);
        GLES20.glUniformMatrix4fv(mMvpLocation, 1, true, mvp, 0);

        {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glCullFace(GLES20.GL_FRONT);
            GLES20.glDepthMask(true);
            if (shortIndices == null) {
                shortIndices = ByteBuffer.allocateDirect(2 * indices.length).order(ByteOrder.nativeOrder()).asShortBuffer();
            }
            shortIndices.position(0);

            shortIndices.put(indices);
            shortIndices.position(0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, shortIndices.capacity(), GLES20.GL_UNSIGNED_SHORT, shortIndices);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_BLEND);
        }
        GLES20.glDisableVertexAttribArray(mAttributePosition);
        GLES20.glDisableVertexAttribArray(mTextureCordLocation);
//        GLES20.glDisableVertexAttribArray(mAttributeNormal);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GlUtil.checkGlError("enddraw");
    }

    @Override
    public void release() {
        super.release();
        if (objTexture != -1 && GLES20.glIsTexture(objTexture)) {
            GLES20.glDeleteTextures(1, new int[]{objTexture}, 0);
            objTexture = -1;
        }
    }

    private static String vertexShader = "precision mediump float;\n" +
            "uniform mat4 u_mvp;\n" +
            "attribute vec3 position;\n" +
            "attribute vec3 normal;\n" +
            "attribute vec2 textureCord;\n"+
            "varying vec3 v_normal;\n" +
            "varying vec2 v_textureCord;\n" +
            "varying vec3 v_position;\n"+
            "void main() {\n" +
            "vec4 dir =  u_mvp * vec4(position, 1.0);"+
            "gl_Position = vec4(dir);\n" +
            "v_textureCord = textureCord;\n"+
            "}";

    private static String FragmentShader = "precision mediump float;\n" +
            "varying vec3 v_position;\n"+
            "varying vec3 v_normal;\n" +
            "varying vec2 v_textureCord;\n" +

            "uniform sampler2D inputTexture;\n"+
            "void main() {\n" +
            "vec2 tmp = v_textureCord;\n"+
            "tmp.y = 1.0 - tmp.y;"+
            "vec4 sample_color = texture2D(inputTexture, tmp);\n"+
            "gl_FragColor = vec4(sample_color.rgb, 0.8); \n"+
            "}";
}
