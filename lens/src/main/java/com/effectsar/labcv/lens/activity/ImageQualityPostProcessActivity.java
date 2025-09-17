package com.effectsar.labcv.lens.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.effectsar.labcv.common.base.BaseBarGLActivity;
import com.effectsar.labcv.common.config.ImageSourceConfig;
import com.effectsar.labcv.common.imgsrc.video.SimplePlayer;
import com.effectsar.labcv.common.imgsrc.video.VideoEncodeHelper;
import com.effectsar.labcv.common.imgsrc.video.VideoSourceImpl;
import com.effectsar.labcv.common.model.ProcessInput;
import com.effectsar.labcv.common.model.ProcessOutput;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.lens.ImageQualityPostProcessManager;
import com.effectsar.labcv.core.lens.ImageQualityResourceHelper;
import com.effectsar.labcv.core.opengl.GlUtil;
import com.effectsar.labcv.core.util.ImageUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.effectsar.labcv.lens.R;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.effectsar.labcv.lens.fragment.BoardFragment;
import com.effectsar.labcv.lens.manager.ImageQualityDataManager;
import com.effectsar.labcv.lens.manager.PostProcessOutput;
import com.effectsar.labcv.lens.view.StepProgressBar;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

import static com.effectsar.labcv.common.config.ImageSourceConfig.IMAGE_SOURCE_CONFIG_KEY;
import static com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.ImageQualityPostProcessType.IMAGE_QUALITY_POST_PROCESS_TYPE_VFI;
import static com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.ImageQualityPostProcessType.IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER;
import static com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.ImageQualityPostProcessType.IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB;
import static com.effectsar.labcv.lens.config.ImageQualityConfig.KEY_VFI;
import static com.effectsar.labcv.lens.config.ImageQualityConfig.KEY_VIDEO_STAB;

public class ImageQualityPostProcessActivity extends BaseBarGLActivity implements
        SimplePlayer.IPlayStateListener,
        BoardFragment.IImageQualityCallback,
        StepProgressBar.OnProgressChangedListener{
    public static final String VIDEO_PATH_KEY = "videoPath";
    public static final String POST_PROCESS_TYPE_KEY = "post_process_type";
    private static final float PROCESS_BAR_DEFAULT_VALUE = 0.8F;

    protected VideoEncodeHelper encodeHelper = null;
    protected VideoSourceImpl videoSource;
    private ImageQualityPostProcessManager postProcessManager;
    private boolean firstFrame = true;
    private boolean inited = false;
    private int mCurTexture = 0;
    private ImageQualityPostProcessManager.PostProcessData processData;
    private String mVideoPath;
    private EffectsSDKEffectConstants.ImageQualityPostProcessType mType;

    private boolean mRecordOn = false;
    private boolean mProcessOn = true;
    private boolean mPlaying = false;

    private ImageQualityDataManager mDataManager;
    private BoardFragment mBoardFragment;
    private ImageQualityConfig mConfig;
    private ImageView mRecordView;
    private StepProgressBar mProcessBar;
    private RelativeLayout mProcessBarRl;
    private ImageView mPlayImageView;
    private FrameLayout flMask;

    private int mFrameRate = 0;
    private float mExpectedFrameRate; // frame rate after frame intersection
    private float mNextExpectedFrameRate = 0; // next frame rate for frame intersection, in case of change when process

    private float mCurVideoPts = 0; // means the video present time
    private float mPreviousPts = 0; // means the previous present time 【mPreviousPts, mCurVideoPts】 makes a interval
    private float mVfiPts = 0; // means the video present time after
    private int mFrameIndex = 0; // when save video framebuffer need pts

    private int mPreviousTexture= 0;
    private boolean mPreviewFrameUsed = false;
    private boolean mHasInsertedFrame = false; // use to decide whether to insert
    private long mSrcVideoPts = 0; // use to save the src video Pts the check if the first frame

    private boolean mVideoStabTrackingState = false;

    public static void startActivity(Context context, EffectsSDKEffectConstants.ImageQualityPostProcessType type, String videoPath) {
        Intent intent = new Intent(context, ImageQualityPostProcessActivity.class);
        intent.putExtra(VIDEO_PATH_KEY, videoPath);
        intent.putExtra(POST_PROCESS_TYPE_KEY, type);
        ImageSourceConfig config =  new ImageSourceConfig();
        intent.putExtra(IMAGE_SOURCE_CONFIG_KEY,  new Gson().toJson(config));
        context.startActivity(intent);
    }

    private BoardFragment getBoardFragment(ImageQualityConfig config, ImageQualityDataManager dataManager) {
        ImageQualityDataManager.ImageQualityItem item = dataManager.getItem(config.getKey());
        if (item == null) {
            return null;
        }
        HashSet selected =  new HashSet<ImageQualityDataManager.ImageQualityItem>();
        selected.add(item);
        BoardFragment fragment = new BoardFragment()
                .setSelectSet(selected)
                .setCallback(this)
                .setItem(item);
        return fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater.from(this)
                .inflate(R.layout.activity_image_quality,
                        findViewById(R.id.fl_base_gl), true);

        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(VIDEO_PATH_KEY);
        mType = (EffectsSDKEffectConstants.ImageQualityPostProcessType )intent.getSerializableExtra(POST_PROCESS_TYPE_KEY);

        postProcessManager = new ImageQualityPostProcessManager(mContext, new ImageQualityResourceHelper(getApplicationContext()));
        processData = new ImageQualityPostProcessManager.PostProcessData();

        flMask = findViewById(R.id.fl_mask);
        flMask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VFI)
            initVFIView();
        else if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER)
            initVideoStabView();
        else if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB) {
            mVideoStabTrackingState = true;
            initVideoStabView();
        }

        if (Config.ALGORITHM_MEMORY_SWITCH){
            Button button = new Button(this);
            button.setText("移除算法");
            ((FrameLayout) findViewById(R.id.viewroot)).addView(button, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            button.setOnClickListener(v->{
                postProcessManager.destroy();
            });
        }
    }

    private void initVFIView() {
        findViewById(R.id.img_open).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_default_activity).setVisibility(View.GONE);

        useImgDefault = false;
        mRecordView = findViewById(R.id.img_record);
        mRecordView.setImageResource(R.drawable.ic_ready_record);
        mConfig = new ImageQualityConfig(KEY_VFI);
        mDataManager = new ImageQualityDataManager();
        mBoardFragment = getBoardFragment(mConfig, mDataManager);

        mProcessBar = findViewById(R.id.progress_vfi);
        mProcessBar.setOnProgressChangedListener(this);
        mPlayImageView = findViewById(R.id.iv_vfi_play);
        mPlayImageView.setOnClickListener(this);

        mProcessBar.setProgress(PROCESS_BAR_DEFAULT_VALUE);
        mProcessBar.setVisibility(View.VISIBLE);
        mPlayImageView.setVisibility(View.VISIBLE);

        mProcessBarRl = findViewById(R.id.rl_progress_vfi);
        mProcessBarRl.setVisibility(View.VISIBLE);

        findViewById(com.effectsar.labcv.common.R.id.root_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mPlaying) {
                   startOrStopPlay(false);
                   return true;
                }
                return false;
            }
        });

        if (mBoardFragment != null) {
            showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", false);
            mBoardFragment.setRecordButton(R.drawable.ic_ready_record);
        }
    }

    private void initVideoStabView() {
        findViewById(R.id.img_open).setVisibility(View.GONE);
        findViewById(R.id.img_default_activity).setVisibility(View.GONE);
        findViewById(R.id.img_record).setVisibility(View.GONE);
        findViewById(R.id.progress_vfi).setVisibility(View.GONE);

        useImgDefault = false;
        mConfig = new ImageQualityConfig(KEY_VIDEO_STAB);
        mDataManager = new ImageQualityDataManager();
        mBoardFragment = getBoardFragment(mConfig, mDataManager);

        mPlayImageView = findViewById(R.id.iv_vfi_play);
        mPlayImageView.setOnClickListener(this);
        mPlayImageView.setVisibility(View.VISIBLE);

        findViewById(com.effectsar.labcv.common.R.id.root_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mPlaying) {
                    startOrStopPlay(false);
                    return true;
                }
                return false;
            }
        });

        if (mBoardFragment != null) {
//            showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", false);
//            mBoardFragment.setRecordButton(R.drawable.ic_ready_record);
        }
    }

    private void setMediaPath (String path) {
        if (videoSource == null) {
            if (encodeHelper == null) {
                encodeHelper = new VideoEncodeHelper();
            }
            videoSource = new VideoSourceImpl(this.mSurfaceView, this, encodeHelper);
            videoSource.setSimplePlayerStepModel(true);
            videoSource.setSimplerAudioOn(false);

            videoSource.open(path, new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    if (videoSource == null) return ;
                    if (!videoSource.isReady()) return ;

                    if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VFI) {
                        mSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    videoSource.update();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//                            LogUtils.e("frame arrived" + surfaceTexture.getTimestamp());
                                long curPts = surfaceTexture.getTimestamp();
                                int textureId = videoSource.getTexture();

                                mPreviousPts = mCurVideoPts;

                                //  {zh} 有些视频首帧的timeStamp 不为 0, 所以用 curPts 判断  {en} The timeStamp of the first frame of some videos is not 0, so use curPts to judge
                                if (mSrcVideoPts == 0 || curPts == 0) {
                                    mCurVideoPts = 0.0f;
                                    mVfiPts = 0.0f;
                                    firstFrame = true;
                                } else {
                                    mCurVideoPts += 1.f / mFrameRate;
                                }

                                mSrcVideoPts = curPts;

                                if (mCurVideoPts == 0.0f) {
                                    mPreviousPts = 0.0f;
                                }

                                ImageUtil.Transition transition = new ImageUtil.Transition();
                                if (videoSource.getOrientation() % 180 == 90) {
//                                    transition.rotate(videoSource.getOrientation()).flip(false, videoSource.isFront());
                                    mTextureHeight = videoSource.getWidth();
                                    mTextureWidth = videoSource.getHeight();
                                } else {
                                    mTextureWidth = videoSource.getWidth();
                                    mTextureHeight = videoSource.getHeight();
                                }

                                if (mPreviousTexture == 0) {
                                    mPreviousTexture = GlUtil.createImageTexture(null,  mTextureWidth, mTextureHeight, GLES20.GL_RGBA);
                                }

                                if (mCurTexture != 0) {
                                    mImageUtil.copyTexture(mCurTexture, mPreviousTexture, mTextureWidth, mTextureHeight);
                                }

                                mCurTexture = mImageUtil.transferTextureToTexture(textureId, EffectsSDKEffectConstants.TextureFormat.Texture_Oes,
                                        EffectsSDKEffectConstants.TextureFormat.Texure2D ,mTextureWidth
                                        ,mTextureHeight, transition, videoSource.getUVMatrix());

                                mSurfaceView.requestRender();
                                mPreviewFrameUsed = false;
                                mHasInsertedFrame = false;
                            }
                        });
                    } else if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB || mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER) {
                        mSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    videoSource.update();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                int textureId = videoSource.getTexture();

                                ImageUtil.Transition transition = new ImageUtil.Transition();
                                if (videoSource.getOrientation() % 180 == 90) {
                                    // videoSource 中surfaceTexture解码的uvMatrix 已经考虑了旋转
//                                    transition.rotate(videoSource.getOrientation()).flip(false, videoSource.isFront());
                                    mTextureHeight = videoSource.getWidth();
                                    mTextureWidth = videoSource.getHeight();
                                } else {
                                    mTextureWidth = videoSource.getWidth();
                                    mTextureHeight = videoSource.getHeight();
                                }

                                mCurTexture = mImageUtil.transferTextureToTexture(textureId, EffectsSDKEffectConstants.TextureFormat.Texture_Oes,
                                        EffectsSDKEffectConstants.TextureFormat.Texure2D ,mTextureWidth
                                        ,mTextureHeight, transition, videoSource.getUVMatrix());

                                mSurfaceView.requestRender();
                            }
                        });
                    }

                }
            });
            videoSource.pausePlay();
        }
    }

    private void updatePerformance(long cost) {
        if (mTimeCost != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTimeCost.setText(""+cost+"ms");
                    mInputSize.setText(mTextureHeight + "*" + mTextureWidth);
                }
            });
        }
    }

    private int  processAndRender(long startMs) {
        int texture = postProcessManager.processTexture(processData);
        long cost = System.currentTimeMillis() - startMs;
        updatePerformance(cost);
        return texture;
    }

    private PostProcessOutput VideoFrameInsertionPostProcess(int texture2d) {
        long startMs = System.currentTimeMillis();

        PostProcessOutput output = new PostProcessOutput();
        if (!GLES20.glIsTexture(texture2d)) {
            LogUtils.e("invalid input texture");
            output.setProcessDone(true);
            output.setTimeCost(0);
            return output;
        }

        if (firstFrame) {
            processData.vfiFlag = 0;
            processData.texture = texture2d;
            processData.scaleX = 1.0f;
            processData.scaleY = 1.0f;
            processData.timeStamp = 0.f;
            processData.textureWidth = mTextureWidth;
            processData.textureHeight= mTextureHeight;

            firstFrame = false;
            output.setTexture(texture2d);
            output.setTimeCost(System.currentTimeMillis() - startMs);
            output.setNeedRecord(true);
            output.setProcessDone(true);

            return output;
        }

//        LogUtils.d("process frame: " + mVfiPts + " " + mPreviousPts + " " + mCurVideoPts);

        // which means we need new frame
        output.setTexture(texture2d);
        if (mVfiPts > mCurVideoPts) {
            output.setProcessDone(true);
            output.setNeedRecord(false);
        }else if (mVfiPts >= mPreviousPts) { // which meas we need add frame
            output.setNeedRecord(true);
            if (mPreviewFrameUsed == false) {
                mPreviewFrameUsed = true;
                output.setTexture(mPreviousTexture);
//                LogUtils.d("add old frame: " + mVfiPts + " old frame time: " + mPreviousPts);
            } else {
                float ratio = (mVfiPts - mPreviousPts) /(mCurVideoPts - mPreviousPts);
                processData.timeStamp = ratio;

//                LogUtils.d("insert frame: " + mVfiPts + " " + mPreviousPts + " " + mCurVideoPts);

                if (!mHasInsertedFrame) {
                    mHasInsertedFrame = true;
                    processData.vfiFlag = 1;
                    processData.textureP = mPreviousTexture;
                    processData.texture = texture2d;
                } else {
                    processData.vfiFlag = 2;
                }
                int texture = processAndRender(startMs);
                if (texture == 0) {
                    output.setNeedRecord(false);
                }
                output.setTexture(texture);
            }
            mVfiPts += 1.f / mExpectedFrameRate;
        } else {
            LogUtils.d("error timeStamp: ");
            mVfiPts += 1.f / mExpectedFrameRate;
        }

        output.setTimeCost(System.currentTimeMillis() - startMs);

        return output;
    }


    static long getFrameNanoSecond(int frameRate, long frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / frameRate;
    }

    //  {zh} 目前的思路是这样，不处理的话，直接上屏  {en} The current idea is this, if you don't deal with it, go directly to the screen
    //  {zh} 出来的话，保存index, 每次处理后 index + 1, 根据处理时间，用帧时间-处理时间作为定时器的值  {en} If it comes out, save index, index + 1 after each processing, according to the processing time, use frame time-processing time as the value of the timer
    //  {zh} 在request render  {en} On request render
    @Override
    public void onDrawFrame(GL10 gl10) {
        if (!inited) {
            setMediaPath(mVideoPath);
            if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VFI)
                videoSource.setSimplerAutoReplay(true);
            inited = true;
            if (postProcessManager == null) {
                postProcessManager = new ImageQualityPostProcessManager(mContext, new ImageQualityResourceHelper(getApplicationContext()));
            }
            postProcessManager.setPostProcessType(mType, true);
            if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER || mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startOrStopPlay(true);
                    }
                });
            }
        }

        if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VFI)
            onVFIDrawFrame();
        else if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB)
            onVideoStabProcess(mVideoStabTrackingState);
        else if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER)
            onVideoDeflickerProcess();
    }

    @Override
    public boolean closeBoardFragment() {
        return false;
    }

    @Override
    public boolean showBoardFragment() {
        if (null == mBoardFragment) return false;
        showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", true);
        mProcessBarRl.setVisibility(View.VISIBLE);
        //  {zh} 关闭 board 的拍照按钮  {en} Turn off the camera button on the board
        return true;
    }

    @Override
    public ProcessOutput processImpl(ProcessInput input) {
        return null;
    }


    @Override
    public void videoAspect(int width, int height, int videoRotation) {
    }

    @Override
    public void frameRate(int rate) {
        mFrameRate = rate;
        mExpectedFrameRate = (float)rate / PROCESS_BAR_DEFAULT_VALUE;
    }

    @Override
    public void onVideoEnd() {
        if (!mVideoStabTrackingState && encodeHelper != null) {
            encodeHelper.stopEncoding();
            LogUtils.e(encodeHelper.getVideoPath());
            encodeHelper.setVideoShouldSave(true);
            String savePath = encodeHelper.getVideoPath();

            //  {zh} 处理完后循环播放处理后的视频，并关闭mask  {en} After processing, loop the processed video and close the mask.
            if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB || mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER) {
                postProcessManager.setPostProcessType(mType, false);
                mProcessOn = false;

                videoSource.close();
                videoSource = null;

                while (encodeHelper.isRecording()) {
                    LogUtils.d("wait for encoding finish");
                }
                setMediaPath(savePath);
                videoSource.setSimplerAutoReplay(true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flMask.setVisibility(View.INVISIBLE);
                        startOrStopPlay(false);
                    }
                });
            }

            saveVideo(savePath);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.show(getString(R.string.capture_video_ok));
                }
            });
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + encodeHelper.getVideoPath())));
            encodeHelper.destroy();
            encodeHelper = null;
        }
        if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB && mVideoStabTrackingState) {
            videoSource.close();
            videoSource = null;
            mVideoStabTrackingState = false;
            setMediaPath(mVideoPath);
            videoSource.continuePlay();
            mFrameIndex = 0;
        }
    }


    // open or close
    @Override
    public void onItem(ImageQualityDataManager.ImageQualityItem item, boolean flag) {
        mProcessOn = flag;
        if (flag && mBubbleTipManager != null){
            mBubbleTipManager.show(item.getTitle(), item.getDesc());
        }
    }

    // board button
    @Override
    public void onClickEvent(View view) {
        if (view.getId() == com.effectsar.labcv.common.R.id.iv_record_board){
            updateRecordButton();
        }else if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mBoardFragment);
            mProcessBarRl.setVisibility(View.INVISIBLE);
        }
    }

    // main screen
    @Override
    public void onClick(View view) {
        if (view.getId() == com.effectsar.labcv.common.R.id.img_record) {
            updateRecordButton();
        } else if (view.getId() == com.effectsar.labcv.common.R.id.img_back) {
            finish();
        } else if (view.getId() == R.id.img_open) {
            showBoardFragment();
        } else if (view.getId() == R.id.iv_vfi_play) {
            startOrStopPlay(true);
        }else if (view.getId() == R.id.img_setting) {
            BubbleWindowManager.ITEM_TYPE[] types;
            types = new BubbleWindowManager.ITEM_TYPE[]{BubbleWindowManager.ITEM_TYPE.PERFORMANCE};
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
            }, types);
        }
    }

    @Override
    protected void onDestroy() {
        if (videoSource != null) {
            videoSource.close();
            videoSource = null;
        }
        if (encodeHelper != null) {
            encodeHelper.destroy();
            encodeHelper = null;
        }
        if (postProcessManager != null) {
            postProcessManager.destroy();
            postProcessManager = null;
        }

        mVideoStabTrackingState = true;
        super.onDestroy();
    }

    private void onVideoStabProcess(boolean trackingState) {
        if (mTextureWidth > ImageQualityPostProcessManager.MAXVASWIDTH || mTextureHeight > ImageQualityPostProcessManager.MAXVASHEIGHT) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.show(getString(R.string.input_invalid));
                }
            });
            return;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int srcTexture = mCurTexture;
        int texture = srcTexture;
        if (!GLES20.glIsTexture(srcTexture)){
            LogUtils.e("input texture is not a valid texture");
            videoSource.continuePlay();
            return;
        }

        PostProcessOutput output = new PostProcessOutput();
        output.setTimeCost(0);

        long startMs = System.currentTimeMillis();
        processData.texture = srcTexture;
        processData.textureWidth = mTextureWidth;
        processData.textureHeight= mTextureHeight;
        processData.videoStabTrackingState = trackingState;
        processData.frameIdx = mFrameIndex ++;

        if (mProcessOn) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView view = (TextView)findViewById(R.id.load_textview);
                    view.setText(R.string.lens_post_progressing);
                    flMask.setVisibility(View.VISIBLE);
                }
            });
            texture = postProcessManager.processTexture(processData);
            output.setTexture(texture);
            long cost = System.currentTimeMillis() - startMs;
            output.setTimeCost(cost);
            updatePerformance(cost);
        }

        output.setProcessDone(true);

        if (texture != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            ImageUtil.Transition drawTransition = new ImageUtil.Transition()
                    .crop(videoSource.getScaleType(), 0, mTextureWidth, mTextureHeight, mSurfaceWidth, mSurfaceHeight);
            mImageUtil.drawFrameOnScreen(texture, EffectsSDKEffectConstants.TextureFormat.Texure2D, mSurfaceWidth, mSurfaceHeight, drawTransition.getMatrix());
        }

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
            if (output.isProcessDone() && mPlaying) {
                if (videoSource != null) {
                    videoSource.continuePlay();
                }
            }
            }
        };

        if (mProcessOn) {
            if (!trackingState) {
                if (encodeHelper == null) {
                    encodeHelper = new VideoEncodeHelper();
                }
                encodeHelper.onVideoData(EGL14.eglGetCurrentContext(), texture, mTextureWidth, mTextureHeight, mFrameRate, getFrameNanoSecond(mFrameRate, mFrameIndex));
            }
            //  {zh} 不间断的执行任务  {en} Uninterrupted execution of tasks
//            timer.schedule(timerTask, 0);
        }
        //  {zh} 正常播放视频  {en} Play video normally
        long delayTime = 1000 / mFrameRate - output.getTimeCost();
        if (delayTime <= 1) delayTime = 1;
        timer.schedule(timerTask, delayTime);

        mFrameRator.addFrameStamp();
    }

    private void onVideoDeflickerProcess() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int srcTexture = mCurTexture;
        int texture = srcTexture;
        if (!GLES20.glIsTexture(srcTexture)){
            LogUtils.e("input texture is not a valid texture");
            videoSource.continuePlay();
            return;
        }

        PostProcessOutput output = new PostProcessOutput();
        output.setTimeCost(0);

        long startMs = System.currentTimeMillis();
        processData.texture = srcTexture;
        processData.textureWidth = mTextureWidth;
        processData.textureHeight= mTextureHeight;

        if (mProcessOn) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView view = (TextView)findViewById(R.id.load_textview);
                    view.setText(R.string.lens_post_progressing);
                   flMask.setVisibility(View.VISIBLE);
                }
            });
            texture = postProcessManager.processTexture(processData);
            output.setTexture(texture);

            long cost = System.currentTimeMillis() - startMs;
            output.setTimeCost(cost);
            updatePerformance(cost);
        }

        output.setProcessDone(true);

        if (texture != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            ImageUtil.Transition drawTransition = new ImageUtil.Transition()
                    .crop(videoSource.getScaleType(), 0, mTextureWidth, mTextureHeight, mSurfaceWidth, mSurfaceHeight);
            mImageUtil.drawFrameOnScreen(texture, EffectsSDKEffectConstants.TextureFormat.Texure2D, mSurfaceWidth, mSurfaceHeight, drawTransition.getMatrix());
        }

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
            if (output.isProcessDone() && mPlaying) {
                if (videoSource != null) {
                    videoSource.continuePlay();
                }
            }
            }
        };

        if (mProcessOn) {
            if (encodeHelper == null) {
               encodeHelper = new VideoEncodeHelper();
            }
           encodeHelper.onVideoData(EGL14.eglGetCurrentContext(), texture, mTextureWidth, mTextureHeight, mFrameRate, getFrameNanoSecond(mFrameRate, mFrameIndex ++));
//            //  {zh} 不间断的执行任务  {en} Uninterrupted execution of tasks
//            timer.schedule(timerTask, 0);
        }
        //  {zh} 正常播放视频  {en} Play video normally
        long delayTime = 1000 / mFrameRate - output.getTimeCost();
        if (delayTime <= 1) delayTime = 1;
        timer.schedule(timerTask, delayTime);

        mFrameRator.addFrameStamp();
    }

    private void onVFIDrawFrame() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int srcTexture = mCurTexture;
        int texture = srcTexture;

        if (!GLES20.glIsTexture(srcTexture)){
            LogUtils.e("output texture not a valid texture");
            videoSource.continuePlay();
            return;
        }

        PostProcessOutput output = new PostProcessOutput();
        output.setProcessDone(true);
        output.setNeedRecord(true);
        output.setTimeCost(0);
        if (mProcessOn) {
            output = VideoFrameInsertionPostProcess(srcTexture);
            texture = output.getTexture();
        } else {
            mVfiPts = mCurVideoPts;
        }

        if (texture == 0) {
            texture = mPreviousTexture;
        }
        if (texture != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            ImageUtil.Transition drawTransition = new ImageUtil.Transition()
                    .crop(videoSource.getScaleType(), 0, mTextureWidth, mTextureHeight, mSurfaceWidth, mSurfaceHeight);
            mImageUtil.drawFrameOnScreen(texture, EffectsSDKEffectConstants.TextureFormat.Texure2D, mSurfaceWidth, mSurfaceHeight, drawTransition.getMatrix());
        } else {
        }
        if (mRecordOn && output.isNeedRecord()) {
            if (encodeHelper == null) {
                encodeHelper = new VideoEncodeHelper();
            }
            encodeHelper.onVideoData(EGL14.eglGetCurrentContext(), texture, mTextureWidth, mTextureHeight, mFrameRate, getFrameNanoSecond(mFrameRate, mFrameIndex ++));
        }

        Timer timer = new Timer();
        PostProcessOutput finalOutput = output;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!finalOutput.isProcessDone()) {
                    if (mSurfaceView != null) {
                        mSurfaceView.requestRender();
                    }
                }

                if (finalOutput.isProcessDone() && mPlaying) {
                    if (videoSource != null) {
                        videoSource.continuePlay();
                    }
                }
            }
        };

        long delayTime = 1000 / mFrameRate - output.getTimeCost();
        if (delayTime <= 1) delayTime = 1;

        timer.schedule(timerTask, delayTime);

        if (output.isProcessDone() && mNextExpectedFrameRate != 0) {
            mExpectedFrameRate = mNextExpectedFrameRate;
        }
        mFrameRator.addFrameStamp();
    }

    private void updateRecordButton() {
        if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VFI) {
            mRecordOn = !mRecordOn;
            if (mRecordOn) {
                mRecordView.setImageResource(R.drawable.ic_under_record);
                mBoardFragment.setRecordButton(R.drawable.ic_under_record);
                if (videoSource != null) {
                    videoSource.setSimplerAutoReplay(false);
                }
                if (null == encodeHelper) {
                    encodeHelper = new VideoEncodeHelper();
                }
            } else {
                mRecordView.setImageResource(R.drawable.ic_ready_record);
                mBoardFragment.setRecordButton(R.drawable.ic_ready_record);
            }
        }
    }

    private void startOrStopPlay(boolean play) {
        mPlaying = play;
        if (play) {
            mPlayImageView.setVisibility(View.INVISIBLE);
            videoSource.continuePlay();
            mFrameRator.start();
        } else {
            mPlayImageView.setVisibility(View.VISIBLE);
            if (videoSource != null) {
                videoSource.pausePlay();
            }
            mFrameRator.stop();
            firstFrame = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetStatus();
        mRecordOn = true;
        updateRecordButton();;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoSource != null) {
            videoSource.close();
            videoSource = null;
        }
        if (encodeHelper != null) {
            encodeHelper.destroy();
            encodeHelper = null;
        }
        if (postProcessManager != null) {
            postProcessManager.destroy();
            postProcessManager = null;
        }
        if (mType == IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB) {
            mVideoStabTrackingState = true;
        }
    }

    @Override
    public void onProgressChanged(StepProgressBar progressBar, float progress, boolean isFormUser) {
        if(mFrameRate != 0) {
            mNextExpectedFrameRate = mFrameRate / progress;
        }
    }

    private void resetStatus() {
        inited = false;
        firstFrame = true;
        mPreviousTexture = 0;
        startOrStopPlay(false);
    }

    private void saveVideo(String path) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, path);
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
            getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

}
