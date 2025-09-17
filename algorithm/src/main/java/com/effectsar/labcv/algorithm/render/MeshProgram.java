package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.renderscript.Matrix4f;

import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.core.opengl.GlUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;


public class MeshProgram extends ShaderProgram {

    private final int mAttributePosition;
//    private final int mAttributeNormal;
    private final int mPerspectiveLocation;
    private final int mViewLocation;
    private final int mWorldModelLocation;
    private final int mTextureCordLocation;
    private final int mTextureUniform;
//    private final int mLightPosUniform;
//    private final int mLightColorUniform;

    private int objTexture = -1;

    private FloatBuffer localCoordinates = null;
    private FloatBuffer meshNormals = null;
    private IntBuffer vertexIndices = null;
    private ShortBuffer shortIndices = null;
    private FloatBuffer textureCoordinates = null;

    protected MeshProgram(Context context, int width, int height) {
        super(context, vertexShader, FragmentShader, width, height);

        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
//        mAttributeNormal = GLES20.glGetAttribLocation(mProgram, "normal");
        GlUtil.checkGlError("enddraw1");
        mTextureCordLocation = GLES20.glGetAttribLocation(mProgram, "textureCord");

        mPerspectiveLocation = GLES20.glGetUniformLocation(mProgram, "u_perspective");
        mViewLocation = GLES20.glGetUniformLocation(mProgram, "u_view");
        mWorldModelLocation = GLES20.glGetUniformLocation(mProgram, "u_model");
        mTextureUniform = GLES20.glGetUniformLocation(mProgram, "inputTexture");

//        mLightPosUniform = GLES20.glGetUniformLocation(mProgram, "u_light_pos");
//        mLightColorUniform = GLES20.glGetUniformLocation(mProgram, "u_light_color");
    }

    public void initModel(String modelPath, String texturePath) {

        try (InputStream inputStream = new FileInputStream(modelPath)) {
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(inputStream));
            localCoordinates = ObjData.getVertices(obj);
            textureCoordinates = ObjData.getTexCoords(obj, 2);
            meshNormals = ObjData.getNormals(obj);
            vertexIndices = ObjData.getFaceVertexIndices(obj, 3);
            shortIndices = ByteBuffer.allocateDirect(2 * vertexIndices.capacity()).order(ByteOrder.nativeOrder()).asShortBuffer();
            for (int i = 0; i < shortIndices.capacity(); i ++) {
                shortIndices.put(i, (short) vertexIndices.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (objTexture != -1 && GLES20.glIsTexture(objTexture)) {
            GLES20.glDeleteTextures(1, new int[]{objTexture}, 0);
        }

        if (texturePath != null) {
            Bitmap bmp =  BitmapUtils.decodeBitmapFromFile(texturePath, 512 ,512);
            objTexture  = GlUtil.createImageTexture(bmp);
            if (objTexture != -1) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objTexture);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }
        }
    }

    public void draw(Matrix4f perspective, Matrix4f view, Matrix4f model) {
        if (perspective == null || view == null || model == null) return ;

        GlUtil.checkGlError("glEnable");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        localCoordinates.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 3, GLES20.GL_FLOAT, false, 0, localCoordinates);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        meshNormals.position(0);
//        GLES20.glVertexAttribPointer(mAttributeNormal, 3, GLES20.GL_FLOAT, false, 0, meshNormals);
//        GLES20.glEnableVertexAttribArray(mAttributeNormal);

        textureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCordLocation, 2, GLES20.GL_FLOAT, false, 0, textureCoordinates);
        GLES20.glEnableVertexAttribArray(mTextureCordLocation);

        useProgram();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objTexture);
        GLES20.glUniform1i(mTextureUniform, 0);

        GLES20.glUniformMatrix4fv(mPerspectiveLocation, 1, true, perspective.getArray(), 0);
        GLES20.glUniformMatrix4fv(mViewLocation, 1, true, view.getArray(), 0);
        GLES20.glUniformMatrix4fv(mWorldModelLocation, 1, true, model.getArray(), 0);

        if (vertexIndices == null) {
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, localCoordinates.array().length);
        } else {
//            GLES20.glEnable(GLES20.GL_CULL_FACE);
            shortIndices.position(0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, shortIndices.capacity(), GLES20.GL_UNSIGNED_SHORT, shortIndices);
//            GLES20.glDisable(GLES20.GL_CULL_FACE);
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
            "uniform mat4 u_model;\n" +
            "uniform mat4 u_view;\n" +
            "uniform mat4 u_perspective;\n" +
            "attribute vec3 position;\n" +
            "attribute vec3 normal;\n" +
            "attribute vec2 textureCord;\n"+
            "varying vec3 v_normal;\n" +
            "varying vec2 v_textureCord;\n" +
            "varying vec3 v_position;\n"+
            "void main() {\n" +
                "gl_Position = u_perspective * u_view * u_model * vec4(position, 1.0);\n" +
                "gl_Position.y = - gl_Position.y;\n"+
                "v_normal = normal;\n" +
                "v_position = (u_model * vec4(position, 1.0)).xyz;\n" +
                "v_textureCord = textureCord;\n"+
            "}";

    private static String FragmentShader = "precision mediump float;\n" +
            "varying vec3 v_position;\n"+
            "varying vec3 v_normal;\n" +
            "varying vec2 v_textureCord;\n" +
            "uniform vec3 u_light_pos;\n "+
            "uniform vec3 u_light_color;\n"+
            "uniform sampler2D inputTexture;\n"+
            "void main() {\n" +
                "vec3 light_dir = normalize(u_light_pos - v_position);\n" +
                "float diff = max(dot(v_normal, light_dir), 0.8);\n" +
                "vec3 diffuse = diff * u_light_color;\n"+
                "vec2 tmp = v_textureCord;\n"+
                "tmp.y = 1.0 - tmp.y;\n"+
                "vec4 sample_color = texture2D(inputTexture, tmp);\n"+
                "gl_FragColor = sample_color; \n"+
            "}";
}
