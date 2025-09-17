package com.effectsar.labcv.algorithm.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.effectsar.labcv.algorithm.config.AlgorithmConfig;
import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.render.AlgorithmRender;
import com.effectsar.labcv.common.imgsrc.video.VideoSourceImpl;
import com.effectsar.labcv.common.utils.PreviewSizeManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.algorithm.AlgorithmResourceHelper;
import com.effectsar.labcv.algorithm.fragment.AlgorithmBoardFragment;
import com.effectsar.labcv.algorithm.ui.AlgorithmUI;
import com.effectsar.labcv.algorithm.ui.AlgorithmUIFactory;
import com.effectsar.labcv.algorithm.view.TipManager;
import com.effectsar.labcv.common.base.BaseBarGLActivity;
import com.effectsar.labcv.common.model.ProcessInput;
import com.effectsar.labcv.common.model.ProcessOutput;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.algorithm.ObjectTrackingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SlamAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmResourceProvider;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTouchInfo;
import com.effectsar.labcv.core.algorithm.factory.AlgorithmTaskFactory;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.core.license.EffectLicenseProvider;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;

import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.effectsar.platform.struct.CategoryData;

import kotlin.Triple;

public class AlgorithmActivity extends BaseBarGLActivity
        implements AlgorithmUI.AlgorithmInfoProvider,
        AlgorithmBoardFragment.IAlgorithmCallback {

    public static final String BOARD_FRAGMENT_TAB = "algorithm_board_tag";

    private AlgorithmTask<?, ?> mAlgorithmTask;
    private AlgorithmUI<Object> mAlgorithmUI;
    private Map<AlgorithmTaskKey, Object> mAlgorithmParams;
    private AlgorithmRender mAlgorithmRender;

    private TextView mExtraView;
    private TextView mExtraInfoView;

    @IdRes
    private int mBoardFragmentTargetId = R.id.fl_algorithm_board;
    private Fragment mBoardFragment;
    private TipManager mTipManager;

    private AlgorithmConfig mConfig;
    private boolean mAlgorithmOn = false;
    private Object mResult;

    private int mOutputTexture = 0;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(getContext())
                .inflate(R.layout.activity_algorithm,
                        findViewById(R.id.fl_base_gl), true);

        removeButtonImgDefault();

        initAlgorithm();

        initView();
        FrameLayout viewRoot = ((FrameLayout) findViewById(R.id.root_view));
        viewRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                closeBoardFragment();
                if (mAlgorithmTask.key() == SlamAlgorithmTask.SLAM) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();

                    x = PreviewSizeManager.getInstance().viewToPreviewXFactor(x);
                    y = PreviewSizeManager.getInstance().viewToPreviewYFactor(y);

                    AlgorithmTouchInfo info = new AlgorithmTouchInfo(x, y, motionEvent.getAction());
                    if (mSurfaceView != null) {
                        mSurfaceView.queueEvent(() -> mAlgorithmTask.setConfig(SlamAlgorithmTask.SLAM_CLICK_FLAG, info));
                    }
                } else if (mAlgorithmTask.key() == ObjectTrackingAlgorithmTask.OBJECT_TRACKING) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();

                    x = PreviewSizeManager.getInstance().viewToPreviewX(x);
                    y = PreviewSizeManager.getInstance().viewToPreviewY(y);
                    AlgorithmTouchInfo info = new AlgorithmTouchInfo(x, y, motionEvent.getAction());

                    if (mSurfaceView != null) {
                        mSurfaceView.queueEvent(() -> mAlgorithmTask.setConfig(ObjectTrackingAlgorithmTask.OBJECT_TRACKING_TOUCH_EVENT, info));
                    }

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        showBoardFragment(mBoardFragment, mBoardFragmentTargetId, BOARD_FRAGMENT_TAB, true);
                    }
                    return true;
                }
                return false;
            }
        });

        if (Config.ALGORITHM_MEMORY_SWITCH){
            Button button = new Button(this);
            button.setText("移除算法");
            viewRoot.addView(button, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            button.setOnClickListener(v->{
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mAlgorithmOn = false;
                        mAlgorithmTask.destroyTask();
                        mAlgorithmRender.destroy();
                        mImageUtil.release();
                    }
                });
            });
        }

    }


    private void initAlgorithm() {
        mConfig = parseAlgorithmConfig(getIntent());

        AlgorithmResourceHelper algorithmResourceHelper = new AlgorithmResourceHelper(getContext());

//        CategoryData categoryData = parseCategoryData(getIntent());
//        if (categoryData != null) {
//            String modelRootPath = PlatformUtils.getModelRootPath().getAbsolutePath();
//            algorithmResourceHelper.setModelRootPath(modelRootPath);
//        }

        mAlgorithmTask = createAlgorithmTask(mConfig, algorithmResourceHelper, EffectLicenseHelper.getInstance(getContext()));
        mAlgorithmUI = createAlgorithmUI(mConfig);

        mAlgorithmParams = createAlgorithmParams(mConfig);
        mAlgorithmRender = new AlgorithmRender(getContext());

    }

    private void initView() {
        mTipManager = new TipManager();
        mAlgorithmUI.init(this);
        mTipManager.init(getContext(), findViewById(R.id.fl_algorithm_info));
        mBoardFragment = getBoardFragment(new HashSet<>(mAlgorithmParams.keySet()));

        if (mAlgorithmTask.key() == SlamAlgorithmTask.SLAM) {
            mExtraView = findViewById(R.id.title_slam_extra);
            mExtraInfoView = findViewById(R.id.tv_slam_extra);
            mExtraView.setVisibility(View.VISIBLE);
            mExtraInfoView.setVisibility(View.VISIBLE);
        }

        if (mConfig.isShowBoard()) {
            showBoardFragment(mBoardFragment, mBoardFragmentTargetId, BOARD_FRAGMENT_TAB, false);
        }
    }


    @Override
    public boolean showBoardFragment() {
        if (null == mBoardFragment){
            mBoardFragment = getBoardFragment(new HashSet<>(mAlgorithmParams.keySet()));
        }
        return false;
    }

    @Override
    protected void onPause() {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(mOutputTexture != 0) {
                    com.effectsar.labcv.core.opengl.GlUtil.deleteTextureId(mOutputTexture);
                    mOutputTexture = 0;
                }
                mAlgorithmTask.destroyTask();
                mAlgorithmRender.destroy();
            }
        });
        super.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);

        mAlgorithmTask.initTask();

        for (Map.Entry<AlgorithmTaskKey, Object> entry : mAlgorithmParams.entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                boolean flag = (boolean) entry.getValue();
                onItem(entry.getKey(), flag);

            }
        }

        // if use vehook, then shell will delete this
        if (mConfig != null && mConfig.isAutoTest()){
               LogUtils.d("hit AutoTest");
              if (mImageSourceProvider instanceof VideoSourceImpl){
                  runOnUiThread(()->{
                      startPlay();
                  });
              }
       }
        // delete above don't delete this line

        // vehook_add don't delete this line
    }

    @Override
    public ProcessOutput processImpl(ProcessInput input) {
        if (input.getHeight() == 0 || input.getWidth() == 0) return null;


        LogUtils.d("mAlgorithmOn ="+mAlgorithmOn);
        if (mAlgorithmOn) {
            //   {zh} 将转正后的纹理转成 ByteBuffer       {en} Convert the regular texture to ByteBuffer
            ByteBuffer bb2 = mImageUtil.transferTextureToBuffer(
                    input.getTexture(), EffectsSDKEffectConstants.TextureFormat.Texure2D, EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                    input.getWidth(), input.getHeight(), 1
            );
            //   {zh} 执行算法检测，并更新 UI       {en} Perform algorithm detection and update the UI  
            if (mAlgorithmTask.key() != SlamAlgorithmTask.SLAM) {
                mResult = mAlgorithmTask.process(bb2, input.getWidth(), input.getHeight(),
                        input.getWidth() * 4, EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                        input.getSensorRotation());
            } else { //  {zh} slam 算法需要传入时间戳  {en} The slam algorithm requires an incoming timestamp
                mResult = mAlgorithmTask.process(bb2, input.getWidth(), input.getHeight(),
                            input.getWidth() * 4, EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                            input.getSensorRotation(),  mImageSourceProvider.getTimestamp());
                if (mResult instanceof SlamAlgorithmTask.SlamRenderInfo) {
                    if (((SlamAlgorithmTask.SlamRenderInfo) mResult).slamInfo != null){
                        int trackingState =  ((SlamAlgorithmTask.SlamRenderInfo) mResult).slamInfo.cameraPose.getTrackingState();
                        changeSlamStatus(trackingState);
                    }
                }
            }

            mAlgorithmUI.onReceiveResult(mResult);

            //   {zh} 将算法结果绘制到目标纹理上       {en} Draw algorithm results onto target texture
            mAlgorithmRender.init(input.getWidth(), input.getHeight());
            mAlgorithmRender.setResizeRatio(1);

            mAlgorithmRender.drawAlgorithmResult(mResult, input.getTexture());

        }

        if(mOutputTexture == 0)
        {
            mOutputTexture = com.effectsar.labcv.core.opengl.GlUtil.createImageTexture(null,  mTextureWidth, mTextureHeight, GLES20.GL_RGBA);
        }
        mImageUtil.copyTexture(input.getTexture(),mOutputTexture,mTextureWidth, mTextureHeight);
        output.setTexture(mOutputTexture);

        output.setWidth(input.getWidth());
        output.setHeight(input.getHeight());
        return output;
    }

    @Override
    public ProcessOutput processImageImpl(ProcessInput input) {
        return null;
    }

    @Override
    public void processImageYUV(byte[] data, int format, EffectsSDKEffectConstants.Rotation rotation, int width, int height, int runtimes) {
    }

    @Override
    public TipManager getTipManager() {
        return mTipManager;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public int getPreviewWidth() {
        return mImageSourceProvider.getWidth();
    }

    @Override
    public int getPreviewHeight() {
        return mImageSourceProvider.getHeight();
    }

    @Override
    public boolean fitCenter() {
        return false;
    }

    @Override
    public FragmentManager getFMManager() {
        return getSupportFragmentManager();
    }

    @Override
    public void setBoardTarget(int targetId) {
        mBoardFragmentTargetId = targetId;
    }

    @Override
    public void onItem(AlgorithmItem item, boolean flag) {
        if (null != mSurfaceView){
            mSurfaceView.queueEvent(()->{
                onItem(item.getKey(), flag);

            });
        }

        if (flag) {
            mBubbleTipManager.show(item.getTitle(), item.getDesc());
        }
        //  {zh} 数据UI同步，避免在关闭算法后 进入视频模式再回退，出现默认开启的问题  {en} Data UI synchronization, avoid the problem of default opening after closing the algorithm, entering video mode and then backing off
        mAlgorithmParams.put(item.getKey(), flag);
    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mBoardFragment);
        } else if (view.getId() == R.id.iv_record_board) {
            takePic();
        }
    }

    @Override
    public boolean closeBoardFragment() {
        if (mBoardFragment != null && mBoardFragment.isVisible()) {
            hideBoardFragment(mBoardFragment);
            return true;
        }
        return false;

    }


    private void onItem(AlgorithmTaskKey key, boolean flag) {
        //   {zh} 算法 key 控制算法是否开启       {en} Algorithm key controls whether the algorithm is turned on
        mSurfaceView.queueEvent(()->{
            if (key.getKey().equals(mConfig.getType())) {
                mAlgorithmOn = flag;
            }
            mAlgorithmTask.setConfig(key, flag);
        });

        runOnUiThread(()->{
            mAlgorithmUI.onEvent(key, flag);

        });
    }

    @Override
    protected void takePic() {
        takePic(output.getTexture());
    }

    @Override
    public void onClick(View view) {
        LogUtils.d("onClick"+view);
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.img_open) {
            showBoardFragment(mBoardFragment, mBoardFragmentTargetId, BOARD_FRAGMENT_TAB, true);
        } else if(id == R.id.img_setting){
            mBubbleWindowManager.show(view, new BubbleWindowManager.BubbleCallback() {


                @Override
                public void onBeautyDefaultChanged(boolean on) {

                }

                @Override
                public void onResolutionChanged(int width, int height) {

                }

                @Override
                public void onPerformanceChanged(boolean on) {
                    mRlPerformance.setVisibility(on?View.VISIBLE:View.INVISIBLE);
                }

                @Override
                public void onPictureModeChanged(boolean on) {

                }
                @Override
                public void onVideoScaleChanged(int width, int height) {}
            }, BubbleWindowManager.ITEM_TYPE.PERFORMANCE);
        }
    }

    private Fragment getBoardFragment(HashSet<AlgorithmTaskKey> selectItems) {
        if (mAlgorithmUI.getFragmentGenerator() != null) {
            return new AlgorithmBoardFragment(this)
                    .setInnerFragment(mAlgorithmUI.getFragmentGenerator().create())
                    .setTitleId(mAlgorithmUI.getFragmentGenerator().title());
        } else {
            return new AlgorithmBoardFragment(this)
                    .setSelectSet(selectItems)
                    .setItem(mAlgorithmUI.getAlgorithmItem());
        }
    }

    private CategoryData parseCategoryData(Intent intent) {
        String sCategoryData = intent.getStringExtra(CategoryData.class.toString());
        if (sCategoryData == null) {
            return null;
        }

//        LogUtils.d("sCategoryData = "+sCategoryData);
        return new Gson().fromJson(sCategoryData, CategoryData.class);
    }

    private AlgorithmConfig parseAlgorithmConfig(Intent intent) {
        String sAlgorithmConfig = intent.getStringExtra(AlgorithmConfig.ALGORITHM_CONFIG_KEY);
        if (sAlgorithmConfig == null) {
            return null;
        }
        LogUtils.d("sAlgorithmConfig ="+sAlgorithmConfig);

        return new Gson().fromJson(sAlgorithmConfig, AlgorithmConfig.class);
    }

    private AlgorithmTask<?, ?> createAlgorithmTask(AlgorithmConfig config, AlgorithmResourceProvider provider, EffectLicenseProvider licenseProvider) {
        return AlgorithmTaskFactory.create(
                getKeyFromType(config.getType()),
                getApplicationContext(),
                provider,
                licenseProvider);
    }

    private AlgorithmUI<Object> createAlgorithmUI(AlgorithmConfig config) {
        return AlgorithmUIFactory.create(getKeyFromType(config.getType()));
    }

    private Map<AlgorithmTaskKey, Object> createAlgorithmParams(AlgorithmConfig config) {
        HashMap<AlgorithmTaskKey, Object> params = new HashMap<>();
        for (Map.Entry<String, Object> entry : config.getParams().entrySet()) {
            params.put(getKeyFromType(entry.getKey()), entry.getValue());
        }
        return params;
    }

    private AlgorithmTaskKey getKeyFromType(String stickerType) {
        return new AlgorithmTaskKey(stickerType);
    }

    private void changeSlamStatus(int trackingState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (trackingState == -1) {
                    mExtraInfoView.setText(R.string.slam_tracking_error);
                } else if (trackingState == 0) {
                    mExtraInfoView.setText(R.string.slam_tracking_init);
                } else if (trackingState == 1) {
                    mExtraInfoView.setText(R.string.slam_tracking_tracking);
                } else {
                    mExtraInfoView.setText(R.string.slam_tracking_lost);
                }
            }
        });

    }


}
