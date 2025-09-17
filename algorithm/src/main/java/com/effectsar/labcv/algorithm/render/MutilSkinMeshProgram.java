package com.effectsar.labcv.algorithm.render;

import android.content.Context;
import android.opengl.GLES20;
import android.renderscript.Matrix4f;
import android.util.Log;

import com.effectsar.labcv.common.utils.BeMatrix4f;
import com.effectsar.labcv.common.utils.BeQuaternion;
import com.effectsar.labcv.common.utils.BeTransform;
import com.effectsar.labcv.common.utils.BeVec3f;
import com.effectsar.labcv.common.utils.BeVec4f;
import com.effectsar.labcv.common.utils.SkinMeshUtils;
import com.effectsar.labcv.core.opengl.GlUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effectsdk.BefSkeleton3DInfo;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MutilSkinMeshProgram extends ShaderProgram
{
    private ArrayList<SkinMeshProgram> mutilSkinMesh;
    private HashMap<String, BeTransform> mBoneTrans;

    private final int mAttributePosition;
    private final int mAttriTextureCord;
    private final int mAtrriBoneID;
    private final int mAttriBoneWeight;

    private final int mPerspectiveUniform;
    private final int mTextureUniform;

    private static final String VERTEX_FILE = "vertex.txt";
    private static final String INDICES_FILE = "indices.txt";
    private static final String INVERSE_BINDING_FILE = "skinInv.txt";
    private static final String DIFFUSE_IMAGE = "diffuse.jpg";
    private HashMap<String, BeQuaternion> mCurrentBoneOrientation = null;
    private HashMap<String, BeQuaternion> mInitBoneOrientation = null;
    private String[] boneIds ;
    private HashMap<String, String> boneNameMap;
    private HashMap<String, String> parentMap ;
    private BeVec3f mPelvisInitPosition = null;
    private BeVec3f mBodyRefposition = new BeVec3f(0, 0, -15);

    protected MutilSkinMeshProgram(Context context, String vertex, String fragment, int width, int height) {
        super(context, vertexShader, fragmentShader, width, height);

        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "aPos");
        mAttriTextureCord = GLES20.glGetAttribLocation(mProgram, "aTexCoords");
        mAtrriBoneID = GLES20.glGetAttribLocation(mProgram, "boneIds");
        mAttriBoneWeight = GLES20.glGetAttribLocation(mProgram, "weights");

        mPerspectiveUniform = GLES20.glGetUniformLocation(mProgram, "mvp");
        mTextureUniform = GLES20.glGetUniformLocation(mProgram, "diffuse");
        mBoneTrans = new HashMap<>();
        boneIds = new String[] {
                "ePelvis",
                "eLeftHip",
                "eRightHip",
                "eSpine",
                "eLeftKnee",
                "eRightKnee",
                "eSpine1",
                "eLeftAnkle",
                "eRightAnkle",
                "eSpine2",
                "eLeftFoot",
                "eRightFoot",
                "eNeck",
                "eLeftCollar",
                "eRightCollar",
                "eHead",
                "eLeftUpperArm",
                "eRightUpperArm",
                "eLeftForeArm",
                "eRightForeArm",
                "eLeftWrist",
                "eRightWrist",
                "eLeftHand",
                "eRightHand"};

        boneNameMap = new HashMap<String, String>(){
            {
                put("ePelvis"          , "Pelvis");
                put("eSpine"           , "Spine1");
                put("eSpine1"          , "Spine2");
                put("eSpine2"          , "Spine3");
                put("eNeck"            , "Neck");
                put("eHead"            , "Head");
                put("eLeftCollar"      , "L_Shoulder");
                put("eLeftUpperArm"    , "L_UpperArm");
                put("eLeftForeArm"     , "L_ForeArm");
                put("eLeftWrist"       , "L_Hand");
                put("eRightCollar"     , "R_Shoulder");
                put("eRightUpperArm"   , "R_UpperArm");
                put("eRightForeArm"    , "R_ForeArm");
                put("eRightWrist"      , "R_Hand");
                put("eLeftHip"         , "L_Thigh");
                put("eLeftKnee"        , "L_Leg");
                put("eLeftAnkle"       , "L_Foot");
                put("eLeftFoot"        , "L_Toe");
                put("eRightHip"        , "R_Thigh");
                put("eRightKnee"       , "R_Leg");
                put("eRightAnkle"      , "R_Foot");
                put("eRightFoot"       , "R_Toe");
            }
        };

        parentMap = new HashMap<String, String>() {
            {
                put("eLeftHip"         ,"ePelvis");
                put("eLeftKnee"        , "eLeftHip");
                put("eLeftAnkle"       , "eLeftKnee");
                put("eLeftFoot"        , "eLeftAnkle");
                put("eRightHip"        , "ePelvis");
                put("eRightKnee"       , "eRightHip");
                put("eRightAnkle"      , "eRightKnee");
                put("eRightFoot"       , "eRightAnkle");
                put("eSpine"           , "ePelvis");
                put("eSpine1"          , "eSpine");
                put("eSpine2"          , "eSpine1");
                put("eNeck"            , "eSpine2");
                put("eHead"            , "eNeck");
                put("eLeftCollar"      , "eSpine2");
                put("eLeftUpperArm"    , "eLeftCollar");
                put("eLeftForeArm"     , "eLeftUpperArm");
                put("eLeftWrist"       , "eLeftForeArm");
                put("eLeftHand"        , "eLeftWrist");
                put("eRightCollar"     , "eSpine2");
                put("eRightUpperArm"   , "eRightCollar");
                put("eRightForeArm"    , "eRightUpperArm");
                put("eRightWrist"      , "eRightForeArm");
                put("eRightHand"       , "eRightWrist");
            }
        };
    }

    public void init(String resourceDir, String initPoseFile, String boneRelationFile) {
        ArrayList<File> dirs = new ArrayList<File>();
        dirs.add(new File(resourceDir, "body"));
        dirs.add(new File(resourceDir, "hudiejie"));
        dirs.add(new File(resourceDir, "yanqiu"));
        mutilSkinMesh = new ArrayList<SkinMeshProgram>();
        for (File f:dirs){
            if (f.isDirectory()) {
                SkinMeshProgram  program = new SkinMeshProgram(mContext, null, null, mWidth, mHeight);

                program.setRenderResourceFile(f.getAbsolutePath() + File.separator + VERTEX_FILE,
                        f.getAbsolutePath() + File.separator + INDICES_FILE,
                        f.getAbsolutePath() + File.separator + INVERSE_BINDING_FILE,
                        f.getAbsolutePath() + File.separator + DIFFUSE_IMAGE);
                mutilSkinMesh.add(program);
            }
        }
        HashMap<String, String>  bonesParent = SkinMeshUtils.readChildToParentDict(boneRelationFile);
        HashMap<String, SkinMeshUtils.BoneInfo> boneInitPose = SkinMeshUtils.readBoneInitPose(initPoseFile);
        if (bonesParent == null || boneInitPose == null) {
            return;
        }
        for (Map.Entry<String, SkinMeshUtils.BoneInfo> entry : boneInitPose.entrySet()) {
            String key = entry.getKey();
            SkinMeshUtils.BoneInfo value = entry.getValue();
            BeTransform transform = new BeTransform(value.translation,  value.scale, value.rotation);
            transform.setName(key);

            mBoneTrans.put(key, transform);
        }

        for (Map.Entry<String, String> entry : bonesParent.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            mBoneTrans.get(key).setParent(mBoneTrans.get(value));
        }

        mInitBoneOrientation = new HashMap<>();
        {
            for (Map.Entry<String, String> entry : boneNameMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                mInitBoneOrientation.put(key, new BeQuaternion(mBoneTrans.get(value).getWorldOrientation()));
            }
        }

        mCurrentBoneOrientation = new HashMap<String, BeQuaternion>();
        mPelvisInitPosition = new BeVec3f(mBoneTrans.get("Pelvis").getWorldPosition());
    }

    private void updatePoseInfo(BefSkeleton3DInfo info) {
        BefSkeleton3DInfo.TargetInfo targetInfo = info.getTargetInfos()[0];
        float[] quaternionArray = targetInfo.quaternion;


        for (int i = 0; i < boneIds.length; i ++) {
            String name = boneIds[i];
            int startIndex = i * 4;
            BeQuaternion curQuaternion = new BeQuaternion(quaternionArray[startIndex], quaternionArray[startIndex + 1],
                    quaternionArray[startIndex + 2] , quaternionArray[startIndex + 3]);

            if (parentMap.containsKey(name)) {
                String parentName = parentMap.get(name);
                BeQuaternion resultQuat = new BeQuaternion();

                if (mCurrentBoneOrientation.containsKey(parentName)){
                    BeQuaternion parentQuat = mCurrentBoneOrientation.get(parentName);
                    resultQuat = BeQuaternion.loadMultiply(parentQuat, curQuaternion);
                }

                mCurrentBoneOrientation.put(name, resultQuat);
            } else {

                mCurrentBoneOrientation.put(name, curQuaternion);
            }

            BeQuaternion quaternion = mCurrentBoneOrientation.get(name);
            if (boneNameMap.containsKey(name)) {
                String realName = boneNameMap.get(name);
                if (mBoneTrans.containsKey(realName) && BeQuaternion.isValid(quaternion)) {
                    BeQuaternion initQuat = mInitBoneOrientation.get(name);
                    BeQuaternion destQuat = BeQuaternion.loadMultiply(quaternion, initQuat);

                    mBoneTrans.get(realName).setWorldOrientation(destQuat);
                }
            }
        }


        // update position
        float[] joints = info.getTargetInfos()[0].joints;
        BeVec3f pelvisAlgoPos = new BeVec3f(joints[0], joints[1], joints[2]);

        pelvisAlgoPos.sub(mBodyRefposition);
        pelvisAlgoPos.scale(20);
        pelvisAlgoPos.add(mPelvisInitPosition);
        mBoneTrans.get("Pelvis").setWorldPosition(pelvisAlgoPos);
    }

    public void render(BefSkeleton3DInfo info) {
        if (info == null || info.getTarget_num() <= 0) return ;
        updatePoseInfo(info);
        {
            float focalLength =  info.getFocal_length();
            float tanAlgoHalfFov = (0.5f * (float) mHeight) / (focalLength * 1.0f);
            float val = (float)Math.atan(tanAlgoHalfFov) * 360.f / 3.1415926f;
            this.useProgram();
            BeMatrix4f perspective = BeMatrix4f.makePerspective(val, (float)mWidth / (float)mHeight, 10.f, 1000.f);
            BeMatrix4f viewMat = new BeMatrix4f();

            viewMat.translate(new BeVec4f(0, 0, -300.f, 0));
            BeMatrix4f model = new BeMatrix4f();

            perspective.multiply(viewMat).multiply(model);

            GLES20.glUniformMatrix4fv(mPerspectiveUniform, 1, false, perspective.toFloatArray(), 0);

        }

        for (SkinMeshProgram program:mutilSkinMesh){
            renderOneSkinMesh(program);
        }
    }

    private void renderOneSkinMesh(SkinMeshProgram program){
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        program.useProgram();

        FloatBuffer vertex = program.getVertexBuffer();
        vertex.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 3, GLES20.GL_FLOAT, false, 16 * 4, vertex);
        GLES20.glEnableVertexAttribArray(mAttributePosition);
        vertex.position(3);
        GLES20.glVertexAttribPointer(mAttriTextureCord, 2, GLES20.GL_FLOAT, false, 16 * 4, vertex);
        GLES20.glEnableVertexAttribArray(mAttriTextureCord);
        vertex.position(8);
        GLES20.glVertexAttribPointer(mAtrriBoneID, 4, GLES20.GL_FLOAT, false, 16 * 4, vertex);
        GLES20.glEnableVertexAttribArray(mAtrriBoneID);
        vertex.position(12);
        GLES20.glVertexAttribPointer(mAttriBoneWeight, 4, GLES20.GL_FLOAT, false, 16 * 4, vertex);
        GLES20.glEnableVertexAttribArray(mAttriBoneWeight);

        ArrayList<String> bones = program.getBones();
        Map<String, BeMatrix4f> inverseBindMatrix = program.getInverseBindMatrix();
        for (int i = 0; i < bones.size(); i ++) {
            String  s = "finalBonesMatrices[" + i + "]";
            int location = GLES20.glGetUniformLocation(mProgram, s);
            String bone = bones.get(i);
            BeMatrix4f world = mBoneTrans.get(bone).getWorldMatrix();
            BeMatrix4f result = BeMatrix4f.loadMultiply(world, inverseBindMatrix.get(bone));

            GLES20.glUniformMatrix4fv(location, 1, false, result.toFloatArray(), 0);
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, program.getDiffuseTexture());
        GLES20.glUniform1i(mTextureUniform, 1);

        ShortBuffer indices = program.getIndicesBuffer();
        GLES20.glCullFace(GLES20.GL_FRONT);

        indices.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.capacity(), GLES20.GL_UNSIGNED_SHORT, indices);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GlUtil.checkGlError("enddraw");
    }

    public void release() {
        for (SkinMeshProgram s:mutilSkinMesh) {
            s.release();
        }
        mutilSkinMesh = null;
    }

    private static String vertexShader = "precision highp float;\n" +
            "attribute vec3 aPos;\n" +
            "attribute vec2 aTexCoords;\n" +
            "attribute vec4 boneIds;\n" +
            "attribute vec4 weights;\n" +
            "\n" +
            "uniform mat4 mvp;\n" +
            "\n" +
            "const int MAX_BONES = 50;\n" +
            "const int MAX_BONE_INFLUENCE = 4;\n" +
            "uniform mat4 finalBonesMatrices[MAX_BONES];\n" +
            "varying vec2 TexCoords;\n" +
            "void main()\n" +
            "{\n" +
            "    vec4 totalPosition = vec4(0.0);\n" +
            "    for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {\n" +
            "        if (boneIds[i] == -1.0)\n" +
            "            continue;\n" +
            "        if (int(boneIds[i]) >= MAX_BONES) {\n" +
            "            totalPosition = vec4(aPos, 1.0);\n" +
            "            break;\n" +
            "        }\n" +
            "        vec4 localPosition = finalBonesMatrices[int(boneIds[i])] * vec4(aPos, 1.0);\n" +
            "        totalPosition += localPosition * weights[i];\n" +
            "    }\n" +
            "    gl_Position = mvp * totalPosition;\n" +
            "    gl_Position.y = - gl_Position.y;\n"+
            "    TexCoords = aTexCoords;\n" +
            "\n" +
            "}";

    private static String fragmentShader = "precision highp float;\n" +
            "varying vec2 TexCoords;\n" +
            "uniform sampler2D diffuse;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "        vec2 tmp = TexCoords;\n" +
            "        tmp.y = 1.0 - tmp.y;\n" +
            "        vec4 color = texture2D(diffuse, tmp);\n" +
            "        gl_FragColor = color;\n" +
            "}";
}