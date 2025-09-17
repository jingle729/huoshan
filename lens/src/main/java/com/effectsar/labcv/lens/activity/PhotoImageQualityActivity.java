package com.effectsar.labcv.lens.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.effectsar.labcv.common.base.BaseBarGLActivity;
import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.ProcessInput;
import com.effectsar.labcv.common.model.ProcessOutput;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.lens.ImageQualityResourceHelper;
import com.effectsar.labcv.core.opengl.ProgramTextureNV21;
import com.effectsar.labcv.core.util.ImageUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.core.util.timer_record.LogTimerRecord;
import com.effectsar.labcv.lens.R;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.effectsar.labcv.lens.fragment.BoardFragment;
import com.effectsar.labcv.lens.manager.ImageQualityDataManager;
import com.effectsar.labcv.lens.manager.PhotoNightSceneHandler;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.microedition.khronos.opengles.GL10;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import static com.effectsar.labcv.core.lens.util.ImageQualityUtil.isPhotoImageQualityNotSupport;


public class PhotoImageQualityActivity extends BaseBarGLActivity
        implements View.OnClickListener,  BoardFragment.IImageQualityCallback, PhotoNightSceneHandler.PhotoNightSceneCallback {

    public enum PreviewStatus {
        PREVIEW_STATUS_PREVIEW,
        PREVIEW_STATUS_PREVIEW_PHOTO,
        PREVIEW_STATUS_AFTER_PROCESS,
    };

    private int saveIndex = 0;

    private ImageQualityDataManager mDataManager;
    private Fragment mBoardFragment;
    private ImageQualityConfig mConfig;
    private CircularProgressBar mCircularProgressBar;
    private PhotoNightSceneHandler mPhotoNightSceneHandler;
    protected  volatile  boolean isOn = true;

    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            if (image == null) {
                return;
            }
            LogUtils.e("Image accured" + image.toString() + " " + saveIndex);
            int width = 0;
            int height = 0;
            if(saveIndex == 0) {
                LogTimerRecord.RECORD("total process");
                if (isOn) {
                    findViewById(R.id.title_fps).setVisibility(View.INVISIBLE);
                    mRlPerformance.setVisibility(View.VISIBLE);
                }
                findViewById(R.id.tv_fps).setVisibility(View.INVISIBLE);
                mInputSize.setVisibility(View.VISIBLE);
            }

            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();

                mInputSize.setText("" + width + "*" + height);
                mTimeCost.setVisibility(View.INVISIBLE);

                byte[] data = BitmapUtils.getDataFromImage(image, 0);
                image.close();

                LogUtils.i("Image accured" + image.toString());
                mPhotoPreviewHeight = width;
                mPhotoPreviewWidth = height;

                PhotoNightSceneHandler.Payload payload = new PhotoNightSceneHandler.Payload(data, width ,height, isOn);
                Message message = Message.obtain();
                message.obj = payload;
                message.what = PhotoNightSceneHandler.ADD_BUFFER;
                if (mPhotoNightSceneHandler != null) {
                    mPhotoNightSceneHandler.sendMessage(message);
                }
                saveIndex ++;
            }

            if(saveIndex == PhotoNightSceneHandler.PhotoNightScenePicCnt) {
                if (isOn) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.show(getString(R.string.mutil_photo_night_scene_process));
                        }
                    });
                }
            }
        }
    };

    private volatile PreviewStatus mPreviewStatus = PreviewStatus.PREVIEW_STATUS_PREVIEW;
    final private int mPicRequestWidth = 1920, mPicRequestHeight = 1080;
    private ProgramTextureNV21 programTextureNV21;
    private byte[] mPreviewBytes = null;
    private int mPhotoPreviewWidth = 0, mPhotoPreviewHeight = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this)
                .inflate(R.layout.activity_image_quality,
                        findViewById(R.id.fl_base_gl), true);
        initView();

        //   {zh} 默认弹出底部面板       {en} Bottom panel pops up by default
        showBoardFragment();
        removeButtonImgDefault();
        mPhotoNightSceneHandler = new PhotoNightSceneHandler(this, new ImageQualityResourceHelper(getApplicationContext()));
        mPhotoNightSceneHandler.setCallback(this);
        if (Config.ALGORITHM_MEMORY_SWITCH){
            Button button = new Button(this);
            button.setText("移除算法");
            ((FrameLayout) findViewById(R.id.viewroot)).addView(button, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            button.setOnClickListener(v->{
                if (mPhotoNightSceneHandler != null) {
                    mPhotoNightSceneHandler.destroy();
                }
                isOn = false;
            });
        }
    }

    private void initView() {
        findViewById(R.id.img_default_activity).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_open).setVisibility(View.INVISIBLE);
        findViewById(R.id.img_setting).setVisibility(View.INVISIBLE);
        mCircularProgressBar = findViewById(R.id.progress_circular);
        mConfig = parseConfig(getIntent());
        mDataManager = new ImageQualityDataManager();
        mBoardFragment = getBoardFragment(mConfig, mDataManager);
        if (mBoardFragment != null) {
            showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", false);
        }
    }

    @Override
    public boolean closeBoardFragment() {
        if (mPreviewStatus == PreviewStatus.PREVIEW_STATUS_AFTER_PROCESS) {
            return true;
        }
        hideBoardFragment(mBoardFragment);
        return true;
    }

    @Override
    public boolean showBoardFragment() {
        if (null == mBoardFragment) return false;
        showBoardFragment(mBoardFragment, R.id.fl_image_quality, "", true);
        //  {zh} 关闭 board 的拍照按钮  {en} Turn off the camera button on the board
        return true;
    }

    @Override
    public ProcessOutput processImpl(ProcessInput input) {
        ProcessOutput output = new ProcessOutput();
        output.width = input.getWidth();
        output.height = input.getHeight();
        output.texture  = input.getTexture();
        return output;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == com.effectsar.labcv.common.R.id.img_record) {
            if (!brustImage()) return ;

            view.setVisibility(View.INVISIBLE);

            if (isOn) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCircularProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }

        } else if (view.getId() == com.effectsar.labcv.common.R.id.img_back) {
            finish();
        } else if (view.getId() == R.id.img_open) {
            showBoardFragment();
        }
    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == com.effectsar.labcv.common.R.id.iv_record_board){
            if (!brustImage()) return ;

            if (isOn) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCircularProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }

            hideBoardAndUnderlingFragment(mBoardFragment, true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showOrHideBoard(true, false);
                }
            }, 0);
            showOrHideBoard(true, false);
        }else if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mBoardFragment);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (mPreviewStatus == PreviewStatus.PREVIEW_STATUS_PREVIEW) {
            super.onDrawFrame(gl10);
        } else {
            if (programTextureNV21 == null) {
                programTextureNV21 = new ProgramTextureNV21();
            }
            if (mPreviewBytes != null) {
                programTextureNV21.updateTexture(mPreviewBytes, mPhotoPreviewWidth, mPhotoPreviewHeight);
                ImageUtil.Transition drawTransition = new ImageUtil.Transition()
                        .crop(ImageView.ScaleType.CENTER_CROP, 0, mPhotoPreviewWidth, mPhotoPreviewHeight, mSurfaceWidth, mSurfaceHeight);
                programTextureNV21.drawFrameOnScreen(0, mSurfaceWidth, mSurfaceHeight, drawTransition.getMatrix());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPreviewStatus == PreviewStatus.PREVIEW_STATUS_PREVIEW_PHOTO) {
            mPreviewStatus = PreviewStatus.PREVIEW_STATUS_PREVIEW;
            findViewById(R.id.img_record).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mPhotoNightSceneHandler != null) {
                    mPhotoNightSceneHandler.destroy();
                    mPhotoNightSceneHandler = null;
                }

                if (programTextureNV21 != null) {
                    programTextureNV21.release();
                    programTextureNV21 = null;
                }

            }
        });
        super.onPause();
    }


    private Fragment getBoardFragment(ImageQualityConfig config, ImageQualityDataManager dataManager) {
        ImageQualityDataManager.ImageQualityItem item = dataManager.getItem(config.getKey());
        if (item == null) {
            return null;
        }
        HashSet selected =  new HashSet<ImageQualityDataManager.ImageQualityItem>();
        selected.add(item);
        Fragment fragment = new BoardFragment()
                .setSelectSet(selected)
                .setCallback(this)
                .setItem(item);
        return fragment;
    }

    @Override
    public void onItem(ImageQualityDataManager.ImageQualityItem item, boolean flag) {
        if (flag && mBubbleTipManager != null){
            mBubbleTipManager.show(item.getTitle(), item.getDesc());
        }
        isOn = flag;
    }


    private ImageQualityConfig parseConfig(Intent intent) {
        String sConfig = intent.getStringExtra(ImageQualityConfig.IMAGE_QUALITY_KEY);
        if (sConfig == null) {
            return null;
        }
        LogUtils.d("imagequlity config ="+sConfig);
        return new Gson().fromJson(sConfig, ImageQualityConfig.class);
    }

    public void removeButtonImgDefault(){
        useImgDefault = false;
        mImgDefault.setVisibility(View.GONE);
    }

    @Override
    public void onProcessFinished(byte[] buffer) {
        mPreviewBytes = buffer;
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    }

    @Override
    public void onProcessFinished(byte[] bytes, int width, int height, double time) {
        mPreviewStatus = PreviewStatus.PREVIEW_STATUS_AFTER_PROCESS;
        mPreviewBytes = bytes;
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCircularProgressBar.progressiveStop();
                mCircularProgressBar.setVisibility(View.INVISIBLE);
                if (isOn) {
                    mTimeCost.setVisibility(View.VISIBLE);
                    mTimeCost.setText("" + time + "ms");
                }
            }
        });

        if (bytes != null){
            Bitmap bitmap = BitmapUtils.getBitmapImageFromYUV(bytes, width, height);
            savePic(bitmap);
        }
    }

    private boolean brustImage() {
        LogUtils.i("click tack pick");
        ArrayList<Integer> aes = new ArrayList<>();
        if (isOn) {
            for (int i = 0; i < PhotoNightSceneHandler.PhotoNightScenePicCnt - 1; i++)
                aes.add(new Integer(0));
        }
        if (isOn) {
            aes.add(new Integer(-2));
        } else {
            aes.add(new Integer(0));
        }

        if ( isPhotoImageQualityNotSupport()) {
            ToastUtils.show("当前设备不支持多帧夜景");
            return  false;
        }
        boolean nightSupport = true;
        nightSupport = ((CameraSourceImpl)mImageSourceProvider).setBrust(mPicRequestWidth, mPicRequestHeight, aes, onImageAvailableListener);
        if (nightSupport == false) {
            ToastUtils.show("当前设备不支持多帧夜景");
            return  false;
        }

        mPreviewStatus = PreviewStatus.PREVIEW_STATUS_PREVIEW_PHOTO;

        //  {zh} 这里需要调换顺序  {en} We need to change the order here
        if (isOn) {
            ToastUtils.makeToast(getString(R.string.capture_mutil_photo));
        }
        findViewById(R.id.img_open).setVisibility(View.INVISIBLE);
        return true;
    }


    private void savePic(Bitmap bitmap) {
        File file =  BitmapUtils.saveToLocal(bitmap);

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.makeToast(getString(R.string.capture_ok));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return ;
    }

}
