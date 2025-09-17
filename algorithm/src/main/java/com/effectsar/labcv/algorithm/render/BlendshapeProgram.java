package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.transition.Scene;

import com.effectsar.labcv.common.utils.BeMatrix4f;
import com.effectsar.labcv.common.utils.BeVec4f;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.FileUtils;
import com.effectsar.labcv.core.opengl.GlUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.core.util.timer_record.LogTimerRecord;
import com.effectsar.labcv.core.util.timer_record.TimerRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class BlendshapeProgram extends ShaderProgram{
    private static final String CONGIG_FILE_NAME = "config.json";
    private static final String BSFILE_KEY_VERTEX = "init_vertex";
    private static final String BSFILE_KEY_TEXTURE_COORD = "init_texture_cord";
    private static final String BSFILE_KEY_BLENS_SHAPE  = "blendshapes";
    private static final String BSFILE_KEY_INDICES  = "indices";
    private static final String BSFILE_KEY_DIFFUSE_IMAGE = "diffuse";

    private final int mAttributePosition;
    private final int mAttributeNormal;
    private final int mMvpLocation;
    private final int mTextureCordLocation;
    private final int mTextureUniform;
    private int objTexture = -1;
    private boolean inited = false;

    private ShortBuffer indicesBuffer = null;
    private FloatBuffer glVertexBuffer = null;
    private FloatBuffer glTextCordBuffer = null;

    private float[] initVertexBuffer = null;
    private float[] textCordBuffer = null;
    private float bsBuffer[][] = null;
    private float[] tmpVertexBuffer;

    // use for extra model (hair)
    private int objTextureExtra = -1;
    private ShortBuffer indicesBufferExtra = null;
    private FloatBuffer glVertexBufferExtra = null;
    private FloatBuffer glTextCordBufferExtra = null;
    private float[] initVertexBufferExtra = null;
    private float[] textCordBufferExta = null;

    public BlendshapeProgram(Context context, int width, int heigit) {
        super(context, vertexShader, FragmentShader, width, heigit);

        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mAttributeNormal = GLES20.glGetAttribLocation(mProgram, "normal");
        mTextureCordLocation = GLES20.glGetAttribLocation(mProgram, "textureCord");
        mMvpLocation = GLES20.glGetUniformLocation(mProgram, "u_mvp");

        mTextureUniform = GLES20.glGetUniformLocation(mProgram, "inputTexture");
    }

    // read binary file
    static ShortBuffer readIndices(String file) throws FileNotFoundException {
        File f = new File(file);
        FileInputStream fis = new FileInputStream(f);
        FileChannel channel = fis.getChannel();

        ShortBuffer shortBuffer = null;
        ByteBuffer buffer = ByteBuffer.allocateDirect((int)f.length());
        try {
            channel.read(buffer);
            buffer.flip();

            shortBuffer = buffer.asShortBuffer();
            channel.close();
            fis.close();
        } catch (Exception e) {
            LogUtils.e("file " + file + " read failed!");
        }
        return shortBuffer;
    }

    public void initModel(String resourceDir, boolean driveModel) {
        {
            File f = new File(resourceDir);
            if (!f.exists() || !f.isDirectory()) {
                return;
            }
        }

        // read all the vertex data
        {
            String configFile = resourceDir + "/" + CONGIG_FILE_NAME;

            File f = new File(configFile);
            if (!f.exists()) {
                LogUtils.e("config file is not exist");
                return;
            }

            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] data = new byte[(int) f.length()];

                fis.read(data);
                fis.close();
                String s = new String(data);
                JSONObject jsonObject = new JSONObject(s);

                String initVertex = jsonObject.getString(BSFILE_KEY_VERTEX);
                String textureCord = jsonObject.getString(BSFILE_KEY_TEXTURE_COORD);
                String indices = jsonObject.getString(BSFILE_KEY_INDICES);
                JSONArray bs = jsonObject.getJSONArray(BSFILE_KEY_BLENS_SHAPE);
                String diffuseImage = jsonObject.getString(BSFILE_KEY_DIFFUSE_IMAGE);

                if (initVertex == null ||textureCord == null || indices == null || diffuseImage == null) {
                    return ;
                }

                {
                    Bitmap bmp = BitmapUtils.decodeBitmapFromFile(resourceDir + "/" + diffuseImage, 1024, 1024);

                    if (driveModel) {
                        objTexture = GlUtil.createImageTexture(bmp);
                    } else {
                        objTextureExtra = GlUtil.createImageTexture(bmp);
                    }
                    bmp.recycle();
                }

                if (driveModel) {
                    indicesBuffer = readIndices(resourceDir + "/" + indices);// bufferUnderFlowException
                } else {
                    indicesBufferExtra = readIndices(resourceDir + "/" + indices);
                }

                //  {zh} 并行读取blendshapes资源文件  {en} Parallel reading of blendshapes resource files
                ArrayList<String> filenames = new ArrayList<>();
                filenames.add(resourceDir + "/" +initVertex);
                if (driveModel) {
                    filenames.add(resourceDir + "/" + initVertex);
                }
                filenames.add(resourceDir + "/" +textureCord);
                for (int i = 0; i < bs.length(); i ++) {
                    String j = (String) bs.get(i);
                    filenames.add(resourceDir + "/" + j);
                }
                ExecutorService executor = Executors.newFixedThreadPool(16);
                List<Future<float[]>> futures = new ArrayList<>();
                //  {zh} 提交任务  {en} Submit a task
                for (String filename : filenames) {
                    futures.add(executor.submit(new FileUtils.FileReaderTask(filename)));
                }
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

                if (driveModel) {
                    initVertexBuffer = futures.get(0).get();
                    tmpVertexBuffer = futures.get(1).get();
                    textCordBuffer = futures.get(2).get();
                    bsBuffer = new float[bs.length()][];
                    for (int i = 0; i < bs.length(); i++) {
                        bsBuffer[i] = futures.get(3 + i).get();
                    }

                    // then use the vertex data to minus all the init vertex to get the diff
                    for (int i = 0 ; i < bsBuffer.length; i ++) {
                        float[] dest = bsBuffer[i];
                        float[] src = initVertexBuffer;
                        for (int index = 0; index < src.length; index ++) {
                            dest[index] -= src[index];
                        }
                    }
                } else {
                    initVertexBufferExtra = futures.get(0).get();
                    textCordBufferExta = futures.get(1).get();
                }
            } catch (Exception e){
                return;
            }
        }

        inited = true;
    }

    public void draw(float mvp[], float bsValues[])
    {
        if (!inited) return;

        if (mvp == null || bsValues == null) return;

        GlUtil.checkGlError("startdraw");
        BeMatrix4f inputMVP = new BeMatrix4f(mvp);

        LogTimerRecord.RECORD("bs");
        if (tmpVertexBuffer != null && tmpVertexBuffer.length > 0) {
            System.arraycopy(tmpVertexBuffer, 0, initVertexBuffer, 0, tmpVertexBuffer.length);
        }
        for (int i = 0 ; i < bsBuffer.length; i ++) {
            float[] dest = bsBuffer[i];
            float[] src = initVertexBuffer;
            float bs = bsValues[i];

            for (int index = 0; index < src.length; index ++) {
                src[index] += dest[index] * bs;
            }
        }
        LogTimerRecord.STOP("bs");
        if (glVertexBuffer == null) {
            glVertexBuffer = ByteBuffer.allocateDirect(initVertexBuffer.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        glVertexBuffer.position(0);
        glVertexBuffer.put(initVertexBuffer);
        glVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 3, GLES20.GL_FLOAT, false, 0, glVertexBuffer);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        if (glTextCordBuffer == null) {
            glTextCordBuffer = ByteBuffer.allocateDirect(textCordBuffer.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            glTextCordBuffer.position(0);
        }
        glTextCordBuffer.position(0);
        glTextCordBuffer.put(textCordBuffer);
        glTextCordBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCordLocation, 2, GLES20.GL_FLOAT, false, 0, glTextCordBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCordLocation);

        useProgram();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objTexture);
        GLES20.glUniform1i(mTextureUniform, 0);
        GLES20.glUniformMatrix4fv(mMvpLocation, 1, true, inputMVP.toFloatArray(), 0);

        {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glCullFace(GLES20.GL_FRONT);
            GLES20.glDepthMask(true);
            indicesBuffer.position(0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indicesBuffer);
        }

        // extra model rendering, another render pass
        if (glVertexBufferExtra == null) {
            glVertexBufferExtra = ByteBuffer.allocateDirect(initVertexBufferExtra.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        glVertexBufferExtra.position(0);
        glVertexBufferExtra.put(initVertexBufferExtra);
        glVertexBufferExtra.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 3, GLES20.GL_FLOAT, false, 0, glVertexBufferExtra);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        if (glTextCordBufferExtra == null) {
            glTextCordBufferExtra = ByteBuffer.allocateDirect(textCordBufferExta.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            glTextCordBufferExtra.position(0);
        }
        glTextCordBufferExtra.position(0);
        glTextCordBufferExtra.put(textCordBufferExta);
        glTextCordBufferExtra.position(0);
        GLES20.glVertexAttribPointer(mTextureCordLocation, 2, GLES20.GL_FLOAT, false, 0, glTextCordBufferExtra);
        GLES20.glEnableVertexAttribArray(mTextureCordLocation);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objTextureExtra);
        GLES20.glUniform1i(mTextureUniform, 0);
        GLES20.glUniformMatrix4fv(mMvpLocation, 1, true, inputMVP.toFloatArray(), 0);

        {
            indicesBufferExtra.position(0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesBufferExtra.capacity(), GLES20.GL_UNSIGNED_SHORT, indicesBufferExtra);
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
            "vec4 dir =  u_mvp * vec4(position, 1.0);\n"+
            "dir.y =  -dir.y;\n"+
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
//            "gl_FragColor = vec4(vec3(0.4), 0.8); \n"+

            "gl_FragColor = vec4(sample_color.rgb, 1.0); \n"+
            "}";
}
