package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.FloatBuffer;


public class ChromaKeyingMaskProgram extends ShaderProgram{
    private float[] mMaskColor;
    private int uMaskColorLocation = -1;
    private int uInputTexture = -1;

    private int aPosition = -1;
    private int aInputTextureCoordinate = -1;

    public ChromaKeyingMaskProgram(Context context, int width, int height) {
        super(context, CAMERA_INPUT_VERTEX_SHADER,FRAGMENT_CHROMA_KEYING, width, height);
        GLES20.glUseProgram(mProgram);
        uMaskColorLocation = GLES20.glGetUniformLocation(mProgram, "maskColor");
        uInputTexture = GLES20.glGetUniformLocation(mProgram, "inputMaskTexture");

        aPosition = GLES20.glGetAttribLocation(mProgram, "position");
        aInputTextureCoordinate = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
    }


    public void drawMaskTexture(int inputTexture, float[] color, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        useProgram();
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(aInputTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(aInputTextureCoordinate);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
        GLES20.glUniform1i(uInputTexture, 0);

        GLES20.glUniform3f(uMaskColorLocation, color[0], color[1], color[2]);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	textureCoordinate = vec2(inputTextureCoordinate.x, 1.0 -inputTextureCoordinate.y);\n" +
            "	gl_Position = position;\n" +
            "}";

    protected static final String FRAGMENT_CHROMA_KEYING =
            "precision mediump float;\n" +
                    "varying highp vec2 textureCoordinate;\n" +
                    " \n" +
                    "uniform sampler2D inputMaskTexture;\n" +
                    "uniform vec3 maskColor;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "vec4 maska = texture2D(inputMaskTexture, textureCoordinate);\n" +
                    "vec3 color = mix(maskColor, maska.rgb, maska.a);\n" +
                    "gl_FragColor = vec4(color, 1.0);\n" +
                    "}";
}
