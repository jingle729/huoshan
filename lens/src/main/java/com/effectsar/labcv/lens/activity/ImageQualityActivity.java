package com.effectsar.labcv.lens.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.TotalCaptureResult;
import android.opengl.GLES20;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;;

import com.effectsar.labcv.common.base.BaseBarGLActivity;
import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.model.ProcessInput;
import com.effectsar.labcv.common.model.ProcessOutput;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.effect.EffectManager;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.lens.ImageQualityInterface;
import com.effectsar.labcv.core.lens.ImageQualityManager;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.core.util.ImageUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.core.util.timer_record.LogTimerRecord;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.effect.view.ProgressBar;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.effectsar.labcv.lens.R;
import com.effectsar.labcv.lens.manager.ImageQualityDataManager;
import com.effectsar.labcv.core.lens.ImageQualityResourceHelper;
import com.effectsar.labcv.lens.view.TaintDetectInfoFragment;
import com.effectsar.labcv.lens.view.VidaInfoFragment;
import com.google.gson.Gson;
import com.effectsar.labcv.lens.fragment.BoardFragment;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.effect.manager.EffectDataManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageQualityActivity extends BaseBarGLActivity
        implements View.OnClickListener, BoardFragment.IImageQualityCallback, EffectManager.OnEffectListener
{
    private ImageQualityInterface mImageQuality;

    private ImageQualityConfig mConfig;
    private ImageQualityDataManager mDataManager;
    private Fragment mBoardFragment;
    private ImageView imgCompare;
    private ImageView imgCompareLine;
    private ProgressBar pb;
    private float mProgress = 0.5f;
    private VidaInfoFragment vidaInfoFragment;
    private TaintDetectInfoFragment taintDetectFragment;
    private volatile boolean isSelected = true;
    private int mWidthPixels, mHeightPixels = 0;
    protected volatile boolean isOn = true;
    protected volatile boolean isSliderHiddenBool = true;
    protected EffectManager mEffectManager = null;
    protected EffectConfig mEffectConfig = null;
    protected EffectDataManager mEffectDataManager = null;
    protected volatile boolean openDefaultBeauty = true;
    protected ImageQualityDataManager.ImageQualityItem mCurSelectItem = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this)
                .inflate(R.layout.activity_image_quality,
                        findViewById(R.id.fl_base_gl), true);

        removeButtonImgDefault();

        initView();

        //   {zh} 默认弹出底部面板       {en} Bottom panel pops up by default  
        showBoardFragment();

        if(mImageSourceProvider instanceof CameraSourceImpl) {
            ((CameraSourceImpl)mImageSourceProvider).setFrameInfoListener(new CameraSourceImpl.CameraFrameInfo() {
                @Override
                public void onFrameInfoArrivedListener(TotalCaptureResult result) {
                    mImageQuality.setFrameInfo(result);
                }
            });
        }

        imgCompare.setVisibility(View.VISIBLE);
        if (mConfig.getKey().equals(ImageQualityConfig.KEY_VIDA)) {
            imgCompare.setVisibility(View.INVISIBLE);
            showVidaInfo(true);
            LayoutInflater.from(this).inflate(R.layout.layout_vida_info, findViewById(R.id.fl_lens_info), true);
        }

        if (mConfig.getKey().equals(ImageQualityConfig.KEY_TAINT_DETECT)) {
            imgCompare.setVisibility(View.INVISIBLE);
            showTaintInfo(true);
            LayoutInflater.from(this).inflate(R.layout.layout_vida_info, findViewById(R.id.fl_lens_info), true);
        }

        if (mConfig.getKey().equals(ImageQualityConfig.KEY_CINE_MOVE)) {
            imgCompare.setVisibility(View.INVISIBLE);
            LayoutInflater.from(this).inflate(R.layout.layout_vida_info, findViewById(R.id.fl_lens_info), true);
        }

        if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
            Intent intent = getIntent();
            String sStr = intent.getStringExtra(EffectConfig.EffectConfigKey);
            LogUtils.d("effectConfig ="+sStr);
            if (sStr == null) {
                mEffectConfig = new EffectConfig().setEffectType(LocaleUtils.isAsia(mContext)? EffectType.LITE_ASIA:EffectType.LITE_NOT_ASIA);
            } else {
                mEffectConfig = new Gson().fromJson(sStr, EffectConfig.class);
            }

            mEffectDataManager = new EffectDataManager(mEffectConfig.getEffectType());
            mEffectManager = new EffectManager(this, new EffectResourceHelper(this), EffectLicenseHelper.getInstance(this));
            mEffectManager.setOnEffectListener(this);
        }
        if (Config.ALGORITHM_MEMORY_SWITCH){
            Button button = new Button(this);
            button.setText("移除算法");
            ((FrameLayout) findViewById(R.id.viewroot)).addView(button, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            button.setOnClickListener(v->{
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        isOn = false;
                        mImageQuality.destroy();
                    }
                });
            });
        }
    }

    private void setCompareLinePos(float progress) {
        // {zh} 设置对比线的位置 {en} Set the position of the contrast line
        int progressWidth = (int) (mWidthPixels * progress);
        int lineWidth = imgCompareLine.getWidth();
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(
                imgCompareLine.getLayoutParams());
        marginLayoutParams.setMargins(progressWidth - (lineWidth / 2), 0, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginLayoutParams);
        imgCompareLine.setLayoutParams(layoutParams);
        /*int lineWidth = imgCompareLine.getWidth();
        int left = progressWidth - (lineWidth / 2);
        imgCompareLine.setLeft(left);
        imgCompareLine.setRight(left + lineWidth);*/
    }

    private void initView() {
        WindowManager manager = ImageQualityActivity.this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mWidthPixels = outMetrics.widthPixels;
        mHeightPixels = outMetrics.heightPixels;

        mConfig = parseConfig(getIntent());
        mDataManager = new ImageQualityDataManager();
        mBoardFragment = getBoardFragment(mConfig, mDataManager);
        if (mBoardFragment != null) {
            showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", false);
        }

        pb = findViewById(R.id.pb1);
        pb.setProgress(mProgress);
        imgCompareLine = findViewById(R.id.img_comoare_len);
        pb.setOnProgressChangedListener(new ProgressBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser) {
                setCompareLinePos(progress);
                mProgress = progress;
            }

            @Override
            public void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser) {

            }
        });

        imgCompare = findViewById(R.id.img_compare);
        imgCompare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        isSliderHiddenBool = !isSliderHiddenBool;
                        if (isSliderHiddenBool) {
                            pb.setVisibility(View.INVISIBLE);
                            imgCompareLine.setVisibility(View.INVISIBLE);
                            imgCompare.setImageResource(com.effectsar.labcv.effect.R.drawable.ic_lens_contrast_off);
                        } else {
                            pb.setVisibility(View.VISIBLE);
                            imgCompareLine.setVisibility(View.VISIBLE);
                            imgCompare.setImageResource(com.effectsar.labcv.effect.R.drawable.ic_lens_contrast_on);
                            setCompareLinePos(mProgress);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void initAlgorithm() {
        mImageQuality = new ImageQualityManager(this, new ImageQualityResourceHelper(getApplicationContext()));
        mImageQuality.init(getExternalFilesDir("assets").getAbsolutePath(), mImageUtil);
        if (null == mDataManager || null == mConfig) return;
        //   {zh} 设置默认开启       {en} Set default on  
        ImageQualityDataManager.ImageQualityItem item = mDataManager.getItem(mConfig.getKey());
        if (mCurSelectItem != null) {
            item = mCurSelectItem;
        }

        if (item != null) {
            mImageQuality.selectImageQuality(item.getType(), isSelected);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);

        initAlgorithm();

        if (mImageSourceProvider instanceof CameraSourceImpl) {
            float[] iso = ((CameraSourceImpl) mImageSourceProvider).getIsoInfo();
            mImageQuality.setCameraIsoInfo((int) iso[0], (int) iso[1]);
        } else {
            mImageQuality.setCameraIsoInfo(0, 0);
        }

        if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
            int ret = mEffectManager.init();
            if (ret != EffectsSDKEffectConstants.EffectsSDKResultCode.BEF_RESULT_SUC) {
                LogUtils.e("oneKeyEnhance mEffectManager.init() fail!! error code =" + ret);
            } else {
                mEffectManager.setPipeline(false);
            }
        }
    }

    private float getShowCompareProgress(int imageWidth, int imageHeight) {
        float progress = mProgress;
        float surfaceAspectRatio = mSurfaceWidth / (float)mSurfaceHeight;
        float imageAspectRatio = imageWidth / (float)imageHeight;
        if (mImageSourceProvider.getScaleType() == ImageView.ScaleType.CENTER_CROP) {
            if (imageAspectRatio > surfaceAspectRatio) {
                float width = imageHeight * surfaceAspectRatio;
                float ratio = width / (float) imageWidth;
                float pading = (1.0f - ratio) / 2;

                progress = pading + (mProgress * ratio);
            }
        } else if (mImageSourceProvider.getScaleType() == ImageView.ScaleType.CENTER_INSIDE) {
            if (imageAspectRatio < surfaceAspectRatio) {
                float width = mSurfaceHeight * imageAspectRatio;
                float ratio = width / (float) mSurfaceWidth;
                float pading = (1.0f - ratio) / 2;
                progress = (mProgress - pading) / ratio;
            }
        }

        return progress;
    }

    private int transferTextureToTextureForCompare(int resultTexture, int inputTexture, int width, int height){
        // {zh} 需要提前处理拍照的逻辑 {en} The logic of taking pictures needs to be processed in advance.

        ProcessOutput output = new ProcessOutput();
        output.texture = resultTexture;
        output.width = width;
        output.height = height;
        handlePic(output);
        // {zh} 计算对比线偏移 {en} Calculate contrast line offset
        int retTexture = mImageUtil.transferTextureToTextureForCompare(resultTexture, inputTexture, getShowCompareProgress(width, height), EffectsSDKEffectConstants.TextureFormat.Texure2D, width, height, new ImageUtil.Transition());
        if (retTexture != 0) {
            return retTexture;
        }

        return resultTexture;
    }

    @Override
    public ProcessOutput processImpl(ProcessInput input) {
        ImageQualityInterface.ImageQualityResult result = new ImageQualityInterface.ImageQualityResult();

        if (isOn && GLES20.glIsTexture(input.getTexture())){
            if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                LogTimerRecord.RECORD("onekey enhance");
            }
            int ret = mImageQuality.processTexture(input.getTexture(), input.getWidth(), input.getHeight(), result);
            if (ret != 0) {
                if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                    if (openDefaultBeauty) {
                        processDefaultBeauty(input.getTexture(), input.getWidth(), input.getHeight(), input.getSensorRotation());

                        if (!isSliderHiddenBool) {
                            output.setTexture(transferTextureToTextureForCompare(output.getTexture(), input.getTexture(), input.getWidth(), input.getHeight()));
                        }
                        return output;
                    }
                }
                output.width = input.getWidth();
                output.height = input.getHeight();
                output.texture = input.getTexture();
                return output;
            }

            if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                if (openDefaultBeauty){
                    processDefaultBeauty(result.getTexture(), result.getWidth(), result.getHeight(), input.getSensorRotation());
                    if (!isSliderHiddenBool) {
                        output.setTexture(transferTextureToTextureForCompare(output.getTexture(), input.getTexture(), input.getWidth(), input.getHeight()));
                    }
                    LogTimerRecord.STOP("onekey enhance");
                    return output;
                } else {
                    LogTimerRecord.STOP("onekey enhance");
                }
            }

            output.setTexture(result.getTexture());
            output.setWidth(result.getWidth());
            output.setHeight(result.getHeight());
            updateAlgoInfo(result);
        }else {
            output.width = input.getWidth();
            output.height = input.getHeight();
            output.texture = input.getTexture();
        }

        return output;
    }

    @Override
    protected void drawOnScreen(ProcessInput input, ProcessOutput output) {
        super.drawOnScreen(input, output);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == com.effectsar.labcv.common.R.id.img_record){
            takePic(output.texture);
            return;
        }
        super.onClick(view);

        if (view.getId() == R.id.img_open) {
            showBoardFragment();
        } else if (view.getId() == R.id.img_setting) {
            BubbleWindowManager.ITEM_TYPE[] types;
            if (mImageSourceProvider instanceof CameraSourceImpl){
                if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                    types = new BubbleWindowManager.ITEM_TYPE[]{BubbleWindowManager.ITEM_TYPE.RESOLUTION, BubbleWindowManager.ITEM_TYPE.BEAUTY, BubbleWindowManager.ITEM_TYPE.PERFORMANCE};
                } else {
                    types = new BubbleWindowManager.ITEM_TYPE[]{BubbleWindowManager.ITEM_TYPE.RESOLUTION, BubbleWindowManager.ITEM_TYPE.PERFORMANCE};
                }
            }else {
                if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                    types = new BubbleWindowManager.ITEM_TYPE[]{BubbleWindowManager.ITEM_TYPE.BEAUTY, BubbleWindowManager.ITEM_TYPE.PERFORMANCE};
                } else {
                    types = new BubbleWindowManager.ITEM_TYPE[]{BubbleWindowManager.ITEM_TYPE.PERFORMANCE};
                }


            }
            if (!mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                if (mConfig.getKey().equals(ImageQualityConfig.KEY_CINE_MOVE)){
                    mBubbleWindowManager.hideResolutionOption(view,480, 1080);
                } else {
                    mBubbleWindowManager.hideResolutionOption(view, 1080);
                }
            }
            mBubbleWindowManager.show(  view, new BubbleWindowManager.BubbleCallback() {
                @Override
                public void onBeautyDefaultChanged(boolean on) {
                    openDefaultBeauty = on;
                }

                @Override
                public void onResolutionChanged(int width, int height) {
                    if (mImageSourceProvider instanceof CameraSourceImpl){
                        ((CameraSourceImpl)mImageSourceProvider).setPreferSize(width,height);
                        ((CameraSourceImpl)mImageSourceProvider).changeCamera(((CameraSourceImpl)mImageSourceProvider).getCameraID(),null);

                    }
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
            }, types);
        }


    }

    @Override
    public void onItem(ImageQualityDataManager.ImageQualityItem item, boolean flag) {
        if (flag && mBubbleTipManager != null){
            mBubbleTipManager.show(item.getTitle(), item.getDesc());
        }
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                isSelected = flag;
                mCurSelectItem = item;
                mImageQuality.selectImageQuality(mCurSelectItem.getType(), flag);
            }
        });

        if (mConfig.getKey().equals(ImageQualityConfig.KEY_VIDA))
            showVidaInfo(flag);
        else if (mConfig.getKey().equals(ImageQualityConfig.KEY_TAINT_DETECT))
            showTaintInfo(flag);
    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == com.effectsar.labcv.common.R.id.iv_record_board){
            takePic(output.texture);
        }else if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mBoardFragment);
        }
    }

    private ImageQualityConfig parseConfig(Intent intent) {
        String sConfig = intent.getStringExtra(ImageQualityConfig.IMAGE_QUALITY_KEY);
        if (sConfig == null) {
            return null;
        }
        LogUtils.d("imagequlity config ="+sConfig);
        return new Gson().fromJson(sConfig, ImageQualityConfig.class);
    }

    private Fragment getBoardFragment(ImageQualityConfig config, ImageQualityDataManager dataManager) {
        ImageQualityDataManager.ImageQualityItem item = dataManager.getItem(config.getKey());
        if (item == null) {
            return null;
        }

        HashSet selected =  new HashSet<ImageQualityDataManager.ImageQualityItem>();
        List<ImageQualityDataManager.ImageQualityItem> items = item instanceof ImageQualityDataManager.ImageQualityItemGroup ?
                ((ImageQualityDataManager.ImageQualityItemGroup) item).getItems() : Collections.singletonList(item);

        mCurSelectItem = items.get(0);
        if (config.getKey().equals(ImageQualityConfig.KEY_CINE_MOVE)) {
            ImageQualityDataManager.ImageQualityItemGroup itemGroup = (ImageQualityDataManager.ImageQualityItemGroup)item;
            for (ImageQualityDataManager.ImageQualityItem childItem : items) {
                if (childItem.getType().name().equals(mConfig.getType())) {
                    item.setType(childItem.getType());
                    mCurSelectItem = childItem;
                }
            }
        }

        selected.add(mCurSelectItem);
        Fragment fragment = new BoardFragment()
                .setSelectSet(selected)
                .setCallback(this)
                .setItem(item);
        return fragment;
    }

    @Override
    public boolean closeBoardFragment() {
        hideBoardFragment(mBoardFragment);
        return true;
    }

    @Override
    public boolean showBoardFragment() {
        if (null == mBoardFragment) return false;
        showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", true);

        return true;
    }

    @Override
    public void onPause() {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mConfig.getKey().equals(ImageQualityConfig.KEY_ONEKEY_ENHANCE)) {
                    if (mEffectManager != null) {
                        mEffectManager.destroy();
                    }
                }
                if (mImageQuality != null) {
                    mImageQuality.destroy();
                }
            }
        });
        // {zh} super.onPause 会触发glsurfaceview释放context，需要放在gl资源释放后调用 {en} super.on Pause will trigger glsurfaceview to release the context, which needs to be called after the gl resource is released
        super.onPause();
    }

    private void showVidaInfo(boolean show) {
        LogUtils.e("show:" + show);
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        vidaInfoFragment = (VidaInfoFragment) fm.findFragmentByTag("vida");

        if (show) {
            if (null == vidaInfoFragment) {
                vidaInfoFragment = new VidaInfoFragment();
                ft.replace(R.id.fl_vida_info, vidaInfoFragment, "vida").commit();
            } else {
                ft.show(vidaInfoFragment).commit();
            }
        } else {
            if (vidaInfoFragment != null) {
                ft.hide(vidaInfoFragment).commit();
            }
        }
    }

    private void showTaintInfo(boolean show) {
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        taintDetectFragment = (TaintDetectInfoFragment) fm.findFragmentByTag("taint_detect");

        if (show) {
            if (taintDetectFragment == null) {
                taintDetectFragment = new TaintDetectInfoFragment();
                ft.replace(R.id.fl_vida_info, taintDetectFragment, "taint_detect").commit();
            } else {
                ft.show(taintDetectFragment).commit();
            }
        } else {
            if (taintDetectFragment != null) {
                ft.hide(taintDetectFragment).commit();
            }
        }
    }

    private void updateAlgoInfo(ImageQualityInterface.ImageQualityResult result) {
        if (mConfig.getKey().equals(ImageQualityConfig.KEY_VIDA)) {
            if (vidaInfoFragment != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vidaInfoFragment.updateProperty(result.getFace(), result.getAes(), result.getClarity());
                    }
                });
            }
        } else if (mConfig.getKey().equals(ImageQualityConfig.KEY_TAINT_DETECT)) {
            if (taintDetectFragment != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taintDetectFragment.updateProperty(result.getScore());
                    }
                });
            }
        }
    }

    public void processDefaultBeauty(int srcTextureId, int width, int height, EffectsSDKEffectConstants.Rotation rotation) {
        int dstTexture = mImageUtil.prepareTexture(width, height);
        mEffectManager.setCameraPosition(mImageSourceProvider.isFront());
        long timestamp = System.nanoTime();
        boolean beautyRet = mEffectManager.process(srcTextureId, dstTexture, width, height, rotation, timestamp);
        output.setWidth(width);
        output.setHeight(height);
        output.setTexture(dstTexture);
        if (!beautyRet) {
            LogUtils.e("OnekeyEnhanceProcess beauty error: " + String.valueOf(beautyRet));
        }
    }

    @Override
    public void onEffectInitialized() {
        LogUtils.d("setBeautyDefault invoked");
        Set<EffectButtonItem> mDefaults = mEffectDataManager.getDefaultItems();
        String[][] nodesAndTags = mEffectDataManager.generateComposerNodesAndTags(mDefaults);
        mEffectManager.setComposeNodes(nodesAndTags[0], nodesAndTags[1]);
        for (EffectButtonItem it : mDefaults) {
            if (it.getNode() == null) {
                continue;
            }
            for (int i = 0; i < it.getNode().getKeyArray().length; i++) {
                mEffectManager.updateComposerNodeIntensity(it.getNode().getPath(),
                        it.getNode().getKeyArray()[i], it.getIntensityArray()[i]);
            }
        }
        mEffectManager.setFilter("");
        mEffectManager.setSticker("");
    }
}
