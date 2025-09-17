package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.renderscript.Matrix4f;

import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.core.opengl.GlUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class SlamWorldProgram extends ShaderProgram{

    private final int mPlanePerspectiveLocation;
    private final int mPlaneViewLocation;
    private final int mPlaneAttributePosition;
    private final int mPlaneColorAttributePosition;
    private final int mPlaneTextureCordPosition;
    private final int mPlaneTextureUniform;
    private final int mPlaneuLineMeshConeUniform;
    private final int mPlaneUColorUniform;

    private int planeTexture;

    private FloatBuffer planeConePosition;
    private FloatBuffer planeLanePosition;
    private FloatBuffer planeUnderPosition;
    private FloatBuffer orientPlanePosition;

    protected SlamWorldProgram(Context context, int width, int height) {
        super(context, PlaneVertexShader, PlaneFragmentShader, width, height);
        mPlanePerspectiveLocation = GLES20.glGetUniformLocation(mProgram, "u_perspective");
        mPlaneViewLocation = GLES20.glGetUniformLocation(mProgram, "u_view");

        mPlaneAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mPlaneColorAttributePosition = GLES20.glGetAttribLocation(mProgram, "color");

        mPlaneTextureCordPosition = GLES20.glGetAttribLocation(mProgram, "textureCord");
        mPlaneTextureUniform = GLES20.glGetUniformLocation(mProgram, "u_basemap");

        mPlaneuLineMeshConeUniform = GLES20.glGetUniformLocation(mProgram,"u_line_mesh_cone");
        mPlaneUColorUniform = GLES20.glGetUniformLocation(mProgram, "u_color");
        float scale = 0.2f;
        initCone(scale);
        initLine(scale);
        initUnderPlane(scale, 10);
        initOrientPlane(scale);
        planeTexture = -1;
    }

    public void setTexturePath(String path) {
        if (planeTexture != -1 && GLES20.glIsTexture(planeTexture)) {
            GLES20.glDeleteTextures(1, new int[]{planeTexture}, 0);
        }
        if (path != null) {
            Bitmap bmp =  BitmapUtils.decodeBitmapFromFile(path, 0 ,0);
            planeTexture  = GlUtil.createImageTexture(bmp);
            if (planeTexture != -1) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planeTexture);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            }
        }
    }

    public void draw(Matrix4f perspective, Matrix4f view, Matrix4f model) {
        if (perspective == null || view == null ) return ;

        GlUtil.checkGlError("before");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GlUtil.checkGlError("glEnable");

        planeConePosition.position(0);

        GLES20.glVertexAttribPointer(mPlaneAttributePosition, 3, GLES20.GL_FLOAT, false, 0, planeConePosition);
        GLES20.glEnableVertexAttribArray(mPlaneAttributePosition);

        useProgram();
        GLES20.glUniformMatrix4fv(mPlanePerspectiveLocation, 1, true, perspective.getArray(), 0);
        GLES20.glUniformMatrix4fv(mPlaneViewLocation, 1, true, view.getArray(), 0);
        GLES20.glUniform1i(mPlaneuLineMeshConeUniform, 0);

        // draw cone
        GLES20.glUniform3f(mPlaneUColorUniform, 1.0f, 0.0f, 0.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 182);
        GLES20.glUniform3f(mPlaneUColorUniform, 0.0f, 1.0f, 0.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 182, 182);
        GLES20.glUniform3f(mPlaneUColorUniform, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 182 * 2, 182);

        // draw axis
        GLES20.glEnableVertexAttribArray(mPlaneColorAttributePosition);
        GLES20.glUniform1i(mPlaneuLineMeshConeUniform, 1);
        planeLanePosition.position(0);
        GLES20.glVertexAttribPointer(mPlaneAttributePosition, 3, GLES20.GL_FLOAT, false, 6 * 4, planeLanePosition);
        planeLanePosition.position(3);
        GLES20.glVertexAttribPointer(mPlaneColorAttributePosition, 3, GLES20.GL_FLOAT, false, 6 * 4, planeLanePosition);
        GLES20.glLineWidth(5.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 6);

        // draw under laying plane
        if (planeTexture != -1) {
            GLES20.glEnableVertexAttribArray(mPlaneTextureCordPosition);

            GLES20.glUniform1i(mPlaneuLineMeshConeUniform, 2);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planeTexture);
            GLES20.glUniform1i(mPlaneTextureUniform, 0);

            planeUnderPosition.position(0);
            GLES20.glVertexAttribPointer(mPlaneAttributePosition, 3, GLES20.GL_FLOAT, false, 5 * 4, planeUnderPosition);
            planeUnderPosition.position(3);
            GLES20.glVertexAttribPointer(mPlaneTextureCordPosition, 2, GLES20.GL_FLOAT, false, 5 * 4, planeUnderPosition);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisable(GLES20.GL_BLEND);

            GLES20.glDisableVertexAttribArray(mPlaneAttributePosition);
        }


        // draw middle blue rectangle
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnableVertexAttribArray(mPlaneAttributePosition);
        GLES20.glUniform1i(mPlaneuLineMeshConeUniform, 0);
        GLES20.glUniform3f(mPlaneUColorUniform, 1.0f, 1.0f, 0.0f);
        float[] viewArray = view.getArray();
        viewArray[3] = viewArray[7] = 0;
        viewArray[11] = -1.0f;
        GLES20.glUniformMatrix4fv(mPlaneViewLocation, 1, true, viewArray, 0);
        orientPlanePosition.position(0);
        GLES20.glVertexAttribPointer(mPlaneAttributePosition, 3, GLES20.GL_FLOAT, false, 0, orientPlanePosition);
        GLES20.glLineWidth(5.0f);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPlaneAttributePosition);
        GLES20.glDisableVertexAttribArray(mPlaneColorAttributePosition);
        GLES20.glDisableVertexAttribArray(mPlaneTextureCordPosition);

//        GLES20.glDisableVertexAttribArray(mAttributeNormal);
        GlUtil.checkGlError("glDisableVertexAttribArray");
        GlUtil.checkGlError("glDisable");
    }

    @Override
    public void release() {
        super.release();
        if (planeTexture != -1) {
            if (GLES20.glIsTexture(planeTexture)) {
                GLES20.glDeleteTextures(1, new int[] {planeTexture}, 0);
                planeTexture = -1;
            }
        }
    }

    private void initCone(float scale) {
        if (planeLanePosition == null) {

            float cone_x[] = new float[182 * 3];
            float cone_y[] = new float[182 * 3];
            float cone_z[] = new float[182 * 3];
            float PI = 3.14159265359f;

            cone_x[0] = scale / 10 + scale;
            cone_y[1] = scale / 10 + scale;
            cone_z[2] = scale / 10 + scale;

            for(int i = 1; i <= 180; ++i) {
                float cos_v = (float)Math.cos(i*2.0f/180.0f * PI) * scale / 10f * 0.3f;
                float sin_v = (float)Math.sin(i*2.0f/180.0f * PI) * scale / 10f * 0.3f;
                cone_x[i*3] = scale;
                cone_x[i*3+1] = cos_v;
                cone_x[i*3+2] = sin_v;
                cone_y[i*3] = cos_v;
                cone_y[i*3+1] = scale;
                cone_y[i*3+2] = sin_v;
                cone_z[i*3] = cos_v;
                cone_z[i*3+1] = sin_v;
                cone_z[i*3+2] = scale;
            }
            for (int i = 0; i < 3; ++i) {
                cone_x[181 * 3 + i] = cone_x[3 + i];
                cone_y[181 * 3 + i] = cone_y[3 + i];
                cone_z[181 * 3 + i] = cone_z[3 + i];
            }

            planeConePosition = ByteBuffer.allocateDirect(182 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(); // 182 * 3 + 182 * 3 + 182 * 3
            planeConePosition.position(0);
            planeConePosition.put(cone_x);
            planeConePosition.put(cone_y);
            planeConePosition.put(cone_z);
        }
    }

    private void initLine(float scale) {
        if (planeLanePosition == null) {
            float lines[] = new float[] {
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, scale, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, scale, 0.0f, 0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, scale, 0.0f, 0.0f, 1.0f,
            };
            planeLanePosition = ByteBuffer.allocateDirect(lines.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            planeLanePosition.position(0);
            planeLanePosition.put(lines);
        }
    }

    private void initUnderPlane(float sideLength, float intensity) {
        if (planeUnderPosition == null) {
            float vertices[] = {
                    sideLength, 0.0f, sideLength, intensity, intensity,
                    sideLength, 0.0f, -sideLength, intensity, 0.0f,
                    -sideLength, 0.0f, sideLength, 0.0f, intensity,
                    -sideLength, 0.0f, -sideLength, 0.0f, 0f,
            };
            planeUnderPosition = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            planeUnderPosition.put(vertices);
            planeUnderPosition.position(0);
        }
    }

    private void initOrientPlane(float sideLength) {
        if (orientPlanePosition == null) {
            float vertices[] = {
                    sideLength,        0.0f,  sideLength,
                    sideLength,      0.0f,  -sideLength,
                    -sideLength,     0.0f,  -sideLength,
                    -sideLength,      0.0f,  sideLength,
            };
            orientPlanePosition = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            orientPlanePosition.position(0);
            orientPlanePosition.put(vertices);
        }
    }
    private static String PlaneVertexShader = "precision mediump float;\n" +
            "uniform mat4 u_view;\n" +
            "uniform mat4 u_perspective;\n" +
            "attribute vec3 position;\n" +
            "attribute vec3 normal;\n" +
            "attribute vec3 color;\n" +
            "attribute vec2 textureCord;\n"+
            "varying vec3 v_normal;\n" +
            "varying vec3 v_color;\n" +
            "varying vec2 v_textureCord;\n" +
            "void main() {\n" +
            "v_color = color;\n" +
            "gl_Position = u_perspective * u_view * vec4(position, 1.0);\n" +
            "gl_Position.y = - gl_Position.y;\n"+
            "gl_PointSize = 5.0;\n"+
            "v_normal = normal;\n" +
            "v_textureCord = textureCord;\n"+
            "}";

    private static String PlaneFragmentShader = "precision mediump float;\n"+
            "precision mediump float;\n"+
            "varying vec3 v_color;\n"+
            "varying vec2 v_textureCord;\n"+
            "uniform int u_line_mesh_cone;\n"+
            "uniform sampler2D u_basemap;\n"+
            "uniform vec3 u_color;\n"+
            "void main()\n"+
            "{\n" +
            "if(u_line_mesh_cone == 1)\n"+
                "gl_FragColor = vec4(v_color, 1.0);\n"+
            "else if(u_line_mesh_cone == 2)" +
                "gl_FragColor = texture2D(u_basemap, v_textureCord);"+
            "else\n"+
                "gl_FragColor = vec4(u_color, 1.0);\n"+
            "}";

}
